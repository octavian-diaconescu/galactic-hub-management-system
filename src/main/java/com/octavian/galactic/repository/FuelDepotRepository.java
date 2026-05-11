package com.octavian.galactic.repository;

import com.octavian.galactic.model.station.FuelDepot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

//TODO: finish FuelDepotRepository and persist FuelDepot
public class FuelDepotRepository implements BaseRepository<FuelDepot> {
    private final EntityManagerFactory emf;

    public FuelDepotRepository(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Optional<FuelDepot> findById(UUID id) {
        try(EntityManager em = emf.createEntityManager()){
            return Optional.ofNullable(em.find(FuelDepot.class, id));
        }
    }

    @Override
    public List<FuelDepot> findAll() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery(
                    "SELECT DISTINCT fd FROM FuelDepot fd",
                    FuelDepot.class
            ).getResultList();
        }
    }

    @Override
    public void save(FuelDepot entity) {

    }

    @Override
    public void update(FuelDepot entity) {

    }

    @Override
    public void delete(FuelDepot entity) {

    }

    @Override
    public void deleteById(UUID id) {

    }
}
