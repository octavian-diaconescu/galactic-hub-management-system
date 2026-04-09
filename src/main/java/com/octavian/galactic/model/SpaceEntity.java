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
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    protected SpaceEntity() {
    }

    public SpaceEntity(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.id = UUID.randomUUID();
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
        return o instanceof SpaceEntity && ((SpaceEntity) o).id.equals(id); // Refactor deemed necessary because of the hibernate guide(https://docs.hibernate.org/orm/7.0/introduction/html_single/#equals-and-hash)
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    } // Same refactor reason

    @Override
    public String toString() {
        return String.format("[%s] '%s' (ID: %s)", this.getClass().getSimpleName(), name, id.toString().substring(0, 8));
    }
}
