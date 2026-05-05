package com.octavian.galactic.service;

import com.octavian.galactic.exception.DockingBayNotFoundException;
import com.octavian.galactic.exception.ShipNotFoundException;
import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.*;
import com.octavian.galactic.model.station.*;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.ShipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HubService {
    private static final Logger logger = LoggerFactory.getLogger(HubService.class);

    private List<SpaceShip> registeredShips = new ArrayList<>(); // Keep a record of every ship that has ever visited
    private Map<Integer, DockingBay> dockingBays = new HashMap<>(); // Manage physical locations
    private final String name;
    private final FuelDepot fuelDepot;
    private final ShipRepository shipRepository;
    private final DockingBayRepository dockingBayRepository;

    private Integer dockingBayNumber = 0; // For now, to prevent another O(n) search, the dockingBayNumber represents the manufacturing number
    // not an ordinal number, like in a parking lot. I would have to know which bay numbers previously existed and were unassigned
    // (like 1 2 x 4 x 6 7) and choose the first empty one to occupy.

    public HubService(String name, FuelDepot fuelDepot) {
        this.name = name;
        if (fuelDepot == null) throw new IllegalArgumentException("Fuel depot cannot be null");
        this.fuelDepot = fuelDepot;
        this.shipRepository = null;
        this.dockingBayRepository = null;
    }

    public HubService(String name, FuelDepot fuelDepot, ShipRepository shipRepository, DockingBayRepository dockingBayRepository) {
        this.name = name;
        if (fuelDepot == null) throw new IllegalArgumentException("Fuel depot cannot be null");
        this.fuelDepot = fuelDepot;
        if (shipRepository == null) throw new IllegalArgumentException("Ship repository cannot be null");
        this.shipRepository = shipRepository;
        if (dockingBayRepository == null) throw new IllegalArgumentException("Docking bay repository cannot be null");
        this.dockingBayRepository = dockingBayRepository;

        registeredShips = getPersistedShips();
        dockingBays = getPersistedBays();
        dockingBayNumber = dockingBays.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    public String getName() {
        return name;
    }

    public List<SpaceShip> getRegisteredShips() {
        registeredShips = getPersistedShips();
        return Collections.unmodifiableList(registeredShips);
    }

    public Map<Integer, DockingBay> getDockingBays() {
        dockingBays = getPersistedBays();
        return Collections.unmodifiableMap(dockingBays);
    }

    public void buildDockingBay(DockingBay dockingBay) {
        if (dockingBay == null) {
            throw new IllegalArgumentException("Docking bay cannot be null");
        }
        if (dockingBays.containsValue(dockingBay)) {
            logger.warn("Docking bay '{}' already exists.", dockingBay.getName());
//            System.out.println("Docking bay '" + dockingBay.getName() + "' already exists.");
            return;
        }
        dockingBay.setBayNumber(++dockingBayNumber);
        if (dockingBayRepository != null) {
            dockingBayRepository.save(dockingBay);
        } else {
            dockingBays.put(dockingBay.getBayNumber(), dockingBay);
        }

        AuditService.log(AuditService.Action.BAY_ADDED, dockingBay.getName());
    }

    public void removeDockingBay(UUID id) {
        if (dockingBayRepository != null) {
            dockingBays = getPersistedBays();
        }

        Optional<Map.Entry<Integer, DockingBay>> targetBay = dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .findFirst();

        targetBay.ifPresentOrElse(
                entry -> {
                    if (entry.getValue().isOccupied()) {
//                        System.out.println("[HUB] Error: Cannot remove Bay " + entry.getKey() + ". A ship is currently docked!");
                        logger.warn("[HUB] Error: Cannot remove Bay '{}'. A ship is currently docked!", entry.getKey());
                    } else {
                        if (dockingBayRepository != null) {
                            dockingBayRepository.delete(entry.getValue());
                        } else {
                            dockingBays.remove(entry.getKey());
                        }
//                        System.out.println("[HUB] Successfully removed bay: " + entry.getValue().getName());
                        logger.info("[HUB] Successfully removed bay: {}", entry.getValue().getName());
                        AuditService.log(AuditService.Action.BAY_REMOVED, entry.getValue().getName(), entry.getValue().getClass().getSimpleName());
                    }
                },
                () -> {
                    throw new DockingBayNotFoundException(id);
                }
        );
    }

    private Map<Integer, DockingBay> getPersistedBays() {
        if (dockingBayRepository == null) {
            return new HashMap<>(dockingBays);
        }
        List<DockingBay> bays = dockingBayRepository.findAll();

        return bays.stream()
                .collect(Collectors.toMap(
                        DockingBay::getBayNumber,
                        Function.identity()
                ));
    }

    public void registerShip(SpaceShip ship) {
        if (ship == null) {
            throw new IllegalArgumentException("[HUB] Error: Ship cannot be null");
        }
        if (registeredShips.contains(ship)) {
            logger.warn("[HUB] Error: Ship '{}' already in history", ship.getName());
//            System.out.printf("[HUB] Error: Ship '%s' already in history%n", ship.getName());
            return;
        }
        registeredShips.add(ship);
        AuditService.log(AuditService.Action.SHIP_REGISTERED, ship.getName(), ship.getClass().getSimpleName());
        if (shipRepository != null) {
            shipRepository.save(ship);
        }
    }

    private List<SpaceShip> getPersistedShips() {
        if (shipRepository == null) {
            return new ArrayList<>(registeredShips);
        }
        return shipRepository.findAll();
    }

    private List<SpaceShip> getPersistedShipsWithCargo() {
        if (shipRepository == null) {
            return new ArrayList<>(registeredShips);
        }
        return shipRepository.findAllWithCargo();
    }

    public void assignShipToBay(UUID id, int bayNumber) {
        dockingBays = getPersistedBays();
        registeredShips = getPersistedShips();

        DockingBay bay = dockingBays.get(bayNumber);
        if (bay == null) {
            throw new DockingBayNotFoundException(bayNumber);
        }

        registeredShips.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .ifPresentOrElse(
                        ship -> {
                            if (bay.isOccupied()) {
                                logger.warn("[HUB] Error: Docking Bay {} is already occupied by {}", bayNumber, ship.getName());
//                                System.out.println("[HUB] Error: Docking Bay " + bayNumber + " is already occupied by " + ship.getName());

                            } else if (ship.getShipSize().compareTo(bay.getBaySize()) <= 0) {
                                // Size validation is enforced by the bay itself through the dockSpaceShip method
                                try {
                                    bay.dockSpaceShip(ship);
                                    if (dockingBayRepository != null) {
                                        dockingBayRepository.update(bay);
                                    }
                                } catch (IllegalStateException e) {
                                    logger.error("[HUB] Error: ", e);
//                                    System.out.println("[HUB] Error: " + e.getMessage());
                                    return;
                                }
                                logger.info("[HUB] Success: '{}' parked in bay {}", ship.getName(), bayNumber);
                                AuditService.log(AuditService.Action.SHIP_DOCKED, ship.getName(), ship.getClass().getSimpleName());
//                                System.out.printf("[HUB] Success: '%s' parked in bay %d%n", ship.getName(), bayNumber);
                            } else {
                                logger.warn("[HUB] Error: Ship '{}' is too large for bay {}", ship.getName(), bayNumber);
//                                System.out.printf("[HUB] Error: Ship '%s' is too large for bay %d%n", ship.getName(), bayNumber);
                            }
                        },
                        () -> {
                            throw new ShipNotFoundException(id);
                        }
                );
    }

    public void unassignShipFromBay(UUID id) {
        dockingBays = getPersistedBays();
        registeredShips = getPersistedShips();

        if (dockingBays.isEmpty()) {
            logger.warn("[HUB] Error: No docking bays found.");
//            System.out.println("[HUB] Error: No docking bays found.");
            return;
        }

        dockingBays.values()
                .stream()
                .filter(entry -> entry.isOccupied() && entry.getSpaceShip().getId().equals(id))
                .findFirst()
                .ifPresentOrElse(
                        entry -> {
                            logger.info("Ship '{}' undocked from bay {}", entry.getSpaceShip().getName(), entry.getName());
                            entry.undockSpaceShip();
                            AuditService.log(AuditService.Action.SHIP_UNDOCKED, entry.getName(), entry.getClass().getSimpleName());
                            if (dockingBayRepository != null) {
                                dockingBayRepository.update(entry);
                            }
                        },
                        () -> {
                            throw new ShipNotFoundException(id);
                        }
                );
    }

    // Searches ship by UUID
    public void onboardCrewToShip(UUID id, CrewMember crew) {
        dockingBays = getPersistedBays();
        registeredShips = getPersistedShips();

        if (crew == null) {
            throw new IllegalArgumentException("[HUB] Error: Crew cannot be null");
        }

        dockingBays.values()
                .stream()
                .filter(entry -> entry.isOccupied() && entry.getSpaceShip().getId().equals(id))
                .findFirst()
                .ifPresentOrElse(entry -> {
                            entry.getSpaceShip().addCrewMember(crew);
                            AuditService.log(AuditService.Action.CREW_ONBOARDED, entry.getSpaceShip().getName(), "Crew onboard ship");
                            if (shipRepository != null) {
                                shipRepository.update(entry.getSpaceShip());
                            }
                        },
                        () -> {
                            throw new ShipNotFoundException(
                                    "Ship (" + id.toString().substring(0, 8) + ") is not docked — cannot board crew"
                            );
                        }
                );
    }

    public void transferCrewToShip(UUID fromShipId, UUID toShipId, UUID crewId) {
        Optional<Map.Entry<Integer, DockingBay>> fromShipDock = dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isOccupied() && entry.getValue().getSpaceShip().getId().equals(fromShipId))
                .findFirst();
        Optional<Map.Entry<Integer, DockingBay>> toShipDock = dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isOccupied() && entry.getValue().getSpaceShip().getId().equals(toShipId))
                .findFirst();
        if (fromShipDock.isEmpty()) {
            throw new ShipNotFoundException("Source ship (" + fromShipId.toString().substring(0, 8) + ") is not docked");
        }
        if (toShipDock.isEmpty()) {
            throw new ShipNotFoundException("Destination ship (" + toShipId.toString().substring(0, 8) + ") is not docked");
        }

        SpaceShip sourceShip = fromShipDock.get().getValue().getSpaceShip();
        SpaceShip destinationShip = toShipDock.get().getValue().getSpaceShip();

        Optional<CrewMember> crewToMove = sourceShip.getCrewMembers().stream()
                .filter(crew -> crew.getId().equals(crewId))
                .findFirst();

        crewToMove.ifPresentOrElse(crew -> {
                    sourceShip.removeCrewMember(crewId);
                    destinationShip.addCrewMember(crew);

                    AuditService.log(AuditService.Action.CREW_TRANSFERRED, sourceShip.getName(), destinationShip.getName());

                    if (shipRepository != null) {
                        shipRepository.update(sourceShip);
                        shipRepository.update(destinationShip);
                    }

                    logger.info("[HUB] Success: Transferred {} from '{}' to '{}'",
                            crew.getName(), sourceShip.getName(), destinationShip.getName());
//                    System.out.printf("[HUB] Success: Transferred %s from '%s' to '%s'.%n",
//                            crew.getName(), sourceShip.getName(), destinationShip.getName());
                },
                () -> {
                    throw new IllegalStateException("Crew member (" + crewId.toString().substring(0, 8) + ") not found on '" + sourceShip.getName() + "'");
                }
        );
    }

    // Chooses ship to scan by  UUID
    public boolean scanShipForHazards(UUID shipId) {
        // Collect all occupied docking bays
        List<Map.Entry<Integer, DockingBay>> occupiedDockingBays = dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isOccupied())
                .toList();

        Optional<Map.Entry<Integer, DockingBay>> targetBay = occupiedDockingBays.stream()
                .filter(entry -> entry.getValue().getSpaceShip().getId().equals(shipId))
                .findFirst();
        if (targetBay.isEmpty()) {
            throw new ShipNotFoundException("Ship (" + shipId.toString().substring(0, 8) + ") is not docked — cannot scan");
        }

        SpaceShip targetShip = targetBay.get().getValue().getSpaceShip();

        if (targetShip instanceof CargoShip cargoShip) {
            if (shipRepository != null) {
                cargoShip = shipRepository.findByIdWithCargo(shipId)
                        .filter(CargoShip.class::isInstance)
                        .map(CargoShip.class::cast)
                        .orElse(cargoShip);
            }

            Set<Map.Entry<CargoItem, Integer>> hazardousCargoManifest = cargoShip.getCargoManifest().entrySet().stream()
                    .filter(cargoEntry -> cargoEntry.getKey() instanceof HazardousCargo)
                    .collect(Collectors.toSet());

            if (hazardousCargoManifest.isEmpty()) {
                logger.info("[HUB] No hazardous materials detected.");
//                System.out.println("[HUB] No hazardous materials detected.");
                return false;
            }

            logger.info("[HUB] HAZARDOUS MATERIALS DETECTED. Printing unsafe cargo report...");
            logger.info(hazardousCargoManifest.toString());
            AuditService.log(AuditService.Action.HAZARD_SCAN, targetShip.getName(), "Hazard manifest:" + hazardousCargoManifest);
//            System.out.println("[HUB] HAZARDOUS MATERIALS DETECTED. Printing unsafe cargo report...");
//            System.out.println(hazardousCargoManifest);
            return true;
        } else {
            logger.warn("[HUB] The selected ship isn't a cargo ship. Scan aborted.");
//            System.out.println("[HUB] The selected ship isn't a cargo ship. Scan aborted.");
        }
        return false;
    }

    // I may want this to return a Set<CrewMember>. For now, it prints the personnelReport.
    public Set<CrewMember> generatePersonnelReport() {
        registeredShips = getPersistedShips();

        Set<CrewMember> personnelReport = new TreeSet<>();
        if (registeredShips.isEmpty()) {
            logger.warn("[HUB] No ships have been registered so far.");
//            System.out.println("[HUB] No ships have been registered so far.");
            return Set.of();
        }

        for (SpaceShip ship : registeredShips) {
            personnelReport.addAll(ship.getCrewMembers());
        }

//        System.out.println("[HUB] Personnel report:");
//        System.out.println(personnelReport);
        return personnelReport;
    }

    public double calculateDockingFeesPerShip(UUID shipId) {
        if (dockingBays.isEmpty()) {
            logger.warn("[HUB] No docking bays exist.");
//            System.out.println("[HUB] No docking bays exist.");
            return 0.0;
        }

        // Pricing Model Constants
        final double FUEL_COST_PER_UNIT = 2.5;
        final double REPAIR_COST_PER_UNIT = 15.0;

        Optional<SpaceShip> ship = dockingBays.values().stream()
                .filter(bay -> bay.isOccupied() && bay.getSpaceShip().getId().equals(shipId))
                .map(DockingBay::getSpaceShip)
                .findFirst();
        if (ship.isEmpty()) {
            throw new ShipNotFoundException("[HUB] Couldn't locate ship with id: " + shipId.toString().substring(0, 8));
        }
        SpaceShip dockedShip = ship.get();

        int fuelNeeded = 100 - dockedShip.getFuelLevel();
        int repairsNeeded = 100 - dockedShip.getHullIntegrity();

        double resourceCost = (fuelNeeded * FUEL_COST_PER_UNIT) + (repairsNeeded * REPAIR_COST_PER_UNIT);

        double baseFee;
        double serviceMultiplier; // Standard labor rate

        switch (dockedShip) {
            case CargoShip _ -> {
                baseFee = 500.0; // Heavy-duty docking fee
                serviceMultiplier = 1.5; // Commercial surcharge for parts and labor
            }
            case ScoutShip _ -> {
                baseFee = 100.0;
                serviceMultiplier = 1.0;
            } // Standard light docking fee
            case FighterShip _ -> {
                baseFee = 200;
                serviceMultiplier = 1.2;
            }
            default ->
                    throw new IllegalArgumentException("Unknown ship type '" + ship.getClass().getSimpleName() + "' — no billing rate defined");
        }

        // Calculate final bill for this ship
        double shipTotalBill = baseFee + (resourceCost * serviceMultiplier);

        // Perform the maintenance
        if (fuelNeeded > 0) {
            if (fuelDepot.fuelTankIsEmpty()) {
                logger.warn("[HUB-BILLING] Warning: depot empty, '{}' could not be refueled",
                        dockedShip.getName());
//                System.out.printf("[HUB-BILLING] Warning: depot empty, '%s' could not be refueled%n",
//                        dockedShip.getName());
                fuelNeeded = 0; // No fuel dispensed, don't bill for it
            } else {
                int dispensable = Math.min(fuelNeeded, fuelDepot.getFuelLevel());
                fuelDepot.dispenseFuel(dockedShip, dispensable);
                fuelNeeded = dispensable; // Bill only for what was actually dispensed
            }
        }
        if (repairsNeeded > 0) dockedShip.setHullIntegrity(100);

        if (shipRepository != null) {
            shipRepository.update(dockedShip);
        }

        // I may want to decouple the invoice logic from the calculation of the docking fee
        // Generate Invoice
        logger.info("[HUB-BILLING] Invoice for ({})'{}' :\n ----> Base Fee: {}\n ----> Fuel Added: {} units | Repairs: {} units \n ----> Total Charged: {} credits", dockedShip.getClass().getSimpleName(), dockedShip.getName(), String.format("%.2f", baseFee), fuelNeeded, repairsNeeded, String.format("%.2f",shipTotalBill));
//        System.out.printf("[HUB-BILLING] Invoice for (%s)'%s' :%n", dockedShip.getClass().getSimpleName(), dockedShip.getName());
//        System.out.printf("----> Base Fee: %.2f%n", baseFee);
//        System.out.printf("----> Fuel Added: %d units | Repairs: %d units%n", fuelNeeded, repairsNeeded);
//        System.out.printf("----> Total Charged: %.2f credits%n", shipTotalBill);
        AuditService.log(AuditService.Action.BILLING_GENERATED, dockedShip.getName(), dockedShip.getClass().getSimpleName());
        return shipTotalBill;
    }


    public double calculateTotalDockingFees() {

        double totalRevenue = 0.0;
        for (DockingBay bay : dockingBays.values()) {
            if (bay.isOccupied()) {
                SpaceShip ship = bay.getSpaceShip();

                totalRevenue += calculateDockingFeesPerShip(ship.getId());
                if (shipRepository != null) {
                    shipRepository.update(ship);
                }
            }
        }

//        System.out.printf("[HUB-BILLING] End of day report: Total Station Revenue = %.2f credits.%n", totalRevenue);
        logger.info("[HUB-BILLING] End of day report: Total Station Revenue = {} credits.%n", String.format("%.2f", totalRevenue));
        return totalRevenue;
    }

    private double calculateCargoWeight(CargoShip ship) {
        if (ship == null) {
            logger.warn("CargoShip cannot be null.");
//            System.out.println("CargoShip cannot be null.");
            return -1.0;
        }

        return ship.getCargoManifest().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getWeight() * entry.getValue())
                .sum();
    }

    // Uses a string filter. I haven't decided yet how the user will interact with this.
    // 'all time' to search through registeredShips; 'docked' to search through the currently docked ships
    public Optional<CargoShip> findHeaviestCargoShip(String filter) {
        registeredShips = getPersistedShipsWithCargo();
        Optional<CargoShip> heaviestShip;

        if (filter.equalsIgnoreCase("all time")) {
            if (registeredShips.isEmpty()) {
                logger.warn("[HUB] No ships have been registered yet.");
//                System.out.println("[HUB] No ships have been registered yet.");
                return Optional.empty();
            }

            heaviestShip = registeredShips.stream()
                    .filter(ship -> ship instanceof CargoShip)
                    .distinct()
                    .map(ship -> (CargoShip) ship)
                    .max(Comparator.comparingDouble(this::calculateCargoWeight));
        } else if (filter.equalsIgnoreCase("docked")) {
            if (dockingBays.isEmpty()) {
                logger.warn("[HUB][HeaviestCargoShip] No docking bays exist.");
//                System.out.println("[HUB] No docking bays exist.");
                return Optional.empty();
            }

            heaviestShip = dockingBays.values().stream()
                    .filter(DockingBay::isOccupied)
                    .map(DockingBay::getSpaceShip)
                    .filter(ship -> ship instanceof CargoShip)
                    .map(ship -> (CargoShip) ship)
                    .max(Comparator.comparingDouble(this::calculateCargoWeight));
        } else {
            logger.warn("[HUB] Error: Invalid filter '{}'. Use 'all time' or 'docked'.", filter);
            return Optional.empty();
        }

        heaviestShip.ifPresentOrElse(
                ship -> logger.info("[HUB] The heaviest {} cargo ship is '{}' carrying {} Tonnes.",
                        filter.toLowerCase(), ship.getName(), String.format("%.2f",calculateCargoWeight(ship))),
//                        System.out.printf("[HUB] The heaviest %s cargo ship is '%s' carrying %.2f Tonnes.%n",
//                        filter.toLowerCase(), ship.getName(), calculateCargoWeight(ship)),
                () -> logger.info("[HUB] No cargo ships found matching the '{}'", filter)
        );
        return heaviestShip;
    }

    public List<DockingBay> getBaysByStatus(boolean occupied) {
        List<DockingBay> filteredBays; // Unmodifiable because of the toList() terminal operation
        filteredBays = dockingBays.values().stream()
                .filter(bay -> bay.isOccupied() == occupied)
                .toList();

        if (filteredBays.isEmpty()) {
            String occupiedString = occupied ? "[HUB] No docking bays are currently occupied." : "[HUB] All docking bays are full.";
            logger.info(occupiedString);
//            System.out.println(occupied ? "[HUB] No docking bays are currently occupied." : "[HUB] All docking bays are full.");
        }

        return filteredBays.isEmpty() ? new ArrayList<>() : filteredBays;
    }

    public void emergencyEvacuation() {
        if (dockingBays.isEmpty()) {
            logger.warn("[HUB] All docking bays are empty. There is no one to evacuate");
//            System.out.println("[HUB] All docking bays are empty. There is no one to evacuate");
            return;
        }

        int totalEvacuated;

        totalEvacuated = dockingBays.values().stream()
                .filter(DockingBay::isOccupied)
                .mapToInt(bay -> bay.getSpaceShip().getCrewMembers().size())
                .sum();
        if (totalEvacuated == 0) {
            logger.warn("[HUB] Ships didn't have anyone on board. There is no one to evacuate");
//            System.out.println("[HUB] Ships didn't have anyone on board. There is no one to evacuate");
            return;
        }

        dockingBays.values().stream()
                .filter(DockingBay::isOccupied)
                .forEach(dockingBay -> {
                    dockingBay.undockSpaceShip();
                    AuditService.log(AuditService.Action.EMERGENCY_EVACUATION, dockingBay.getName());
                    if (dockingBayRepository != null) {
                        dockingBayRepository.update(dockingBay);
                    }
                });
        logger.info("[HUB] EMERGENCY OVERRIDE: Successfully evacuated {} personnel", totalEvacuated);
//        System.out.printf("[HUB] EMERGENCY OVERRIDE: Successfully evacuated %d personnel%n", totalEvacuated);
    }
}
