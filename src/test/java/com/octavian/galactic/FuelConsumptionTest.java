package com.octavian.galactic;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.spaceship.FighterShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class FuelConsumptionTest {
    private SpaceShip fullTankShip;
    private SpaceShip lowFuelShip;

    @BeforeEach
    void setup() {
        fullTankShip = new FighterShip.Builder("Full tank test subject", Size.MEDIUM)
                .fuelLevel(100)
                .build();
        lowFuelShip = new FighterShip.Builder("Low fuel test subject", Size.LARGE)
                .fuelLevel(10)
                .build();
    }

    @Test
    @DisplayName("Should successfully consume fuel from the ship")
    void shouldConsumeFuelFromShip() {
        boolean result = fullTankShip.travel(500);

        // A medium ship has a consumption multiplier of 2. Based on the formula (distance / 100) * multiplier
        // This ship should lose (500 / 100) * 2 = 10 units of fuel.
        assertTrue(result);
        assertEquals(100, fullTankShip.getFuelCapacity());
        assertEquals(90, fullTankShip.getFuelLevel());
    }

    @Test
    @DisplayName("Should fail travel because of lack of fuel")
    void shouldFailTravelInsufficientFuel(){
        boolean result = lowFuelShip.travel(500);

        // Large ship multiplier: 3
        assertFalse(result);
        assertEquals(10, lowFuelShip.getFuelLevel());
    }

}
