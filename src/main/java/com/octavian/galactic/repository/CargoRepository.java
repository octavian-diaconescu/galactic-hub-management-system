package com.octavian.galactic.repository;

import com.octavian.galactic.model.cargo.CargoItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CargoRepository implements BaseRepository<CargoItem> {
    private final EntityManagerFactory emf;

    CargoRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<CargoItem> findById(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(CargoItem.class, id));
        }
    }

    @Override
    public List<CargoItem> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT ci from CargoItem ci", CargoItem.class).getResultList();
        }
    }

    @Override
    public void save(CargoItem entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void update(CargoItem entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void delete(CargoItem entity) {
        emf.runInTransaction(em -> {
            em.getTransaction().begin();
            em.remove(entity);
            em.getTransaction().commit();
        });
    }

    @Override
    public void deleteById(UUID id) {
        emf.runInTransaction(em ->{
           em.getTransaction().begin();
           em.remove(em.find(CargoItem.class, id));
           em.getTransaction().commit();
        });
    }
}
