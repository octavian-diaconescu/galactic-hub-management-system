package com.octavian.galactic.service;

import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;

import java.util.*;
import java.util.stream.Collectors;

public class HubService {
    private final List<SpaceShip> registeredShips = new ArrayList<>(); // Keep a record of every ship that has ever visited
    private final Map<Integer, DockingBay> dockingBays = new HashMap<>(); // Manage physical locations
    private final String name;
    private Integer dockingBayNumber = 0; // For now, to prevent another O(n) search, the dockingBayNumber represents the manufacturing number
    // not an ordinal number, like in a parking lot. I would have to know which bay numbers previously existed and were unassigned
    // (like 1 2 x 4 x 6 7) and choose the first empty one to occupy.

    public HubService(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<SpaceShip> getRegisteredShips() {
        return Collections.unmodifiableList(registeredShips);
    }

    public Map<Integer, DockingBay> getDockingBays() {
        return Collections.unmodifiableMap(dockingBays);
    }

    public void buildDockingBay(DockingBay dockingBay) {
        if (dockingBay == null) {
            throw new IllegalArgumentException("Docking bay cannot be null");
        }
        if (dockingBays.containsValue(dockingBay)) {
            System.out.println("Docking bay '" + dockingBay.getName() + "' already exists.");
            return;
        }

        dockingBays.put(++dockingBayNumber, dockingBay);
    }

    public void removeDockingBay(UUID id) {
        Optional<Map.Entry<Integer, DockingBay>> targetBay = dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .findFirst();

        targetBay.ifPresentOrElse(
                entry -> {
                    if (entry.getValue().isOccupied()) {
                        System.out.println("[HUB] Error: Cannot remove Bay " + entry.getKey() + ". A ship is currently docked!");
                    } else {
                        dockingBays.remove(entry.getKey());
                        System.out.println("[HUB] Successfully removed bay: " + entry.getValue().getName());
                    }
                },
                () -> System.out.println("[HUB] Error: Didn't find bay with ID ==>" + id.toString().substring(0, 8))
        );
    }

    public void registerShip(SpaceShip ship) {
        if (ship == null) {
            throw new IllegalArgumentException("[HUB] Error: Ship cannot be null");
        }
        if (registeredShips.contains(ship)) {
            System.out.printf("[HUB] Error: Ship '%s' already in history", ship.getName());
            return;
        }
        registeredShips.add(ship);
    }

    public void assignShipToBay(UUID id, int bayNumber) {
        DockingBay bay = dockingBays.get(bayNumber);
        if (bay == null) {
            System.out.println("[HUB] Error: Docking Bay " + bayNumber + " does not exist");
            return;
        }

        registeredShips.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .ifPresentOrElse(
                        ship -> {
                            if (bay.isOccupied()) {
                                System.out.println("[HUB] Error: Docking Bay " + bayNumber + " is already occupied by" + ship.getName());

                            } else if (ship.getShipSize().compareTo(bay.getBaySize()) <= 0) {
                                bay.dockSpaceShip(ship);
                                System.out.printf("[HUB] Success: '%s' parked in bay %d%n", ship.getName(), bayNumber);
                            } else {
                                System.out.printf("[HUB] Error: Ship '%s' is too large for bay %d%n", ship.getName(), bayNumber);
                            }
                        },
                        () -> System.out.println("[HUB] Error: No registered ship found with ID " + id.toString().substring(0, 8))
                );
    }

    public void unassignShipFromBay(UUID id) {
        if (dockingBays.isEmpty()) {
            System.out.println("[HUB] Error: No docking bays found.");
            return;
        }

        dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isOccupied() && entry.getValue().getSpaceShip().getId().equals(id))
                .findFirst()
                .ifPresentOrElse(
                        bayEntry -> bayEntry.getValue().undockSpaceShip(),
                        () -> System.out.println("[HUB] Error: Ship (" + id.toString().substring(0, 8) + ") does not exist")
                );
    }

