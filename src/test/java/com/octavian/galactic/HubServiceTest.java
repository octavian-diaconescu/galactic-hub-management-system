package com.octavian.galactic;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.*;
import com.octavian.galactic.model.station.*;
import com.octavian.galactic.service.HubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HubServiceTest {
    private HubService hubService;
    private HubService mainHub;
    private FuelDepot fuelDepot;

    private DockingBay dockingBay;
    private DockingBay dockingBay2;
    private DockingBay dockingBay3;

    private SpaceShip spaceShip;
    private SpaceShip cargoShip;
    private CargoShip cargoShip1;
    private ScoutShip scoutShip;

    private HazardousCargo hc;
    private HazardousCargo hazardousCargoWithDescription;
    private HazardousCargo hazardousCargoWithDescription2;

    private CrewMember guestCrewMember;
    private Set<CrewMember> scoutShipCrewMembers;
    private Set<CrewMember> cargoShipCrewMembers;

    @BeforeEach
    void setUp() {
        fuelDepot = new FuelDepot("Fuel Depot", 10000, 8000);
        mainHub = new HubService("MAIN HUB", fuelDepot);
        hubService = new HubService("HUB ALPHA", fuelDepot);

        dockingBay = new DockingBay("Earth-Dock-1", Size.LARGE, false);
        dockingBay2 = new DockingBay("Earth-Dock-2", Size.LARGE, false);
        dockingBay3 = new DockingBay("Earth-Dock-3", Size.LARGE, false);

        spaceShip = new ScoutShip.Builder("TARS", Size.MEDIUM)
                .fuelLevel(100)
                .hullIntegrity(100)
                .maxCrewCapacity(10)
                .sensorRange(200)
                .build();

        cargoShip = new CargoShip.Builder("RATS", Size.SMALL)
                .fuelLevel(50)
                .hullIntegrity(80)
                .maxCrewCapacity(3)
                .maxCargoWeight(5000)
                .build();
        cargoShip1 = new CargoShip.Builder("Alpha 1", Size.SMALL)
                .fuelLevel(88)
                .hullIntegrity(100)
                .maxCrewCapacity(20)
                .maxCargoWeight(25.75)
                .build();

        scoutShip = new ScoutShip.Builder("Alpha 1", Size.LARGE)
                .fuelLevel(90)
                .hullIntegrity(90)
                .maxCrewCapacity(5)
                .sensorRange(10)
                .build();

        guestCrewMember = new CrewMember("Michael Bay", CrewMember.Rank.GUEST, CrewMember.Species.KORVAX);
        scoutShipCrewMembers = Set.of(
                new CrewMember("John Doe", CrewMember.Rank.COMMANDER, CrewMember.Species.HUMAN),
                new CrewMember("Mark Twain", CrewMember.Rank.GUEST, CrewMember.Species.HUMAN),
                new CrewMember("John Cena", CrewMember.Rank.SPECIALIST, CrewMember.Species.VYKEEN),
                new CrewMember("Clark Kent", CrewMember.Rank.ENGINEER, CrewMember.Species.ATLAS)
        );
        cargoShipCrewMembers = Set.of(
                new CrewMember("John Doe", CrewMember.Rank.COMMANDER, CrewMember.Species.KORVAX),
                new CrewMember("Matthew Murdock", CrewMember.Rank.SPECIALIST, CrewMember.Species.ATLAS)
        );

        hc = new HazardousCargo("Radioactive milk", 2.5, 8, "Lead-lined");
        hazardousCargoWithDescription = new HazardousCargo("Antimatter jelly", 5, 0, "Regular", "A mysterious jelly extracted from unknown sources");
        hazardousCargoWithDescription2 = new HazardousCargo("Schrodinger's matter", 0, 0, "Paradoxical", "Is it solid? Is it liquid? Is it gas?");
    }

    @Test
    @DisplayName("Should successfully create a hub and call the implemented methods so far")
    void testCreateHubAndCallImplementationMethods() {
        // Act
        scoutShipCrewMembers.forEach(spaceShip::addCrewMember);
        cargoShipCrewMembers.forEach(cargoShip::addCrewMember);

        hubService.buildDockingBay(dockingBay);
        hubService.buildDockingBay(dockingBay); // Should print 'Docking bay already exists'
        hubService.buildDockingBay(dockingBay2);

        hubService.registerShip(spaceShip);
        hubService.registerShip(spaceShip); // Should print 'Ship already in history'
        hubService.registerShip(cargoShip);

        hubService.assignShipToBay(spaceShip.getId(), 1);
        hubService.assignShipToBay(spaceShip.getId(), 1); // Should print 'Docking bay is occupied'
        hubService.assignShipToBay(cargoShip.getId(), 2);

        hubService.onboardCrewToShip(spaceShip.getId(), guestCrewMember);
        hubService.transferCrewToShip(spaceShip.getId(), cargoShip.getId(), guestCrewMember.getId());

        hubService.unassignShipFromBay(cargoShip.getId());
        hubService.removeDockingBay(dockingBay2.getId());

        // Assert
        Set<CrewMember> personnelReport;
        personnelReport = hubService.generatePersonnelReport();
        if (personnelReport.isEmpty()) {
            System.out.println("[HUB] Ships have no crew on board.");
        } else {
            System.out.println("[HUB] Personnel report: " + personnelReport);
        }
        // Verify Ship Registry
        // Both ships are now successfully registered
        assertEquals(2, hubService.getRegisteredShips().size(), "Both TARS and RATS should be registered");
        assertTrue(hubService.getRegisteredShips().contains(spaceShip), "TARS should be in the registry");
        assertTrue(hubService.getRegisteredShips().contains(cargoShip), "RATS should be in the registry");

        // Verify Docking Bays
        // Bay 2 had RATS, but RATS was unassigned, making Bay 2 empty. Then Bay 2 was removed.
        assertEquals(1, hubService.getDockingBays().size(), "Only one docking bay should remain");
        assertTrue(hubService.getDockingBays().containsKey(1), "Bay 1 should still exist");
        assertFalse(hubService.getDockingBays().containsKey(2), "Bay 2 should have been successfully removed");

        // Verify Docking Status
        // TARS never left Bay 1
        DockingBay bay1 = hubService.getDockingBays().get(1);
        assertTrue(bay1.isOccupied(), "Bay 1 should be occupied");
        assertEquals(spaceShip, bay1.getSpaceShip(), "TARS should be parked in Bay 1");

        // Verify Crew Manifests (The Transfer!)
        // TARS started with 4, got Michael Bay (5), then transferred him away (Back to 4).
        assertEquals(4, spaceShip.getCrewMembers().size(), "TARS should be back to 4 crew members");
        assertFalse(spaceShip.getCrewMembers().contains(guestCrewMember), "Michael Bay should no longer be on TARS");

        // RATS started with 2, and successfully received Michael Bay (3).
        assertEquals(3, cargoShip.getCrewMembers().size(), "RATS should now have 3 members");
        assertTrue(cargoShip.getCrewMembers().contains(guestCrewMember), "Michael Bay should be on RATS");
    }

    @Test
    @DisplayName("Should scan for hazardous cargo on a docked cargo ship")
    void testScanCargoShipForHazardousCargo() {
        // Act
        cargoShip1.addCargoItem(hc, 1);
        cargoShip1.addCargoItem(hazardousCargoWithDescription, 1);
        cargoShip1.addCargoItem(hazardousCargoWithDescription2, 1);

        mainHub.buildDockingBay(dockingBay3);
        mainHub.registerShip(cargoShip1);
        mainHub.registerShip(scoutShip);
        mainHub.assignShipToBay(cargoShip1.getId(), 1);
        boolean shouldReturnTrue = mainHub.scanShipForHazards(cargoShip1.getId());

        mainHub.unassignShipFromBay(cargoShip1.getId());
        mainHub.assignShipToBay(scoutShip.getId(), 1);

        boolean shouldReturnFalse = mainHub.scanShipForHazards(scoutShip.getId());

        // Assert
        assertTrue(shouldReturnTrue);
        assertFalse(shouldReturnFalse);
    }

    @Test
    @DisplayName("Should correctly calculate billing for a CargoShip and restore its stats")
    void testCalculateDockingFeesPerShip_CargoShip() {
        // Arrange
        hubService.buildDockingBay(dockingBay);
        hubService.registerShip(cargoShip);
        hubService.assignShipToBay(cargoShip.getId(), 1);

        // Act
        double bill = hubService.calculateDockingFeesPerShip(cargoShip.getId());

        // Assert
        assertEquals(1137.5, bill, "Billing calculation for CargoShip is incorrect");
        assertEquals(100, cargoShip.getFuelLevel(), "Fuel should be refilled to 100");
        assertEquals(100, cargoShip.getHullIntegrity(), "Hull should be repaired to 100");
    }

    @Test
    @DisplayName("Should sum up total revenue for all docked ships")
    void testCalculateTotalDockingFees() {
        // Arrange
        hubService.buildDockingBay(dockingBay);
        hubService.buildDockingBay(dockingBay2);
        hubService.registerShip(cargoShip);
        hubService.registerShip(scoutShip);

        hubService.assignShipToBay(cargoShip.getId(), 1);
        hubService.assignShipToBay(scoutShip.getId(), 2);

        // Act
        double totalRevenue = hubService.calculateTotalDockingFees();

        // Assert
        assertEquals(1412.5, totalRevenue, "Total revenue aggregation is incorrect");
    }

    @Test
    @DisplayName("Should return the correct docking bays based on occupied status")
    void testGetBaysByStatus() {
        // Arrange
        // Build 3 bays, but only dock 2 ships.
        hubService.buildDockingBay(dockingBay);  // Bay 1
        hubService.buildDockingBay(dockingBay2); // Bay 2
        hubService.buildDockingBay(dockingBay3); // Bay 3

        hubService.registerShip(cargoShip);
        hubService.registerShip(scoutShip);

        hubService.assignShipToBay(cargoShip.getId(), 1);
        hubService.assignShipToBay(scoutShip.getId(), 2);

        // Act
        List<DockingBay> occupiedBays = hubService.getBaysByStatus(true);
        List<DockingBay> emptyBays = hubService.getBaysByStatus(false);

        // Assert
        assertEquals(2, occupiedBays.size(), "Should find 2 occupied bays");
        assertEquals(1, emptyBays.size(), "Should find 1 empty bay (Bay 3)");
    }

    @Test
    @DisplayName("Should undock all ships during an emergency evacuation")
    void testEmergencyEvacuation() {
        // Arrange
        cargoShip.addCrewMember(guestCrewMember);
        hubService.buildDockingBay(dockingBay);
        hubService.registerShip(cargoShip);
        hubService.assignShipToBay(cargoShip.getId(), 1);

        // Act
        hubService.emergencyEvacuation();

        // Assert
        List<DockingBay> occupiedBays = hubService.getBaysByStatus(true);
        assertEquals(0, occupiedBays.size(), "All bays should be empty after an evacuation");

        // Ensure ships are actually detached from the bays
        assertNull(hubService.getDockingBays().get(1).getSpaceShip(), "Bay 1 should not hold a ship reference");
    }

    @Test
    @DisplayName("Should find the heaviest CargoShip")
    void testFindHeaviestCargoShip() {
        // Arrange
        // Load up RATS so it is heavier than Alpha 1, then register both
        ((CargoShip) cargoShip).addCargoItem(hc, 5); // Add 12.5 tonnes to RATS
        cargoShip1.addCargoItem(hc, 1); // Add 2.5 tonnes to Alpha 1

        hubService.registerShip(cargoShip);
        hubService.registerShip(cargoShip1);

        // Act
        Optional<CargoShip> heaviest = hubService.findHeaviestCargoShip("all time");

        // Assert
        assertTrue(heaviest.isPresent());
        assertEquals(cargoShip, heaviest.get(), "RATS should be the heaviest ship");
    }

}
