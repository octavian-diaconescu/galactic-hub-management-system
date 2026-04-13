package com.octavian.galactic.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseRepository<T> {
    Optional<T> findById(UUID id);
    List<T> findAll();
    void save(T entity);
    void update(T entity);
    void delete(T entity);
    void deleteById(UUID id);
}
