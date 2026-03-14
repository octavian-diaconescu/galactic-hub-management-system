package com.octavian.galactic;

import com.octavian.galactic.model.spaceship.CargoShip;
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
        CargoShip ship = new CargoShip("Alpha", 100, 100, 2, 1000.0);
        com.octavian.galactic.model.station.CrewMember bob = new com.octavian.galactic.model.station.CrewMember("Bob");

        // Act
        ship.addCrewMember(bob);

        // Assert
        assertEquals(1, ship.getCrewMembers().size());
        assertTrue(ship.getCrewMembers().contains(bob));
    }
}
