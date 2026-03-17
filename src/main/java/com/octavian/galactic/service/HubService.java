package com.octavian.galactic.service;

import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.DockingBay;

import java.util.*;

public class HubService {
    private final List<SpaceShip> registeredShips = new ArrayList<>(); // Keep a record of every ship that has ever visited;
    private final Map<Integer, DockingBay> dockingBays = new HashMap<>(); // Manage physical locations
    static int count = 0; // TODO: find a better way to do this

    public void buildDockingBay(DockingBay dockingBay) {
        if (dockingBay == null) {
            throw new IllegalArgumentException("Docking bay cannot be null");
        }
        if (dockingBays.containsValue(dockingBay)) {
            System.out.println("Docking bay '" + dockingBay.getName() + "' already exists.");
            return;
        }
        dockingBays.put(count, dockingBay);
        count += 1;
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
            throw new IllegalArgumentException("[HUB] Ship cannot be null");
        }
        if (registeredShips.contains(ship)) {
            System.out.println("[HUB] Ship already in history");
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
                                System.out.println("[HUB] Error: Docking Bay " + bayNumber + " is already occupied");

                            } else if (ship.getShipSize().compareTo(bay.getBaySize()) <= 0) {
                                bay.setSpaceShip(ship);
                                bay.setOccupied(true);
                                System.out.printf("[HUB] Success: '%s' parked in bay %d", ship.getName(), bayNumber);
                            } else {
                                System.out.printf("[HUB]: Error: Ship '%s' is too large for bay %d", ship.getName(), bayNumber);
                            }
                        },
                        () -> System.out.println("[HUB] Error: No registered ship found with ID " + id)
                );
    }

}
