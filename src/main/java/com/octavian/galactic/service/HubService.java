package com.octavian.galactic.service;

import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;

import java.util.*;

public class HubService {
    private final List<SpaceShip> registeredShips = new ArrayList<>(); // Keep a record of every ship that has ever visited;
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
                        () -> System.out.println("[HUB] Error: No registered ship found with ID " + id)
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
                        () -> System.out.println("[HUB] Error: Ship (" + id + ") does not exist")
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
}
