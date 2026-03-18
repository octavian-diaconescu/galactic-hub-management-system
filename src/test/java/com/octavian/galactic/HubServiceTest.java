package com.octavian.galactic;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.service.HubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class HubServiceTest {

    @Test
    @DisplayName("Should successfully create a hub and call the implemented methods so far")
    void testCreateHubAndCallImplementationMethods() {
        // Arrange
        HubService hubService = new HubService("HUB ALPHA");

        DockingBay dockingBay = new DockingBay("Earth-Dock-1", Size.LARGE, false);
        DockingBay dockingBay2 = new DockingBay("Earth-Dock-2", Size.LARGE, false);

        SpaceShip spaceShip = new ScoutShip("TARS", Size.MEDIUM, 100, 100, 10, 200);
        SpaceShip cargoShip = new CargoShip("RATS", Size.SMALL, 100, 100, 3, 100.88);

        CrewMember guestCrewMember = new CrewMember("Michael Bay", CrewMember.Rank.GUEST);
        Set<CrewMember> scoutShipCrewMembers = Set.of(
                new CrewMember("John Doe", CrewMember.Rank.COMMANDER),
                new CrewMember("Mark Twain", CrewMember.Rank.GUEST),
                new CrewMember("John Cena", CrewMember.Rank.SPECIALIST),
                new CrewMember("Clark Kent", CrewMember.Rank.ENGINEER)
        );
        Set<CrewMember> cargoShipCrewMembers = Set.of(
                new CrewMember("John Doe", CrewMember.Rank.COMMANDER),
                new CrewMember("Matthew Murdock", CrewMember.Rank.SPECIALIST)
        );

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
}
