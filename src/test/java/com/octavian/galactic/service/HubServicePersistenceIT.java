package com.octavian.galactic.service;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.FuelDepotRepository;
import com.octavian.galactic.repository.ShipRepository;
import com.octavian.galactic.support.AbstractPostgresJpaIT;
import com.octavian.galactic.support.PostgresJpaTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "com.octavian.galactic.support.TestEnvironment#dockerAvailable",
        disabledReason = "Requires Docker for Testcontainers PostgreSQL")
class HubServicePersistenceIT extends AbstractPostgresJpaIT {

    @Test
    void assignShipPersistsAndReloadsFromDatabase() {
        FuelDepot depot = new FuelDepot("Depot", 10_000, 8_000);
        ShipRepository shipRepository = new ShipRepository(PostgresJpaTestSupport.emf());
        DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());
        FuelDepotRepository fuelDepotRepository = new FuelDepotRepository(PostgresJpaTestSupport.emf());
        fuelDepotRepository.save(depot);

        ScoutShip ship = new ScoutShip.Builder("Reloader", Size.SMALL)
                .sensorRange(20)
                .build();
        shipRepository.save(ship);

        DockingBay bay = new DockingBay("Pad1", Size.LARGE, false);
        bay.setBayNumber(1);
        bayRepository.save(bay);

        HubService hub = new HubService("Station", depot, shipRepository, bayRepository, fuelDepotRepository);
        hub.assignShipToBay(ship.getId(), 1);

        HubService reloaded = new HubService("Station", depot, shipRepository, bayRepository, fuelDepotRepository);
        assertTrue(reloaded.getDockingBays().get(1).isOccupied());
        assertEquals(ship.getId(), reloaded.getDockingBays().get(1).getSpaceShip().getId());
        assertTrue(reloaded.getRegisteredShips().stream().anyMatch(s -> s.getId().equals(ship.getId())));
    }

    @Test
    void findHeaviestDockedCargoShipUsesPersistedManifestWeights() {
        FuelDepot depot = new FuelDepot("Depot", 10_000, 8_000);
        ShipRepository shipRepository = new ShipRepository(PostgresJpaTestSupport.emf());
        DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());
        FuelDepotRepository fuelDepotRepository = new FuelDepotRepository(PostgresJpaTestSupport.emf());
        fuelDepotRepository.save(depot);

        CargoShip heavy = new CargoShip.Builder("Heavy Hauler", Size.LARGE)
                .maxCargoWeight(10_000)
                .build();
        CargoShip light = new CargoShip.Builder("Light Runner", Size.SMALL)
                .maxCargoWeight(10_000)
                .build();
        heavy.addCargoItem(new HazardousCargo("Ore batch A", 2.5, 5, "Lead-lined"), 10);
        light.addCargoItem(new HazardousCargo("Ore batch B", 2.5, 5, "Lead-lined"), 1);
        shipRepository.save(heavy);
        shipRepository.save(light);

        DockingBay bay1 = new DockingBay("Pad A", Size.LARGE, false);
        bay1.setBayNumber(1);
        DockingBay bay2 = new DockingBay("Pad B", Size.LARGE, false);
        bay2.setBayNumber(2);
        bayRepository.save(bay1);
        bayRepository.save(bay2);

        HubService hub = new HubService("Station", depot, shipRepository, bayRepository, fuelDepotRepository);
        hub.assignShipToBay(heavy.getId(), 1);
        hub.assignShipToBay(light.getId(), 2);

        Optional<CargoShip> heaviest = hub.findHeaviestCargoShip("docked");
        assertTrue(heaviest.isPresent());
        assertEquals(heavy.getId(), heaviest.get().getId());

        double weight = heaviest.get().getCargoManifest().entrySet().stream()
                .mapToDouble(e -> e.getKey().getWeight() * e.getValue())
                .sum();
        assertEquals(25.0, weight, 0.01);

        HubService reloaded = new HubService("Station", depot, shipRepository, bayRepository, fuelDepotRepository);
        Optional<CargoShip> again = reloaded.findHeaviestCargoShip("docked");
        assertTrue(again.isPresent());
        assertEquals(heavy.getId(), again.get().getId());
    }

    @Test
    void findHeaviestDockedIgnoresStaleDockedFlagWhenNotInBay() {
        FuelDepot depot = new FuelDepot("Depot", 10_000, 8_000);
        ShipRepository shipRepository = new ShipRepository(PostgresJpaTestSupport.emf());
        DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());
        FuelDepotRepository fuelDepotRepository = new FuelDepotRepository(PostgresJpaTestSupport.emf());
        fuelDepotRepository.save(depot);

        CargoShip ghost = new CargoShip.Builder("Ghost Freighter", Size.LARGE)
                .maxCargoWeight(10_000)
                .build();
        ghost.addCargoItem(new HazardousCargo("Heavy crate", 62.0, 5, "Lead-lined"), 1);
        shipRepository.save(ghost);
        ghost.setDocked(true);
        shipRepository.update(ghost);

        CargoShip inBay = new CargoShip.Builder("Bay Winner", Size.LARGE)
                .maxCargoWeight(10_000)
                .build();
        inBay.addCargoItem(new HazardousCargo("Ore", 30.0, 5, "Lead-lined"), 6);
        shipRepository.save(inBay);

        DockingBay bay = new DockingBay("Pad", Size.LARGE, false);
        bay.setBayNumber(3);
        bayRepository.save(bay);

        HubService hub = new HubService("Station", depot, shipRepository, bayRepository, fuelDepotRepository);
        hub.assignShipToBay(inBay.getId(), 3);

        Optional<CargoShip> heaviest = hub.findHeaviestCargoShip("docked");
        assertTrue(heaviest.isPresent());
        assertEquals(inBay.getId(), heaviest.get().getId());
        assertEquals(180.0, heaviest.get().getCargoManifest().entrySet().stream()
                .mapToDouble(e -> e.getKey().getWeight() * e.getValue())
                .sum(), 0.01);
    }
}
