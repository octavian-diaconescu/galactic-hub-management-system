package com.octavian.galactic.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared PostgreSQL container and JPA {@link EntityManagerFactory} for integration tests.
 * Call {@link #ensureStarted()} before use (typically from {@link AbstractPostgresJpaIT}).
 */
public final class PostgresJpaTestSupport {

    private static final Object LOCK = new Object();
    private static PostgreSQLContainer<?> postgres;
    private static EntityManagerFactory emf;

    private PostgresJpaTestSupport() {
    }

    public static void ensureStarted() {
        if (emf != null) {
            return;
        }
        synchronized (LOCK) {
            if (emf != null) {
                return;
            }
            postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
            postgres.start();
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
            props.put("jakarta.persistence.jdbc.user", postgres.getUsername());
            props.put("jakarta.persistence.jdbc.password", postgres.getPassword());
            props.put("jakarta.persistence.schema-generation.database.action", "create");
            emf = Persistence.createEntityManagerFactory("com.octavian.galactic", props);
        }
    }

    public static EntityManagerFactory emf() {
        ensureStarted();
        return emf;
    }

    public static void truncateAll() {
        ensureStarted();
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("""
                    TRUNCATE TABLE cargo_manifest_entry, crew_member, docking_bay, scout_ship,
                        fighter_ship, cargo_ship, spaceship, hazardous_cargo, agricultural_cargo,
                        manufactured_cargo, raw_material_cargo, cargo_item RESTART IDENTITY CASCADE
                    """).executeUpdate();
            em.getTransaction().commit();
        }
    }
}
