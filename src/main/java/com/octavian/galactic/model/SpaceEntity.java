package com.octavian.galactic.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class SpaceEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    protected SpaceEntity() {
    }

    public SpaceEntity(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SpaceEntity that = (SpaceEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] '%s' (ID: %s)", this.getClass().getSimpleName(), name, id.toString().substring(0, 8));
    }
}
