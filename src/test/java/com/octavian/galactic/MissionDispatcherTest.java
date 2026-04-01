package com.octavian.galactic;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.mission.Mission;
import com.octavian.galactic.model.mission.MissionResult;
import com.octavian.galactic.model.mission.MissionType;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.FighterShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.service.MissionDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MissionDispatcherTest {

    private FighterShip fighter;
    private ScoutShip scout;
    private CargoShip cargo;

    @BeforeEach
    void setUp() {
        fighter = new FighterShip.Builder("X-Wing", Size.SMALL)
                .fuelLevel(100)
                .hullIntegrity(100)
                .ammunitionCount(50)
                .shieldStrength(100)
                .build();

        scout = new ScoutShip.Builder("Enterprise", Size.MEDIUM)
                .fuelLevel(100)
                .hullIntegrity(100)
                .sensorRange(350)
                .build();

        cargo = new CargoShip.Builder("Nostromo", Size.LARGE)
                .fuelLevel(100)
                .hullIntegrity(100)
                .maxCargoWeight(5000)
                .build();
    }

    @Test
    @DisplayName("Should successfully dispatch FighterShip on PATROL")
    void testFighterPatrolMission() {
        Mission patrol = new Mission("Border Patrol", MissionType.PATROL, 200, 1000.0);

        MissionResult result = MissionDispatcher.dispatch(fighter, patrol);

        assertNotNull(result);
        assertEquals(patrol, result.getMission());
        assertTrue(fighter.getFuelLevel() < 100, "Fuel should have been consumed");
    }

    @Test
    @DisplayName("Should successfully dispatch ScoutShip on EXPLORE")
    void testScoutExploreMission() {
        Mission explore = new Mission("Deep Space Recon", MissionType.EXPLORE, 300, 2000.0);

        MissionResult result = MissionDispatcher.dispatch(scout, explore);

        assertNotNull(result);
        assertEquals(explore, result.getMission());
        assertTrue(result.getFuelConsumed() > 0, "Mission should calculate fuel cost");
        // A medium ship traveling 300 distance uses (300/100) * 2 = 6 fuel
        assertEquals(94, scout.getFuelLevel());
    }

    @Test
    @DisplayName("Should penalize incorrect ship types assigned to missions")
    void testIncorrectShipTypePenalty() {
        // Assign a CargoShip to an EXPLORE mission
        Mission explore = new Mission("Asteroid Mapping", MissionType.EXPLORE, 100, 1000.0);

        MissionResult result = MissionDispatcher.dispatch(cargo, explore);

        assertFalse(result.isSuccess(), "Cargo ship should fail an Explore mission");
        assertTrue(result.getCreditsEarned() < 1000.0, "Credits earned should be heavily penalized");
        assertTrue(result.getHullDamageTaken() > 0, "Ship should take damage for doing wrong mission type");
    }

    @Test
    @DisplayName("Should throw IllegalStateException if ship has insufficient fuel")
    void testDispatchInsufficientFuel() {
        Mission longHaul = new Mission("Cross-Galaxy Delivery", MissionType.HAUL, 9000, 5000.0);

        // CargoShip is Large, multiplier is 3. 9000 distance = 90 * 3 = 270 fuel cost. Max fuel is 100.
        assertThrows(IllegalStateException.class,
                () -> MissionDispatcher.dispatch(cargo, longHaul),
                "Should throw exception if ship lacks fuel to make the jump");
    }

    @Test
    @DisplayName("Mission creation constraints check")
    void testMissionCreationValidation() {
        assertThrows(NullPointerException.class, () -> new Mission(null, MissionType.PATROL, 100, 500));
        assertThrows(IllegalArgumentException.class, () -> new Mission("Valid Name", MissionType.PATROL, -50, 500));
        assertThrows(IllegalArgumentException.class, () -> new Mission("Valid Name", MissionType.PATROL, 100, -500));
    }
}