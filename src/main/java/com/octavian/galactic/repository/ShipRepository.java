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
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(SpaceShip.class, id));
        } finally {
            emf.close();
        }
    }

    @Override
    public List<SpaceShip> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s from SpaceShip s", SpaceShip.class).getResultList();
        }
    }

    @Override
    public void save(SpaceShip entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void update(SpaceShip entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        });
    }


    @Override
    public void delete(SpaceShip entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.remove(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void deleteById(UUID id) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.remove(em.find(SpaceShip.class, id));
            em.getTransaction().commit();
        });
    }
}
