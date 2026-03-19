package com.octavian.galactic.service;

import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;

import java.util.*;
import java.util.stream.Collectors;

public class HubService {
    private final List<SpaceShip> registeredShips = new ArrayList<>(); // Keep a record of every ship that has ever visited
    private final Map<Integer, DockingBay> dockingBays = new HashMap<>(); // Manage physical locations
    private final String name;

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
        dockingBays.put(dockingBays.size() + 1, dockingBay);
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

    //TODO: public void calculateTotalDockingFees()

    private double calculateCargoWeight(CargoShip ship) {
        return ship.getCargoManifest().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getWeight() * entry.getValue())
                .sum();
    }

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
}
