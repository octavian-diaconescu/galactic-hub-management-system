package com.octavian.galactic.repository;

import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.support.AbstractPostgresJpaIT;
import com.octavian.galactic.support.PostgresJpaTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "com.octavian.galactic.support.TestEnvironment#dockerAvailable",
        disabledReason = "Requires Docker for Testcontainers PostgreSQL")
class CargoRepositoryIT extends AbstractPostgresJpaIT {

    private final CargoRepository repository = new CargoRepository(PostgresJpaTestSupport.emf());

    @Test
    void saveAndFindHazardousCargoSubtype() {
        HazardousCargo cargo = new HazardousCargo("Plasma core", 1.5, 9, "Shielded");
        repository.save(cargo);

        Optional<CargoItem> found = repository.findById(cargo.getId());
        assertTrue(found.isPresent());
        assertInstanceOf(HazardousCargo.class, found.get());
        HazardousCargo loaded = (HazardousCargo) found.get();
        assertEquals("Plasma core", loaded.getName());
        assertEquals(1.5, loaded.getWeight());
        assertTrue(loaded.toString().contains("radLvl: 9"));
    }
}
