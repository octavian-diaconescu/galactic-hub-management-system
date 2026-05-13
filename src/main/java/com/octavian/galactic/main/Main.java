package com.octavian.galactic.main;

import com.octavian.galactic.exception.InsufficientContainmentException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.mission.Mission;
import com.octavian.galactic.model.mission.MissionType;
import com.octavian.galactic.model.spaceship.*;
import com.octavian.galactic.model.station.*;
import com.octavian.galactic.repository.CargoRepository;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.FuelDepotRepository;
import com.octavian.galactic.repository.ShipRepository;
import com.octavian.galactic.service.AuditService;
import com.octavian.galactic.service.HubService;
import com.octavian.galactic.service.MissionDispatcher;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static FuelDepot fuelDepot;
    private static HubService hub;
    private static List<DockingBay> knownBays = new ArrayList<>();

    private static final EntityManagerFactory emf = createEntityManagerFactory();
    private static final DockingBayRepository dockingRepository = new DockingBayRepository(emf);
    private static final ShipRepository shipRepository = new ShipRepository(emf);
    private static final CargoRepository cargoRepository = new CargoRepository(emf);
    private static final FuelDepotRepository fuelDepotRepository = new FuelDepotRepository(emf);

    private static final String PERSISTENCE_UNIT = "com.octavian.galactic";

    public static void main(@SuppressWarnings("unused") String[] args) {
        try {
            fuelDepot = fuelDepotRepository.findAll().stream().findFirst()
                    .orElseGet(() -> {
                        FuelDepot seeded = new FuelDepot("Omega F-Depot", 10000, 8000);
                        fuelDepotRepository.save(seeded);
                        logger.info("Seeded fuel depot '{}' with {}/{} units",
                                seeded.getName(), seeded.getFuelLevel(), seeded.getFuelCapacity());
                        return seeded;
                    });

            hub = new HubService("Omega Station", fuelDepot, shipRepository, dockingRepository, fuelDepotRepository);
            knownBays = dockingRepository.findAll();

            initializeStation();
            runMenu();
        } finally {
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }

    private static Dotenv loadDotenv() {
        return Dotenv.configure()
                .directory("./")
                .filename(".env.persistence")
                .load();
    }

    private static EntityManagerFactory createEntityManagerFactory() {
        Dotenv dotenv = loadDotenv();

        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.user", required(dotenv, "GALACTIC_DB_USER"));
        overrides.put("jakarta.persistence.jdbc.password", required(dotenv, "GALACTIC_DB_PASSWORD"));

        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, overrides);
    }

    private static String required(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing or empty .env entry: " + key);
        }
        return value;
    }

    private static void initializeStation() {
        logger.info("Initializing {}...", hub.getName());
        System.out.println("Initializing " + hub.getName() + "...");

        // Build Bays
        if (knownBays.isEmpty()) {
            logger.info("No existing data found. Seeding initial station...");
            System.out.println("No existing data found. Seeding initial station...");
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
                CargoItem cargo = new HazardousCargo("Marker Fragment", 50.0, 100, "Lead-lined", "Highly volatile alien artifact");
                freighter.addCargoItem(cargo, 1);

                cargoRepository.save(cargo);
            } catch (InsufficientContainmentException i) {
                logger.error("[INIT] Containment check failed: {}", i.getMessage());
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
        } else {
            logger.info("Loaded {} docking bays from database.", knownBays.size());
            System.out.println("Loaded " + knownBays.size() + " docking bays from database.");
            int shipCount = shipRepository.findAll().size();
            logger.info("Loaded {} ships from database.", shipCount);
            System.out.println("Loaded " + shipCount + " ships from database.");
        }

        logger.info("Initialization complete.");
        System.out.println("Initialization complete. Welcome to the Hub.\n");
    }

    private static void runMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=========================================");
            System.out.println("   " + hub.getName().toUpperCase() + " TERMINAL");
            System.out.println("=========================================");
            System.out.println(" 1. View Station Status (Bays)");
            System.out.println(" 2. View Docked Ship Stats");
            System.out.println(" 3. Dock a Ship");
            System.out.println(" 4. Undock a Ship");
            System.out.println(" 5. Scan Docked Ships for Hazards");
            System.out.println(" 6. Find Heaviest Cargo Ship");
            System.out.println(" 7. Generate Docked Personnel Report");
            System.out.println(" 8. Run End-of-Day Billing");
            System.out.println(" 9. Dispatch Ship on Mission");
            System.out.println("10. Trigger Emergency Evacuation");
            System.out.println("11. Manage fuel depot");
            System.out.println(" 0. Exit Terminal");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().strip();

            switch (input) {
                case "1" -> {viewStationStatus();waitForEnter();}
                case "2" -> {viewDockedShipStats();waitForEnter();}
                case "3" -> {handleDocking();waitForEnter();}
                case "4" -> {handleUndocking();waitForEnter();}
                case "5" -> {handleHazardScan();waitForEnter();}
                case "6" -> {handleFindHeaviestCargo();waitForEnter();}
                case "7" -> {handlePersonnelReport();waitForEnter();}
                case "8" -> {hub.calculateTotalDockingFees();waitForEnter();}
                case "9" -> {handleMissionDispatch();waitForEnter();}
                case "10" -> {hub.emergencyEvacuation();waitForEnter();}
                case "11" -> {manageFuelDepot();waitForEnter();}
                case "0" -> {
                    logger.info("Terminal shutdown requested.");
                    System.out.println("Shutting down terminal. Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void viewStationStatus() {
        System.out.println("\n--- DOCKING BAYS ---");
        hub.getDockingBays().forEach((number, bay) -> {
            String status = bay.isOccupied() ? "OCCUPIED by " + bay.getSpaceShip().getName() : "EMPTY";
            System.out.printf("Bay %d: %s [%s] - %s%n", number, bay.getName(), bay.getBaySize(), status);
        });
    }

    private static void handleDocking() {
        List<SpaceShip> ships = shipRepository.findUndockedShips();

        System.out.println("\n--- INCOMING SHIPS ---");
        if (ships.isEmpty()) {
            System.out.println("No ships are waiting to dock.");
            return;
        }
        for (int i = 0; i < ships.size(); i++) {
            System.out.printf("%d. %s [%s]%n", i + 1, ships.get(i).getName(), ships.get(i).getShipSize());
        }

        System.out.println("\n--- AVAILABLE BAYS ---");
        List<Map.Entry<Integer, DockingBay>> emptyBays = hub.getDockingBays().entrySet().stream()
                .filter(e -> !e.getValue().isOccupied())
                .toList();
        if (emptyBays.isEmpty()) {
            System.out.println("No empty bays available.");
            return;
        }
        emptyBays.forEach(e ->
                System.out.printf("Bay %d: %s [%s]%n", e.getKey(), e.getValue().getName(), e.getValue().getBaySize()));

        System.out.print("\nSelect a ship to dock (enter number): ");
        try {
            int shipChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (shipChoice < 0 || shipChoice >= ships.size()) {
                System.out.println("Invalid ship selection.");
                return;
            }

            System.out.print("Select a bay number to dock in: ");
            int bayChoice = Integer.parseInt(scanner.nextLine());

            SpaceShip selectedShip = ships.get(shipChoice);
            hub.assignShipToBay(selectedShip.getId(), bayChoice);
        } catch (NumberFormatException e) {
            logger.warn("[MENU] Invalid numeric input during docking");
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (Exception e) {
            logger.error("[MENU] Docking error: {}", e.getMessage());
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
            logger.warn("[MENU] Invalid numeric input during undocking");
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            logger.error("[MENU] Undocking error: {}", e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleHazardScan() {
        System.out.println("\n--- HAZARD SCANNER ---");
        System.out.println("Docked ships: ");

        boolean isOccupied = true;
        List<DockingBay> occupiedBays = hub.getBaysByStatus(isOccupied);
        List<SpaceShip> dockedShips = new ArrayList<>();
        if (occupiedBays.isEmpty()) {
            return;
        }

        occupiedBays.forEach(bay -> dockedShips.add(bay.getSpaceShip()));

        System.out.println(dockedShips);

        for (SpaceShip dockedShip : dockedShips) {
            hub.scanShipForHazards(dockedShip.getId());
        }
    }

    private static void handleMissionDispatch() {
        List<DockingBay> occupiedBays = hub.getBaysByStatus(true);
        if (occupiedBays.isEmpty()) {
            System.out.println("No ships are currently docked to dispatch.");
            return;
        }

        List<SpaceShip> dockedShips = occupiedBays.stream()
                .map(DockingBay::getSpaceShip)
                .toList();

        System.out.println("\n--- MISSION BOARD ---");
        for (int i = 0; i < dockedShips.size(); i++) {
            SpaceShip s = dockedShips.get(i);
            System.out.printf("[%s] %d. %s (Fuel: %d%%, Hull: %d%%)%n",
                    s.getClass().getSimpleName(), i + 1, s.getName(), s.getFuelLevel(), s.getHullIntegrity());
        }
        System.out.print("Select a ship to dispatch (enter number): ");
        try {
            int shipChoice = Integer.parseInt(scanner.nextLine()) - 1;
            if (shipChoice < 0 || shipChoice >= dockedShips.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            SpaceShip selectedShip = dockedShips.get(shipChoice);

            System.out.println("Available Mission Types: 1. PATROL  2. EXPLORE  3. HAUL");
            System.out.print("Select mission type: ");
            int typeChoice = Integer.parseInt(scanner.nextLine());

            MissionType type = switch (typeChoice) {
                case 1 -> MissionType.PATROL;
                case 2 -> MissionType.EXPLORE;
                case 3 -> MissionType.HAUL;
                default -> throw new IllegalArgumentException("Invalid mission type");
            };

            if (type == MissionType.HAUL) {
                selectedShip = shipRepository.findByIdWithCargo(selectedShip.getId())
                        .orElse(selectedShip);
            }

            System.out.print("Enter mission distance (e.g., 500): ");
            int distance = Integer.parseInt(scanner.nextLine());

            Mission mission = new Mission("Sector " + (int) (Math.random() * 100) + " Operation", type, distance, 1500.0);
            MissionDispatcher.dispatch(selectedShip, mission);
            shipRepository.update(selectedShip);
            hub.unassignShipFromBay(selectedShip.getId());

        } catch (NumberFormatException e) {
            logger.warn("[MENU] Invalid numeric input during mission dispatch");
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("[MENU] Mission aborted: {}", e.getMessage());
            System.out.println("Mission Aborted: " + e.getMessage());
        }
    }

    /**
     * Prints all personnel from REGISTERED ships, not currently docked ones
     */
    private static void handlePersonnelReport() {
        Set<CrewMember> personnel = hub.generatePersonnelReport();

        if (personnel.isEmpty()) {
            System.out.println("[HUB] Ships have no crew on board.");
            return;
        }

        System.out.println("\n--- PERSONNEL REPORT ---");
        for (CrewMember crew : personnel) {
            System.out.printf("  - %s [%s]%n", crew, crew.getSpecies());
        }
        System.out.printf("Total personnel on station: %d%n", personnel.size());
    }

    private static void viewDockedShipStats() {
        List<DockingBay> occupiedBays = hub.getBaysByStatus(true);
        if (occupiedBays.isEmpty()) {
            System.out.println("No ships are currently docked.");
            return;
        }

        System.out.println("\n--- DOCKED SHIP STATS ---");
        for (DockingBay bay : occupiedBays) {
            SpaceShip ship = bay.getSpaceShip();
            System.out.println("-----------------------------------------");
            System.out.printf("Bay %d: %s%n", bay.getBayNumber(), bay.getName());
            System.out.printf("  Ship:      %s (%s)%n", ship.getName(), ship.getClass().getSimpleName());
            System.out.printf("  Size:      %s%n", ship.getShipSize());
            System.out.printf("  Fuel:      %d%%%n", ship.getFuelLevel());
            System.out.printf("  Hull:      %d%%%n", ship.getHullIntegrity());
            System.out.printf("  Crew (%d): %s%n", ship.getCrewMembers().size(), ship.getCrewMembers());

            if (ship instanceof CargoShip) {
                CargoShip cargoShip = shipRepository.findByIdWithCargo(ship.getId())
                        .filter(CargoShip.class::isInstance)
                        .map(CargoShip.class::cast)
                        .orElse((CargoShip) ship);
                Map<CargoItem, Integer> manifest = cargoShip.getCargoManifest();
                if (manifest.isEmpty()) {
                    System.out.println("  Cargo:     [empty]");
                } else {
                    System.out.println("  Cargo:");
                    manifest.forEach((item, qty) ->
                            System.out.printf("    - %s x%d (%.1f kg each)%n", item.getName(), qty, item.getWeight()));
                }
            }
        }
        System.out.println("-----------------------------------------");
    }

    private static void handleFindHeaviestCargo() {
        System.out.println("\n--- HEAVIEST CARGO SHIP ---");
        System.out.println("Search scope: 1. Docked  2. All Time");
        System.out.print("Select: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            String filter = switch (choice) {
                case 1 -> "docked";
                case 2 -> "all time";
                default -> throw new IllegalArgumentException("Invalid filter choice");
            };
            hub.findHeaviestCargoShip(filter);
        } catch (NumberFormatException e) {
            logger.warn("[MENU] Invalid numeric input for heaviest cargo search");
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (IllegalArgumentException e) {
            logger.error("[MENU] Heaviest cargo search error: {}", e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void manageFuelDepot() {
        try {
            fuelDepot = fuelDepotRepository.findAll().getFirst();
        } catch (NoSuchElementException e) {
            logger.warn("[MENU] Fuel depot not found");
        }

        System.out.println("\n--- MANAGE FUEL DEPOT ---");
        System.out.println("Fuel depot: " + fuelDepot.getName());

        Pair<Integer, Integer> fuelDepotInfo = Pair.with(-1, -1);
        try {
            fuelDepotInfo = hub.fuelDepotStats(fuelDepot);
        } catch (IllegalStateException e) {
            logger.error("[MENU] Fuel depot not found");
        }
        double fuelLevel = (double)fuelDepotInfo.getValue0();
        double fuelCapacity = (double)fuelDepotInfo.getValue1();

        logger.info("Fuel depot: {}. Fuel: {} out of {} ({}% left)", fuelDepot.getName(), (int)fuelLevel, (int)fuelCapacity, (fuelLevel / fuelCapacity) * 100);
        System.out.printf("Fuel: %d out of %d (%.2f%% left)%n", (int)fuelLevel, (int)fuelCapacity, (fuelLevel / fuelCapacity) * 100);
        System.out.println("Refuel? [Y/N]");

        try {
            String choice = String.join(" ", scanner.nextLine().split(" ")).trim().toLowerCase();
            switch (choice) {
                case "y" -> {
                    fuelDepot.refuel((int)fuelCapacity);
                    fuelDepotRepository.update(fuelDepot);
                    logger.info("Fuel depot: {} refueled", fuelDepot.getName());
                    AuditService.getInstance().log(AuditService.Action.FUEL_DEPOT_REFUELED, fuelDepot.getName());
                }
                case "n" -> {
                }
                default -> throw new IllegalArgumentException("Invalid choice");
            }
        } catch (NumberFormatException e) {
            logger.warn("[MENU] Invalid string input for refuel option");
        } catch (IllegalArgumentException e) {
            logger.info("[MENU] Invalid choice: {}", e.getMessage());
        }


    }
}