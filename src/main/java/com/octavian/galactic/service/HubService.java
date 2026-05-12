package com.octavian.galactic.service;

import com.octavian.galactic.exception.DockingBayNotFoundException;
import com.octavian.galactic.exception.ShipNotFoundException;
import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.*;
import com.octavian.galactic.model.station.*;
import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.FuelDepotRepository;
import com.octavian.galactic.repository.ShipRepository;
import org.javatuples.Pair;
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
    private final FuelDepotRepository fuelDepotRepository;

    /**
     * For now, to prevent another O(n) search, the dockingBayNumber represents the manufacturing number,
     * not an ordinal number, like in a parking lot. I would have to know which bay numbers previously existed and were unassigned
     * (like 1 2 x 4 x 6 7) and choose the first empty one to occupy.
     */
    private Integer dockingBayNumber = 0;

    public HubService(String name, FuelDepot fuelDepot) {
        this.name = name;
        if (fuelDepot == null) throw new IllegalArgumentException("Fuel depot cannot be null");
        this.fuelDepot = fuelDepot;
        this.shipRepository = null;
        this.dockingBayRepository = null;
        this.fuelDepotRepository = null;
    }

    public HubService(String name, FuelDepot fuelDepot, ShipRepository shipRepository, DockingBayRepository dockingBayRepository, FuelDepotRepository fuelDepotRepository) {
        this.name = name;
        if (fuelDepot == null) throw new IllegalArgumentException("Fuel depot cannot be null");
        this.fuelDepot = fuelDepot;
        if (shipRepository == null) throw new IllegalArgumentException("Ship repository cannot be null");
        this.shipRepository = shipRepository;
        if (dockingBayRepository == null) throw new IllegalArgumentException("Docking bay repository cannot be null");
        this.dockingBayRepository = dockingBayRepository;
        if (fuelDepotRepository == null) throw new IllegalArgumentException("Fuel depot repository cannot be null");
        this.fuelDepotRepository = fuelDepotRepository;

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
            logger.info("Docking bay '{}' already exists.", dockingBay.getName());
//            System.out.println("Docking bay '" + dockingBay.getName() + "' already exists.");
            return;
        }
        dockingBay.setBayNumber(++dockingBayNumber);
        if (dockingBayRepository != null) {
            dockingBayRepository.save(dockingBay);
        } else {
            dockingBays.put(dockingBay.getBayNumber(), dockingBay);
        }

        AuditService.getInstance().log(AuditService.Action.BAY_ADDED, dockingBay.getName());
    }

    public void removeDockingBay(UUID id) {
        if (dockingBayRepository != null) {
            DockingBay bay = dockingBayRepository.findById(id)
                    .orElseThrow(() -> new DockingBayNotFoundException(id));
            if (bay.isOccupied()) {
                logger.warn("[HUB] Error: Cannot remove Bay '{}'. A ship is currently docked!", bay.getBayNumber());
            } else {
                dockingBayRepository.delete(bay);
                logger.info("[HUB] Successfully removed bay: {}", bay.getName());
                AuditService.getInstance().log(AuditService.Action.BAY_REMOVED, bay.getName(), bay.getClass().getSimpleName());
            }
            return;
        }

        Optional<Map.Entry<Integer, DockingBay>> targetBay = dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .findFirst();

        targetBay.ifPresentOrElse(
                entry -> {
                    if (entry.getValue().isOccupied()) {
                        logger.warn("[HUB] Error: Cannot remove Bay '{}'. A ship is currently docked!", entry.getKey());
                    } else {
                        dockingBays.remove(entry.getKey());
                        logger.info("[HUB] Successfully removed bay: {}", entry.getValue().getName());
                        AuditService.getInstance().log(AuditService.Action.BAY_REMOVED, entry.getValue().getName(), entry.getValue().getClass().getSimpleName());
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
            logger.info("[HUB] Error: Ship '{}' already in history", ship.getName());
//            System.out.printf("[HUB] Error: Ship '%s' already in history%n", ship.getName());
            return;
        }
        registeredShips.add(ship);
        AuditService.getInstance().log(AuditService.Action.SHIP_REGISTERED, ship.getName(), ship.getClass().getSimpleName());
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

    public void assignShipToBay(UUID id, int bayNumber) {
        if (dockingBayRepository != null) {
            DockingBay bay = dockingBayRepository.findByBayNumberWithShip(bayNumber)
                    .orElseThrow(() -> new DockingBayNotFoundException(bayNumber));
            SpaceShip ship = shipRepository.findById(id)
                    .orElseThrow(() -> new ShipNotFoundException(id));

            if (bay.isOccupied()) {
                logger.info("[HUB] Error: Docking Bay {} is already occupied by {}", bayNumber, bay.getSpaceShip().getName());
            } else if (ship.getShipSize().compareTo(bay.getBaySize()) <= 0) {
                try {
                    bay.dockSpaceShip(ship);
                    dockingBayRepository.update(bay);
                    shipRepository.update(ship);
                } catch (IllegalStateException e) {
                    logger.error("[HUB] Error: {}", e.getMessage());
                    return;
                }
                logger.info("[HUB] Success: '{}' parked in bay {}", ship.getName(), bayNumber);
                AuditService.getInstance().log(AuditService.Action.SHIP_DOCKED, ship.getName(), ship.getClass().getSimpleName());
            } else {
                logger.warn("[HUB] Error: Ship '{}' is too large for bay {}", ship.getName(), bayNumber);
            }
            return;
        }

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
                                logger.info("[HUB] Error: Docking Bay {} is already occupied by {}", bayNumber, ship.getName());
                            } else if (ship.getShipSize().compareTo(bay.getBaySize()) <= 0) {
                                // Size validation is enforced by the bay itself through the dockSpaceShip method
                                try {
                                    bay.dockSpaceShip(ship);
                                } catch (IllegalStateException e) {
                                    logger.error("[HUB] Error: {}", e.getMessage());
                                    return;
                                }
                                logger.info("[HUB] Success: '{}' parked in bay {}", ship.getName(), bayNumber);
                                AuditService.getInstance().log(AuditService.Action.SHIP_DOCKED, ship.getName(), ship.getClass().getSimpleName());
                            } else {
                                logger.warn("[HUB] Error: Ship '{}' is too large for bay {}", ship.getName(), bayNumber);
                            }
                        },
                        () -> {
                            throw new ShipNotFoundException(id);
                        }
                );
    }

    public void unassignShipFromBay(UUID id) {
        if (dockingBayRepository != null) {
            dockingBayRepository.findOccupiedByShipId(id)
                    .ifPresentOrElse(
                            bay -> {
                                SpaceShip ship = bay.getSpaceShip();
                                logger.info("Ship '{}' undocked from bay {}", ship.getName(), bay.getName());
                                bay.undockSpaceShip();
                                AuditService.getInstance().log(AuditService.Action.SHIP_UNDOCKED, bay.getName(), bay.getClass().getSimpleName());
                                dockingBayRepository.update(bay);
                                shipRepository.update(ship);
                            },
                            () -> {
                                throw new ShipNotFoundException(id);
                            }
                    );
            return;
        }

        if (dockingBays.isEmpty()) {
            logger.warn("[HUB] Error: No docking bays found.");
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
                            AuditService.getInstance().log(AuditService.Action.SHIP_UNDOCKED, entry.getName(), entry.getClass().getSimpleName());
                        },
                        () -> {
                            throw new ShipNotFoundException(id);
                        }
                );
    }

    // Searches ship by UUID
    public void onboardCrewToShip(UUID id, CrewMember crew) {
        if (crew == null) {
            throw new IllegalArgumentException("[HUB] Error: Crew cannot be null");
        }

        if (dockingBayRepository != null) {
            dockingBayRepository.findOccupiedByShipId(id)
                    .ifPresentOrElse(
                            bay -> {
                                bay.getSpaceShip().addCrewMember(crew);
                                AuditService.getInstance().log(AuditService.Action.CREW_ONBOARDED, bay.getSpaceShip().getName(), "Crew onboard ship");
                                shipRepository.update(bay.getSpaceShip());
                            },
                            () -> {
                                throw new ShipNotFoundException(
                                        "Ship (" + id.toString().substring(0, 8) + ") is not docked — cannot board crew"
                                );
                            }
                    );
            return;
        }

        dockingBays.values()
                .stream()
                .filter(entry -> entry.isOccupied() && entry.getSpaceShip().getId().equals(id))
                .findFirst()
                .ifPresentOrElse(entry -> {
                            entry.getSpaceShip().addCrewMember(crew);
                            AuditService.getInstance().log(AuditService.Action.CREW_ONBOARDED, entry.getSpaceShip().getName(), "Crew onboard ship");
                        },
                        () -> {
                            throw new ShipNotFoundException(
                                    "Ship (" + id.toString().substring(0, 8) + ") is not docked — cannot board crew"
                            );
                        }
                );
    }

    public void transferCrewToShip(UUID fromShipId, UUID toShipId, UUID crewId) {
        final SpaceShip sourceShip;
        final SpaceShip destinationShip;

        if (dockingBayRepository != null) {
            sourceShip = dockingBayRepository.findOccupiedByShipId(fromShipId)
                    .map(DockingBay::getSpaceShip)
                    .orElseThrow(() -> new ShipNotFoundException("Source ship (" + fromShipId.toString().substring(0, 8) + ") is not docked"));
            destinationShip = dockingBayRepository.findOccupiedByShipId(toShipId)
                    .map(DockingBay::getSpaceShip)
                    .orElseThrow(() -> new ShipNotFoundException("Destination ship (" + toShipId.toString().substring(0, 8) + ") is not docked"));
        } else {
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
            sourceShip = fromShipDock.get().getValue().getSpaceShip();
            destinationShip = toShipDock.get().getValue().getSpaceShip();
        }

        Optional<CrewMember> crewToMove = sourceShip.getCrewMembers().stream()
                .filter(crew -> crew.getId().equals(crewId))
                .findFirst();

        crewToMove.ifPresentOrElse(crew -> {
                    sourceShip.removeCrewMember(crewId);
                    destinationShip.addCrewMember(crew);

                    AuditService.getInstance().log(AuditService.Action.CREW_TRANSFERRED, sourceShip.getName(), destinationShip.getName());

                    if (shipRepository != null) {
                        shipRepository.update(sourceShip);
                        shipRepository.update(destinationShip);
                    }

                    logger.info("[HUB] Success: Transferred {} from '{}' to '{}'",
                            crew.getName(), sourceShip.getName(), destinationShip.getName());
                },
                () -> {
                    throw new IllegalStateException("Crew member (" + crewId.toString().substring(0, 8) + ") not found on '" + sourceShip.getName() + "'");
                }
        );
    }

    // Chooses ship to scan by UUID
    public boolean scanShipForHazards(UUID shipId) {
        final SpaceShip targetShip;

        if (dockingBayRepository != null) {
            // Single query validates docking status and eagerly loads cargo for CargoShips
            targetShip = dockingBayRepository.findOccupiedByShipIdWithCargo(shipId)
                    .map(DockingBay::getSpaceShip)
                    .orElseThrow(() -> new ShipNotFoundException(
                            "Ship (" + shipId.toString().substring(0, 8) + ") is not docked — cannot scan"));
        } else {
            SpaceShip found = dockingBays.values().stream()
                    .filter(dockingBay -> dockingBay.isOccupied()
                            && dockingBay.getSpaceShip().getId().equals(shipId))
                    .map(DockingBay::getSpaceShip)
                    .findFirst()
                    .orElseThrow(() -> new ShipNotFoundException(
                            "Ship (" + shipId.toString().substring(0, 8) + ") is not docked — cannot scan"));
            if (found instanceof CargoShip && shipRepository != null) {
                found = shipRepository.findByIdWithCargo(shipId)
                        .filter(CargoShip.class::isInstance)
                        .orElse(found);
            }
            targetShip = found;
        }

        if (targetShip instanceof CargoShip cargoShip) {
            Set<Map.Entry<CargoItem, Integer>> hazardousCargoManifest = cargoShip.getCargoManifest().entrySet().stream()
                    .filter(cargoEntry -> cargoEntry.getKey() instanceof HazardousCargo)
                    .collect(Collectors.toSet());

            if (hazardousCargoManifest.isEmpty()) {
                logger.info("[HUB] No hazardous materials detected.");
                return false;
            }

            logger.info("[HUB] HAZARDOUS MATERIALS DETECTED. Printing unsafe cargo report...");
            logger.info(hazardousCargoManifest.toString());
            AuditService.getInstance().log(AuditService.Action.HAZARD_SCAN, targetShip.getName(), "Hazard manifest:" + hazardousCargoManifest);
            return true;
        } else {
            logger.info("[HUB] The selected ship isn't a cargo ship. Scan aborted.");
        }
        return false;
    }

    public Set<CrewMember> generatePersonnelReport() {
        if (shipRepository != null) {
            List<CrewMember> crew = shipRepository.findAllCrew();
            if (crew.isEmpty()) {
                logger.warn("[HUB] No ships have been registered so far.");
                return Set.of();
            }
            return new TreeSet<>(crew);
        }

        if (registeredShips.isEmpty()) {
            logger.warn("[HUB] No ships have been registered so far.");
            return Set.of();
        }

        Set<CrewMember> personnelReport = new TreeSet<>();
        for (SpaceShip ship : registeredShips) {
            personnelReport.addAll(ship.getCrewMembers());
        }
        return personnelReport;
    }

    private SpaceShip findDockedShipById(UUID shipId) {
        if (dockingBayRepository != null) {
            return dockingBayRepository.findOccupiedByShipId(shipId)
                    .map(DockingBay::getSpaceShip)
                    .orElseThrow(() -> new ShipNotFoundException(
                            "[HUB] Couldn't locate ship with id: " + shipId.toString().substring(0, 8)));
        }
        return dockingBays.values().stream()
                .filter(bay -> bay.isOccupied() && bay.getSpaceShip().getId().equals(shipId))
                .map(DockingBay::getSpaceShip)
                .findFirst()
                .orElseThrow(() -> new ShipNotFoundException(
                        "[HUB] Couldn't locate ship with id: " + shipId.toString().substring(0, 8)));
    }

    public double calculateDockingFeesPerShip(UUID shipId) {
        // Pricing Model Constants
        final double FUEL_COST_PER_UNIT = 2.5;
        final double REPAIR_COST_PER_UNIT = 15.0;

        SpaceShip dockedShip = findDockedShipById(shipId);

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
                    throw new IllegalArgumentException("Unknown ship type '" + dockedShip.getClass().getSimpleName() + "' — no billing rate defined");
        }

        // Calculate final bill for this ship
        double shipTotalBill = baseFee + (resourceCost * serviceMultiplier);

        // Perform the maintenance
        if (fuelNeeded > 0) {
            if (fuelDepot.fuelTankIsEmpty()) {
                logger.warn("[HUB-BILLING] Warning: depot empty, '{}' could not be refueled",
                        dockedShip.getName());
                fuelNeeded = 0; // No fuel dispensed, don't bill for it
            } else {
                int dispensable = Math.min(fuelNeeded, fuelDepot.getFuelLevel());
                fuelDepot.dispenseFuel(dockedShip, dispensable);
                fuelNeeded = dispensable; // Bill only for what was actually dispensed
                if (fuelDepotRepository != null) {
                    fuelDepotRepository.update(fuelDepot);
                }
            }
        }
        if (repairsNeeded > 0) dockedShip.setHullIntegrity(100);

        if (shipRepository != null) {
            shipRepository.update(dockedShip);
        }

        // I may want to decouple the invoice logic from the calculation of the docking fee
        // Generate Invoice
        logger.info("[HUB-BILLING] Invoice for ({})'{}' :\n ----> Base Fee: {}\n ----> Fuel Added: {} units | Repairs: {} units \n ----> Total Charged: {} credits", dockedShip.getClass().getSimpleName(), dockedShip.getName(), String.format("%.2f", baseFee), fuelNeeded, repairsNeeded, String.format("%.2f", shipTotalBill));

        AuditService.getInstance().log(AuditService.Action.BILLING_GENERATED, dockedShip.getName(), dockedShip.getClass().getSimpleName());
        return shipTotalBill;
    }


    public double calculateTotalDockingFees() {
        List<DockingBay> occupiedBays;
        if (dockingBayRepository != null) {
            occupiedBays = dockingBayRepository.findByOccupied(true);
        } else {
            occupiedBays = dockingBays.values().stream()
                    .filter(DockingBay::isOccupied)
                    .toList();
        }

        double totalRevenue = 0.0;
        for (DockingBay bay : occupiedBays) {
            // calculateDockingFeesPerShip fetches a fresh copy of the ship, applies changes, and persists it internally
            totalRevenue += calculateDockingFeesPerShip(bay.getSpaceShip().getId());
        }

        logger.info("[HUB-BILLING] End of day report: Total Station Revenue = {} credits.", String.format("%.2f", totalRevenue));
        return totalRevenue;
    }

    private double calculateCargoWeight(CargoShip ship) {
        if (ship == null) {
            logger.warn("CargoShip cannot be null.");
            return -1.0;
        }

        return ship.getCargoManifest().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getWeight() * entry.getValue())
                .sum();
    }

    // Uses a string filter. I haven't decided yet how the user will interact with this.
    // 'all time' to search through registeredShips; 'docked' to search through the currently docked ships
    public Optional<CargoShip> findHeaviestCargoShip(String filter) {
        Optional<CargoShip> heaviestShip;

        if (filter.equalsIgnoreCase("all time")) {
            List<SpaceShip> cargoShips = (shipRepository != null)
                    ? shipRepository.findAllCargoShipsWithCargo()
                    : registeredShips;

            if (cargoShips.isEmpty()) {
                logger.warn("[HUB] No ships have been registered yet.");
                return Optional.empty();
            }

            heaviestShip = cargoShips.stream()
                    .filter(ship -> ship instanceof CargoShip)
                    .distinct()
                    .map(ship -> (CargoShip) ship)
                    .max(Comparator.comparingDouble(this::calculateCargoWeight));

        } else if (filter.equalsIgnoreCase("docked")) {
            if (dockingBayRepository != null) {
                heaviestShip = dockingBayRepository.findDockedCargoShipsWithCargo().stream()
                        .map(DockingBay::getSpaceShip)
                        .filter(ship -> ship instanceof CargoShip)
                        .map(ship -> (CargoShip) ship)
                        .max(Comparator.comparingDouble(this::calculateCargoWeight));
            } else {
                if (dockingBays.isEmpty()) {
                    logger.warn("[HUB][HeaviestCargoShip] No docking bays exist.");
                    return Optional.empty();
                }
                heaviestShip = dockingBays.values().stream()
                        .filter(DockingBay::isOccupied)
                        .map(DockingBay::getSpaceShip)
                        .filter(ship -> ship instanceof CargoShip)
                        .map(ship -> (CargoShip) ship)
                        .max(Comparator.comparingDouble(this::calculateCargoWeight));
            }
        } else {
            logger.warn("[HUB] Error: Invalid filter '{}'. Use 'all time' or 'docked'.", filter);
            return Optional.empty();
        }

        heaviestShip.ifPresentOrElse(
                ship -> logger.info("[HUB] The heaviest {} cargo ship is '{}' carrying {} Tonnes.",
                        filter.toLowerCase(), ship.getName(), String.format("%.2f", calculateCargoWeight(ship))),
                () -> logger.info("[HUB] No cargo ships found matching the '{}'", filter)
        );
        return heaviestShip;
    }

    public List<DockingBay> getBaysByStatus(boolean occupied) {
        List<DockingBay> filteredBays;
        if (dockingBayRepository != null) {
            filteredBays = dockingBayRepository.findByOccupied(occupied);
        } else {
            filteredBays = dockingBays.values().stream()
                    .filter(bay -> bay.isOccupied() == occupied)
                    .toList();
        }

        if (filteredBays.isEmpty()) {
            String occupiedString = occupied ? "[HUB] No docking bays are currently occupied." : "[HUB] All docking bays are full.";
            logger.info(occupiedString);
        }

        return filteredBays.isEmpty() ? new ArrayList<>() : filteredBays;
    }

    public void emergencyEvacuation() {
        List<DockingBay> occupiedBays;
        if (dockingBayRepository != null) {
            occupiedBays = dockingBayRepository.findByOccupied(true);
        } else {
            occupiedBays = dockingBays.values().stream()
                    .filter(DockingBay::isOccupied)
                    .toList();
        }

        if (occupiedBays.isEmpty()) {
            logger.warn("[HUB] All docking bays are empty. There is no one to evacuate");
            return;
        }

        int totalEvacuated = occupiedBays.stream()
                .mapToInt(bay -> bay.getSpaceShip().getCrewMembers().size())
                .sum();

        if (totalEvacuated == 0) {
            logger.warn("[HUB] Ships didn't have anyone on board. There is no one to evacuate");
            return;
        }

        occupiedBays.forEach(bay -> {
            SpaceShip ship = bay.getSpaceShip();
            bay.undockSpaceShip();
            AuditService.getInstance().log(AuditService.Action.EMERGENCY_EVACUATION, bay.getName());
            if (dockingBayRepository != null) {
                dockingBayRepository.update(bay);
                shipRepository.update(ship);
            }
        });
        logger.info("[HUB] EMERGENCY OVERRIDE: Successfully evacuated {} personnel", totalEvacuated);
    }

    public Pair<Integer, Integer> fuelDepotStats(FuelDepot fuelDepot) {
        Pair<Integer, Integer> fuelDepotDetails = Pair.with(-1, -1);
        if (fuelDepot != null) {
            fuelDepotDetails = fuelDepotDetails.setAt0(fuelDepot.getFuelLevel());
            fuelDepotDetails = fuelDepotDetails.setAt1(fuelDepot.getFuelCapacity());
        }
        else{
            throw new IllegalStateException("Fuel depot cannot be null");
        }
        return fuelDepotDetails;
    }
}
