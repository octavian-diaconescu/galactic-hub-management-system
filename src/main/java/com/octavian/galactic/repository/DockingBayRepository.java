package com.octavian.galactic.repository;

import com.octavian.galactic.model.station.DockingBay;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DockingBayRepository implements BaseRepository<DockingBay> {
    private final EntityManagerFactory emf;

    public DockingBayRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<DockingBay> findById(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(DockingBay.class, id));
        }
    }

    public Optional<DockingBay> findByIdWithShip(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip " +
                                    "WHERE db.id = :id",
                            DockingBay.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Optional<DockingBay> findByBayNumberWithShip(int bayNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "WHERE db.bayNumber = :bayNumber",
                            DockingBay.class)
                    .setParameter("bayNumber", bayNumber)
                    .getResultStream()
                    .findFirst();
        }
    }

    public List<DockingBay> findByOccupied(boolean occupied) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "WHERE db.isOccupied = :occupied",
                            DockingBay.class)
                    .setParameter("occupied", occupied)
                    .getResultList();
        }
    }

    public Optional<DockingBay> findOccupiedByShipId(UUID shipId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "WHERE db.isOccupied = true AND s.id = :shipId",
                            DockingBay.class)
                    .setParameter("shipId", shipId)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Optional<DockingBay> findOccupiedByShipIdWithCargo(UUID shipId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "LEFT JOIN FETCH TREAT(s AS CargoShip).cargoManifestLine cme " +
                                    "LEFT JOIN FETCH cme.cargoItem " +
                                    "WHERE db.isOccupied = true AND s.id = :shipId",
                            DockingBay.class)
                    .setParameter("shipId", shipId)
                    .getResultStream()
                    .findFirst();
        }
    }

    @Override
    public List<DockingBay> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers",
                            DockingBay.class)
                    .getResultList();
        }
    }

    @SuppressWarnings("unused")
    public List<DockingBay> findAllWithCargo() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT db FROM DockingBay db " +
                                    "LEFT JOIN FETCH db.spaceShip s " +
                                    "LEFT JOIN FETCH s.crewMembers " +
                                    "LEFT JOIN FETCH TREAT(s AS CargoShip).cargoManifestLine cme " +
                                    "LEFT JOIN FETCH cme.cargoItem",
                            DockingBay.class)
                    .getResultList();
        }
    }

    @Override
    public void save(DockingBay entity) {
        emf.runInTransaction(em ->
            em.persist(entity)
        );
    }

    @Override
    public void update(DockingBay entity) {
        emf.runInTransaction(em ->
            em.merge(entity)
        );
    }

    @Override
    public void delete(DockingBay entity) {
        emf.runInTransaction(em ->
            em.remove(em.contains(entity) ? entity : em.merge(entity))
        );
    }

    @Override
    public void deleteById(UUID id) {
        emf.runInTransaction(em -> {
            DockingBay dockingBay = em.find(DockingBay.class, id);
            if (dockingBay != null) {
                em.remove(dockingBay);
            }
        });
    }
}