    // Searches ship by UUID
    public void onboardCrewToShip(UUID id, CrewMember crew) {
        if (crew == null) {
            throw new IllegalArgumentException("[HUB] Error: Crew cannot be null");
        }

        dockingBays.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isOccupied() && entry.getValue().getSpaceShip().getId().equals(id))
                .findFirst()
                .ifPresentOrElse(entry ->
                                entry.getValue().getSpaceShip().addCrewMember(crew),
                        () -> System.out.println("[HUB] Error: Ship (" + id + ") not found")
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
        if (fromShipDock.isEmpty() || toShipDock.isEmpty()) {
            System.out.println("[HUB] Error: Could not locate both ships in the docking bays.");
            return;
        }

        SpaceShip sourceShip = fromShipDock.get().getValue().getSpaceShip();
        SpaceShip destinationShip = toShipDock.get().getValue().getSpaceShip();

        Optional<CrewMember> crewToMove = sourceShip.getCrewMembers().stream()
                .filter(crew -> crew.getId().equals(crewId))
                .findFirst();

        crewToMove.ifPresentOrElse(crew -> {
                    sourceShip.removeCrewMember(crewId);
                    destinationShip.addCrewMember(crew);
                    System.out.printf("[HUB] Success: Transferred %s from '%s' to '%s'.%n",
                            crew.getName(), sourceShip.getName(), destinationShip.getName());
                },
                () -> System.out.println("[HUB] Error: Crew ID (" + crewId.toString().substring(0, 8) + ") wasn't found on the source ship.")
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
            System.out.println("[HUB] Cannot find the ship in any of the currently occupied bays.");
            return false;
        }

        SpaceShip targetShip = targetBay.get().getValue().getSpaceShip();

        if (targetShip instanceof CargoShip) {
            Set<Map.Entry<CargoItem, Integer>> hazardousCargoManifest = ((CargoShip) targetShip).getCargoManifest().entrySet().stream()
                    .filter(cargoEntry -> cargoEntry.getKey() instanceof HazardousCargo)
                    .collect(Collectors.toSet());

            if (hazardousCargoManifest.isEmpty()) {
                System.out.println("[HUB] No hazardous materials detected.");
                return false;
            }

            System.out.println("[HUB] HAZARDOUS MATERIALS DETECTED. Printing safety report...");
            System.out.println(hazardousCargoManifest);
            return true;
        } else {
            System.out.println("[HUB] The selected ship isn't a cargo ship. Scan aborted.");
        }
        return false;
    }

    // I may want this to return a Set<CrewMember>. For now, it prints the personnelReport.
    public void generatePersonnelReport() {
        Set<CrewMember> personnelReport = new TreeSet<>();
        if (registeredShips.isEmpty()) {
            System.out.println("[HUB] No ships have been registered so far.");
            return;
        }

        for (SpaceShip ship : registeredShips) {
            personnelReport.addAll(ship.getCrewMembers());
        }

        System.out.println(personnelReport);
    }

    public double calculateDockingFeesPerShip(UUID shipId) {
        if (dockingBays.isEmpty()) {
            System.out.println("[HUB] No docking bays exist.");
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
            System.out.println("[HUB] Couldn't locate ship with id: " + shipId.toString().substring(0, 8));
            return -1.0;
        }
        SpaceShip dockedShip = ship.get();

        int fuelNeeded = 100 - dockedShip.getFuelLevel();
        int repairsNeeded = 100 - dockedShip.getHullIntegrity();

        double resourceCost = (fuelNeeded * FUEL_COST_PER_UNIT) + (repairsNeeded * REPAIR_COST_PER_UNIT);

        double baseFee = 0.0;
        double serviceMultiplier = 1.0; // Standard labor rate

        if (dockedShip instanceof CargoShip) {
            baseFee = 500.0; // Heavy-duty docking fee
            serviceMultiplier = 1.5; // Commercial surcharge for parts and labor
        } else if (dockedShip instanceof ScoutShip) {
            baseFee = 100.0; // Standard light docking fee
        }

        // Calculate final bill for this ship
        if (baseFee == 0) {
            System.out.println("[HUB] Error: Couldn't identify ship type.");
            return -2.0;
        }
        double shipTotalBill = baseFee + (resourceCost * serviceMultiplier);

        // Perform the maintenance (State Change)
        if (fuelNeeded > 0) dockedShip.setFuelLevel(100);
        if (repairsNeeded > 0) dockedShip.setHullIntegrity(100);

        // I may want to decouple the invoice logic from the calculation of the docking fee
        // Generate Invoice
        System.out.printf("[HUB-BILLING] Invoice for (%s)'%s' :%n", dockedShip.getName(), dockedShip.getClass().getSimpleName());
        System.out.printf("----> Base Fee: %.2f%n", baseFee);
        System.out.printf("----> Fuel Added: %d units | Repairs: %d units%n", fuelNeeded, repairsNeeded);
        System.out.printf("----> Total Charged: %.2f credits%n", shipTotalBill);

        return shipTotalBill;
    }


