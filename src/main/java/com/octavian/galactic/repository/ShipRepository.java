package com.octavian.galactic.repository;

import com.octavian.galactic.model.spaceship.SpaceShip;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShipRepository implements BaseRepository<SpaceShip> {
    private final EntityManagerFactory emf;

    public ShipRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<SpaceShip> findById(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM SpaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "WHERE s.id = :id",
                            SpaceShip.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Optional<SpaceShip> findByIdWithCargo(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM SpaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "LEFT JOIN FETCH TREAT(s AS CargoShip).cargoManifestLine cme " +
                                    "LEFT JOIN FETCH cme.cargoItem " +
                                    "WHERE s.id = :id",
                            SpaceShip.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        }
    }

    @Override
    public List<SpaceShip> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM SpaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers",
                            SpaceShip.class)
                    .getResultList();
        }
    }

    public List<SpaceShip> findAllWithCargo() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM SpaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "LEFT JOIN FETCH TREAT(s AS CargoShip).cargoManifestLine cme " +
                                    "LEFT JOIN FETCH cme.cargoItem",
                            SpaceShip.class)
                    .getResultList();
        }
    }

    @Override
    public void save(SpaceShip entity) {
        emf.runInTransaction(em ->
                em.persist(entity)
        );
    }

    @Override
    public void update(SpaceShip entity) {
        emf.runInTransaction(em ->
                em.merge(entity)
        );
    }


    @Override
    public void delete(SpaceShip entity) {
        emf.runInTransaction(em ->
                em.remove(em.contains(entity) ? entity : em.merge(entity))
        );
    }

    @Override
    public void deleteById(UUID id) {
        emf.runInTransaction(em -> {
            SpaceShip ship = em.find(SpaceShip.class, id);
            if (ship != null)
                em.remove(ship);
        });
    }
}
