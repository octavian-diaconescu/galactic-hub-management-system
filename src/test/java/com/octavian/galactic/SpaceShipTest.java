package com.octavian.galactic;

import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.station.CrewMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class SpaceShipTest {
    @Test
    @DisplayName("Should throw exception when fuel is set below 0")
    void testInvalidFuelLevel() {
        // Arrange: Create a ship for testing
        CargoShip ship = new CargoShip("Test Ship", 50, 100, 5, 500.0);

        // Act & Assert: Check if setting fuel to -10 throws the expected exception
        assertThrows(IllegalArgumentException.class, () -> ship.setFuelLevel(-10), "Setting fuel below 0 must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Should correctly add crew member within capacity")
    void testAddCrewMemberSuccess() {
        // Arrange
        CargoShip ship = new CargoShip("Alpha 1", 100, 100, 2, 1000.0);
        CrewMember bob = new CrewMember("Bob", CrewMember.Rank.COMMANDER);
        CrewMember michael = new CrewMember("Michael ", CrewMember.Rank.COMMANDER);

        // Act
        ship.addCrewMember(michael);
        ship.addCrewMember(bob);

        // Assert
        assertEquals(2, ship.getCrewMembers().size());
        assertTrue(ship.getCrewMembers().contains(bob));
        assertTrue(ship.getCrewMembers().contains(michael));

        System.out.println("Crew currently on board: " + ship.getCrewMembers());
    }

    @Test
    @DisplayName("Should say ship is full when adding a crew member over capacity")
    void testAddCrewMemberFail(){
        // Arrange
        CargoShip ship = new CargoShip("Venus", 57, 95, 1, 25.50);
        CrewMember NPC = new CrewMember("John Doe", CrewMember.Rank.CIVILIAN);
        CrewMember NPC2 = new CrewMember("Jason Statham", CrewMember.Rank.COMMANDER);

        // Act
        ship.addCrewMember(NPC);
        ship.addCrewMember(NPC2);

        // Assert
        assertEquals(1, ship.getCrewMembers().size());
    }

}
