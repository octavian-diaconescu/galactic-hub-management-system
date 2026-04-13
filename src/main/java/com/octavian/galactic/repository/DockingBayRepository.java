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
        try(EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(DockingBay.class, id));
        }
    }

    @Override
    public List<DockingBay> findAll() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT db from DockingBay db", DockingBay.class).getResultList();
        }
    }

    @Override
    public void save(DockingBay entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void update(DockingBay entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void delete(DockingBay entity) {
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
            em.remove(em.find(DockingBay.class, id));
            em.getTransaction().commit();
        });
    }
}
