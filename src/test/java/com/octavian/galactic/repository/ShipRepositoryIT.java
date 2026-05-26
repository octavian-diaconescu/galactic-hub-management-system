package com.octavian.galactic.repository;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.support.AbstractPostgresJpaIT;
import com.octavian.galactic.support.PostgresJpaTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "com.octavian.galactic.support.TestEnvironment#dockerAvailable",
        disabledReason = "Requires Docker for Testcontainers PostgreSQL")
class ShipRepositoryIT extends AbstractPostgresJpaIT {

    private final ShipRepository repository = new ShipRepository(PostgresJpaTestSupport.emf());

    @Test
    void saveAndFindScoutShipWithCrew() {
        ScoutShip ship = new ScoutShip.Builder("Probe", Size.SMALL)
                .fuelLevel(80)
                .hullIntegrity(90)
                .maxCrewCapacity(3)
                .sensorRange(100)
                .build();
        CrewMember cm = new CrewMember("Alex", CrewMember.Rank.OFFICER, CrewMember.Species.HUMAN);
        ship.addCrewMember(cm);

        repository.save(ship);

        Optional<SpaceShip> loaded = repository.findById(ship.getId());
        assertTrue(loaded.isPresent());
        assertInstanceOf(ScoutShip.class, loaded.get());
        ScoutShip scout = (ScoutShip) loaded.get();
        assertEquals("Probe", scout.getName());
        assertEquals(1, scout.getCrewMembers().size());
        assertTrue(scout.getCrewMembers().stream().anyMatch(c -> c.getName().equals("Alex")));
    }

    @Test
    void findAllReturnsCargoAndScoutSubtypes() {
        CargoShip cargo = new CargoShip.Builder("Hauler", Size.LARGE)
                .maxCargoWeight(1000)
                .build();
        ScoutShip scout = new ScoutShip.Builder("Vanguard", Size.MEDIUM)
                .sensorRange(50)
                .build();
        repository.save(cargo);
        repository.save(scout);

        List<SpaceShip> all = repository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(s -> s.getId().equals(cargo.getId()) && s instanceof CargoShip));
        assertTrue(all.stream().anyMatch(s -> s.getId().equals(scout.getId()) && s instanceof ScoutShip));
    }

    @Test
    void findDockedCargoShipsWithCargoLoadsManifestAfterDocking() {
        HazardousCargo item = new HazardousCargo("Slag", 3.0, 4, "Regular");
        CargoShip ship = new CargoShip.Builder("Docked Hauler", Size.MEDIUM)
                .maxCargoWeight(500)
                .build();
        ship.addCargoItem(item, 4);
        repository.save(ship);

        DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());
        DockingBay bay = new DockingBay("Pad", Size.LARGE, false);
        bay.setBayNumber(7);
        bayRepository.save(bay);
        DockingBay managed = bayRepository.findById(bay.getId()).orElseThrow();
        managed.dockSpaceShip(ship);
        bayRepository.update(managed);
        repository.update(ship);

        var docked = repository.findDockedCargoShipsWithCargo();
        assertEquals(1, docked.size());
        assertEquals(ship.getId(), docked.getFirst().getId());
        assertEquals(12.0, docked.getFirst().getCargoManifest().entrySet().stream()
                .mapToDouble(e -> e.getKey().getWeight() * e.getValue())
                .sum(), 0.01);
    }

    @Test
    void findDockedCargoShipsWithCargoExcludesShipWithStaleDockedFlagOnly() {
        CargoShip ghost = new CargoShip.Builder("Ghost", Size.SMALL)
                .maxCargoWeight(100)
                .build();
        ghost.addCargoItem(new HazardousCargo("Ballast", 50.0, 1, "Regular"), 1);
        repository.save(ghost);
        ghost.setDocked(true);
        repository.update(ghost);

        CargoShip berthed = new CargoShip.Builder("Berthed", Size.SMALL)
                .maxCargoWeight(100)
                .build();
        berthed.addCargoItem(new HazardousCargo("Ore", 2.0, 1, "Regular"), 2);
        repository.save(berthed);

        DockingBayRepository bayRepository = new DockingBayRepository(PostgresJpaTestSupport.emf());
        DockingBay bay = new DockingBay("Pad", Size.MEDIUM, false);
        bay.setBayNumber(8);
        bayRepository.save(bay);
        DockingBay managed = bayRepository.findById(bay.getId()).orElseThrow();
        managed.dockSpaceShip(berthed);
        bayRepository.update(managed);
        repository.update(berthed);

        var docked = repository.findDockedCargoShipsWithCargo();
        assertEquals(1, docked.size());
        assertEquals(berthed.getId(), docked.getFirst().getId());
    }
}