    public double calculateTotalDockingFees() {

        double totalRevenue = 0.0;
        for (DockingBay bay : dockingBays.values()) {
            if (bay.isOccupied()) {
                SpaceShip ship = bay.getSpaceShip();

                totalRevenue += calculateDockingFeesPerShip(ship.getId());
            }
        }

        System.out.printf("[HUB-BILLING] End of day report: Total Station Revenue = %.2f credits.%n", totalRevenue);
        return totalRevenue;
    }

    private double calculateCargoWeight(CargoShip ship) {
        if (ship == null) {
            System.out.println("CargoShip cannot be null.");
            return -1.0;
        }

        return ship.getCargoManifest().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getWeight() * entry.getValue())
                .sum();
    }

    // Uses a string filter. I haven't decided yet how the user will interact with this.
    // 'all time' to search through registeredShips; 'docked' to search through the currently docked ships
    public void findHeaviestCargoShip(String filter) {
        Optional<CargoShip> heaviestShip;

        if (filter.equalsIgnoreCase("all time")) {
            if (registeredShips.isEmpty()) {
                System.out.println("[HUB] No ships have been registered yet.");
                return;
            }

            heaviestShip = registeredShips.stream()
                    .filter(ship -> ship instanceof CargoShip)
                    .distinct()
                    .map(ship -> (CargoShip) ship)
                    .max(Comparator.comparingDouble(this::calculateCargoWeight));
        } else if (filter.equalsIgnoreCase("docked")) {
            if (dockingBays.isEmpty()) {
                System.out.println("[HUB] No docking bays exist.");
                return;
            }

            heaviestShip = dockingBays.values().stream()
                    .filter(DockingBay::isOccupied)
                    .map(DockingBay::getSpaceShip)
                    .filter(ship -> ship instanceof CargoShip)
                    .map(ship -> (CargoShip) ship)
                    .max(Comparator.comparingDouble(this::calculateCargoWeight));
        } else {
            System.out.println("[HUB] Error: Invalid filter. Use 'all time' or 'docked'.");
            return;
        }

        heaviestShip.ifPresentOrElse(
                ship -> System.out.printf("[HUB] The heaviest %s cargo ship is '%s' carrying %.2f Tonnes.%n",
                        filter.toLowerCase(), ship.getName(), calculateCargoWeight(ship)),
                () -> System.out.println("[HUB] No cargo ships found matching the '" + filter + "' filter.")
        );
    }

    public List<DockingBay> getBaysByStatus(boolean occupied) {
        List<DockingBay> filteredBays; // Unmodifiable by the toList() terminal operation

        if (occupied) {
            filteredBays = dockingBays.values()
                    .stream()
                    .filter(DockingBay::isOccupied)
                    .toList();

            if (filteredBays.isEmpty()) {
                System.out.println("[HUB] No docking bays are currently occupied.");
                return new ArrayList<>();
            }
        } else {
            filteredBays = dockingBays.values()
                    .stream()
                    .filter(dock -> !dock.isOccupied())
                    .toList();

            if (filteredBays.isEmpty()) {
                System.out.println("[HUB] All docking bays are full.");
                return new ArrayList<>();
            }
        }
        return filteredBays;
    }

    public void emergencyEvacuation() {
        if (dockingBays.isEmpty()) {
            System.out.println("[HUB] All docking bays are empty. There is no one to evacuate");
            return;
        }

        int totalEvacuated;

        totalEvacuated = dockingBays.values().stream()
                .filter(DockingBay::isOccupied)
                .mapToInt(bay -> bay.getSpaceShip().getCrewMembers().size())
                .sum();
        if (totalEvacuated == 0) {
            System.out.println("[HUB] Ships didn't have anyone on board. There is no one to evacuate");
            return;
        }

        dockingBays.values().forEach(DockingBay::undockSpaceShip);

        System.out.printf("[HUB] EMERGENCY OVERRIDE: Successfully evacuated %d personnel", totalEvacuated);
    }
}
