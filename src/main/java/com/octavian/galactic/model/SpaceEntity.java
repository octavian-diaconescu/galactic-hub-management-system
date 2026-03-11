package com.octavian.galactic.model;

import java.util.UUID;

public abstract class SpaceEntity {
    protected String name;
    private final UUID id;

    public SpaceEntity(String name){
        this.name = name;
        id = UUID.randomUUID();
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
    public String toString() {
        return String.format("[%s] %s (ID: %s)", this.getClass().getSimpleName(), name, id.toString().substring(0,8));
    }
}
