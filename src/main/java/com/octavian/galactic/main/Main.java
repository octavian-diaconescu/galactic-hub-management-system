package com.octavian.galactic.main;

import com.octavian.galactic.exception.InsufficientContainmentException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.*;
import com.octavian.galactic.model.station.*;
import com.octavian.galactic.service.*;
import com.octavian.galactic.model.mission.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final FuelDepot fuelDepot = new FuelDepot("Omega F-Depot", 10000, 8000);
    private static final HubService hub = new HubService("Omega Station", fuelDepot);
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
            System.out.println("8. Dispatch Ship on Mission");
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
                case "8" -> handleMissionDispatch();
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
        try {
            int shipChoice = Integer.parseInt(scanner.nextLine()) - 1;

            System.out.print("Select a bay number to dock in: ");
            int bayChoice = Integer.parseInt(scanner.nextLine());

            if (shipChoice >= 0 && shipChoice < knownShips.size()) {
                SpaceShip selectedShip = knownShips.get(shipChoice);
                hub.assignShipToBay(selectedShip.getId(), bayChoice);
            } else {
                System.out.println("Invalid ship selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleUndocking() {
        System.out.print("Enter the Bay Number to clear: ");
        try {
            int bayChoice = Integer.parseInt(scanner.nextLine());
            DockingBay bay = hub.getDockingBays().get(bayChoice);

            if (bay != null && bay.isOccupied()) {
                hub.unassignShipFromBay(bay.getSpaceShip().getId());
            } else {
                System.out.println("That bay is already empty or does not exist.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleHazardScan() {
        System.out.println("\n--- HAZARD SCANNER ---");
        System.out.println("Docked ships: ");

        boolean isOccupied = true;
        List<DockingBay> occupiedBays = hub.getBaysByStatus(isOccupied);
        List<SpaceShip> dockedShips = new ArrayList<>();
        if(occupiedBays.isEmpty()){
            return;
        }

        occupiedBays.forEach(bay -> dockedShips.add(bay.getSpaceShip()));

        System.out.println(dockedShips);

        for (SpaceShip dockedShip : dockedShips) {
            hub.scanShipForHazards(dockedShip.getId());
        }
    }

    private static void handleMissionDispatch() {
        System.out.println("\n--- MISSION BOARD ---");
        for (int i = 0; i < knownShips.size(); i++) {
            System.out.printf("[%s] %d. %s (Fuel: %d%%)%n", knownShips.get(i).getClass().getSimpleName(), i + 1, knownShips.get(i).getName(), knownShips.get(i).getFuelLevel());
        }
        System.out.print("Select a ship to dispatch (enter number): ");
        try {
            int shipChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (shipChoice < 0 || shipChoice >= knownShips.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            SpaceShip selectedShip = knownShips.get(shipChoice);

            System.out.println("Available Mission Types: 1. PATROL  2. EXPLORE  3. HAUL");
            System.out.print("Select mission type: ");
            int typeChoice = Integer.parseInt(scanner.nextLine());

            MissionType type = switch (typeChoice) {
                case 1 -> MissionType.PATROL;
                case 2 -> MissionType.EXPLORE;
                case 3 -> MissionType.HAUL;
                default -> throw new IllegalArgumentException("Invalid mission type");
            };

            System.out.print("Enter mission distance (e.g., 500): ");
            int distance = Integer.parseInt(scanner.nextLine());

            Mission mission = new Mission("Sector " + (int) (Math.random() * 100) + " Operation", type, distance, 1500.0);
            MissionDispatcher.dispatch(selectedShip, mission);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Mission Aborted: " + e.getMessage());
        }
    }
}