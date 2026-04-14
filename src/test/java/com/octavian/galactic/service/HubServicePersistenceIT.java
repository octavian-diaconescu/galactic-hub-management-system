package com.octavian.galactic.service;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.ShipRepository;
import com.octavian.galactic.support.AbstractPostgresJpaIT;
import com.octavian.galactic.support.PostgresJpaTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "com.octavian.galactic.support.TestEnvironment#dockerAvailable",
        disabledReason = "Requires Docker for Testcontainers PostgreSQL")
class HubServicePersistenceIT extends AbstractPostgresJpaIT {

    @Test
    void assignShipPersistsAndReloadsFromDatabase() {
        FuelDepot depot = new FuelDepot("Depot", 10_000, 8_000);
        ShipRepository shipRepository = new ShipRepository(PostgresJpaTestSupport.emf());
        DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());

        ScoutShip ship = new ScoutShip.Builder("Reloader", Size.SMALL)
                .sensorRange(20)
                .build();
        shipRepository.save(ship);

        DockingBay bay = new DockingBay("Pad1", Size.LARGE, false);
        bay.setBayNumber(1);
        bayRepository.save(bay);

        HubService hub = new HubService("Station", depot, shipRepository, bayRepository);
        hub.assignShipToBay(ship.getId(), 1);

        HubService reloaded = new HubService("Station", depot, shipRepository, bayRepository);
        assertTrue(reloaded.getDockingBays().get(1).isOccupied());
        assertEquals(ship.getId(), reloaded.getDockingBays().get(1).getSpaceShip().getId());
        assertTrue(reloaded.getRegisteredShips().stream().anyMatch(s -> s.getId().equals(ship.getId())));
    }
}
