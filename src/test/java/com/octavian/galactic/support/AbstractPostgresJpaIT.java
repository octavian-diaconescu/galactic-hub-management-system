package com.octavian.galactic.support;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Ensures PostgreSQL is up and the schema is empty between integration tests.
 * Concrete subclasses should use {@code @EnabledIf("com.octavian.galactic.support.TestEnvironment#dockerAvailable")}
 * so they are skipped when Docker is unavailable.
 */
public abstract class AbstractPostgresJpaIT {

    @BeforeAll
    static void startPostgres() {
        PostgresJpaTestSupport.ensureStarted();
    }

    @BeforeEach
    void truncateDatabase() {
        PostgresJpaTestSupport.truncateAll();
    }
}
