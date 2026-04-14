package com.octavian.galactic.repository;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.ScoutShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.CrewMember;
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
}
