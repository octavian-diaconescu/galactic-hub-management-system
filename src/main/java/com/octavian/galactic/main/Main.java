package com.octavian.galactic.main;

import com.octavian.galactic.exception.InsufficientContainmentException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.service.HubService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final HubService hub = new HubService("Omega Station");
    private static final List<SpaceShip> knownShips = new ArrayList<>();

    public static void main(String[] args) {
        initializeStation();
        runMenu();
    }

    private static void initializeStation() {
        System.out.println("Initializing " + hub.getName() + "...");

        // Build Bays
        hub.buildDockingBay(new DockingBay("Aero-Dock-Small", Size.SMALL, false));
        hub.buildDockingBay(new DockingBay("Commercial-Medium", Size.MEDIUM, false));
        hub.buildDockingBay(new DockingBay("Heavy-Hauler-Large", Size.LARGE, false));

        // Create Ships (with missing fuel/hull for billing demonstration)
        CargoShip freighter = new CargoShip.Builder("USG Ishimura", Size.LARGE)
                .fuelLevel(40)
                .hullIntegrity(60)
                .maxCrewCapacity(10)
                .maxCargoWeight(50000)
                .build();
        freighter.addCrewMember(new CrewMember("Isaac Clarke", CrewMember.Rank.ENGINEER, CrewMember.Species.HUMAN));
        try {
            freighter.addCargoItem(new HazardousCargo("Marker Fragment", 50.0, 100, "Lead-lined", "Highly volatile alien artifact"), 1);
        } catch (InsufficientContainmentException i) {
            System.out.println(i.getMessage());
        }

        ScoutShip fighter = new ScoutShip.Builder("Swordfish II", Size.SMALL)
                .fuelLevel(80)
                .hullIntegrity(95)
                .maxCrewCapacity(2)
                .sensorRange(500)
                .build();
        fighter.addCrewMember(new CrewMember("Spike Spiegel", CrewMember.Rank.COMMANDER, CrewMember.Species.HUMAN));

        // Register Ships
        hub.registerShip(freighter);
        hub.registerShip(fighter);
        knownShips.add(freighter);
        knownShips.add(fighter);

        System.out.println("Initialization complete. Welcome to the Hub.\n");
    }

    private static void runMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=========================================");
            System.out.println("   " + hub.getName().toUpperCase() + " TERMINAL");
            System.out.println("=========================================");
            System.out.println("1. View Station Status (Bays)");
            System.out.println("2. Dock a Ship");
            System.out.println("3. Undock a Ship");
            System.out.println("4. Scan Docked Ships for Hazards");
            System.out.println("5. Generate Personnel Report");
            System.out.println("6. Run End-of-Day Billing");
            System.out.println("7. Trigger Emergency Evacuation");
            System.out.println("0. Exit Terminal");
            System.out.print("Select an option: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> viewStationStatus();
                case "2" -> handleDocking();
                case "3" -> handleUndocking();
                case "4" -> handleHazardScan();
                case "5" -> hub.generatePersonnelReport();
                case "6" -> hub.calculateTotalDockingFees();
                case "7" -> hub.emergencyEvacuation();
                case "0" -> {
                    System.out.println("Shutting down terminal. Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static void viewStationStatus() {
        System.out.println("\n--- DOCKING BAYS ---");
        hub.getDockingBays().forEach((number, bay) -> {
            String status = bay.isOccupied() ? "OCCUPIED by " + bay.getSpaceShip().getName() : "EMPTY";
            System.out.printf("Bay %d: %s [%s] - %s%n", number, bay.getName(), bay.getBaySize(), status);
        });
    }

    private static void handleDocking() {
        System.out.println("\n--- INCOMING SHIPS ---");
        for (int i = 0; i < knownShips.size(); i++) {
            System.out.printf("%d. %s [%s]%n", i + 1, knownShips.get(i).getName(), knownShips.get(i).getShipSize());
        }
        System.out.print("Select a ship to dock (enter number): ");
        int shipChoice = Integer.parseInt(scanner.nextLine()) - 1;

        System.out.print("Select a bay number to dock in (1-3): ");
        int bayChoice = Integer.parseInt(scanner.nextLine());

        if (shipChoice >= 0 && shipChoice < knownShips.size()) {
            SpaceShip selectedShip = knownShips.get(shipChoice);
            hub.assignShipToBay(selectedShip.getId(), bayChoice);
        } else {
            System.out.println("Invalid ship selection.");
        }
    }

    private static void handleUndocking() {
        System.out.print("Enter the Bay Number to clear (1-3): ");
        try {
            int bayChoice = Integer.parseInt(scanner.nextLine());
            DockingBay bay = hub.getDockingBays().get(bayChoice);

            if (bay != null && bay.isOccupied()) {
                hub.unassignShipFromBay(bay.getSpaceShip().getId());
            } else {
                System.out.println("That bay is already empty or does not exist.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void handleHazardScan() {
        System.out.println("\n--- HAZARD SCANNER ---");
        for (int i = 0; i < knownShips.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, knownShips.get(i).getName());
        }
        System.out.print("Select a ship to scan (enter number): ");
        try {
            int shipChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (shipChoice >= 0 && shipChoice < knownShips.size()) {
                hub.scanShipForHazards(knownShips.get(shipChoice).getId());
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }
}