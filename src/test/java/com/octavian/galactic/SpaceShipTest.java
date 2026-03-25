package com.octavian.galactic;

import com.octavian.galactic.exception.CrewCapacityExceededException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.station.CrewMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class SpaceShipTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException when fuel is set below 0")
    void testInvalidFuelLevel() {
        // Arrange
        CargoShip ship = new CargoShip("Test Ship", Size.MEDIUM, 100, 5, 10, 500.0);

        // Assert
        assertThrows(IllegalArgumentException.class,
                () -> ship.setFuelLevel(-10),
                "Setting fuel below 0 must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Should correctly add crew members within capacity")
    void testAddCrewMemberSuccess() {
        // Arrange
        CargoShip ship = new CargoShip("Alpha 1", Size.LARGE, 100, 2, 5, 1000.0);
        CrewMember bob     = new CrewMember("Bob",     CrewMember.Rank.COMMANDER, CrewMember.Species.HUMAN);
        CrewMember michael = new CrewMember("Michael", CrewMember.Rank.COMMANDER, CrewMember.Species.HUMAN);

        // Act
        ship.addCrewMember(michael);
        ship.addCrewMember(bob);

        // Assert
        assertEquals(2, ship.getCrewMembers().size());
        assertTrue(ship.getCrewMembers().contains(bob));
        assertTrue(ship.getCrewMembers().contains(michael));
    }

    @Test
    @DisplayName("Should throw CrewCapacityExceededException when adding a crew member over capacity")
    void testAddCrewMemberOverCapacity() {
        // Arrange: ship with maxCrewCapacity of 1
        CargoShip ship = new CargoShip("Venus", Size.SMALL, 57, 95, 1, 25.50);
        CrewMember NPC  = new CrewMember("John Doe",      CrewMember.Rank.CIVILIAN,  CrewMember.Species.VYKEEN);
        CrewMember NPC2 = new CrewMember("Jason Statham", CrewMember.Rank.COMMANDER, CrewMember.Species.VYKEEN);

        // Act
        ship.addCrewMember(NPC); // Fills the single slot

        // Assert: boarding a NPC2 member must throw
        assertThrows(CrewCapacityExceededException.class,
                () -> ship.addCrewMember(NPC2),
                "Adding crew beyond capacity must throw CrewCapacityExceededException");

        assertEquals(1, ship.getCrewMembers().size(), "Only the NPC crew member should be aboard");
        assertTrue(ship.getCrewMembers().contains(NPC));
        assertFalse(ship.getCrewMembers().contains(NPC2));
    }
}