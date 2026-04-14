package com.octavian.galactic.repository;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.support.AbstractPostgresJpaIT;
import com.octavian.galactic.support.PostgresJpaTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "com.octavian.galactic.support.TestEnvironment#dockerAvailable",
        disabledReason = "Requires Docker for Testcontainers PostgreSQL")
class DockingBayRepositoryIT extends AbstractPostgresJpaIT {

    private final ShipRepository shipRepository = new ShipRepository(PostgresJpaTestSupport.emf());
    private final DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());

    @Test
    void findAllJoinFetchLoadsDockedShip() {
        ScoutShip ship = new ScoutShip.Builder("Docked", Size.SMALL)
                .sensorRange(10)
                .build();
        shipRepository.save(ship);

        DockingBay bay = new DockingBay("Bay A", Size.LARGE, false);
        bay.setBayNumber(1);
        bayRepository.save(bay);

        DockingBay managed = bayRepository.findById(bay.getId()).orElseThrow();
        managed.dockSpaceShip(ship);
        bayRepository.update(managed);

        List<DockingBay> bays = bayRepository.findAll();
        assertEquals(1, bays.size());
        DockingBay loaded = bays.getFirst();
        assertTrue(loaded.isOccupied());
        assertNotNull(loaded.getSpaceShip());
        assertEquals(ship.getId(), loaded.getSpaceShip().getId());
        assertEquals("Docked", loaded.getSpaceShip().getName());
    }
}
