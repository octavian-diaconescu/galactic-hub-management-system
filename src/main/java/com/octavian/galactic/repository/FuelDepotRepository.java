package com.octavian.galactic.repository;

import com.octavian.galactic.model.station.FuelDepot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FuelDepotRepository implements BaseRepository<FuelDepot> {
    private final EntityManagerFactory emf;

    public FuelDepotRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<FuelDepot> findById(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(FuelDepot.class, id));
        }
    }

    /*
    Right now, I plan on holding only one fuel depot. Creating multiple hubs isn't a priority right now
    so findAll is fine here because there should only be one fuel depot in the database.
    When I'll move on to multiple hubs and multiple depots, I'll have to have a hub linked to each fuel depot or vice versa
    */
    @Override
    public List<FuelDepot> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT fd FROM FuelDepot fd", FuelDepot.class)
                    .getResultList();
        }
    }

    @Override
    public void save(FuelDepot entity) {
        emf.runInTransaction(em -> em.persist(entity));
    }

    @Override
    public void update(FuelDepot entity) {
        emf.runInTransaction(em -> em.merge(entity));
    }

    @Override
    public void delete(FuelDepot entity) {
        emf.runInTransaction(em ->
                em.remove(em.contains(entity) ? entity : em.merge(entity))
        );
    }

    @SuppressWarnings("unused")
    @Override
    public void deleteById(UUID id) {
        emf.runInTransaction(em -> {
            FuelDepot depot = em.find(FuelDepot.class, id);
            if (depot != null) {
                em.remove(depot);
            }
        });
    }
}
