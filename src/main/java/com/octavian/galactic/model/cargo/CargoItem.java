package com.octavian.galactic.model.cargo;

import com.octavian.galactic.model.SpaceEntity;

public abstract class CargoItem extends SpaceEntity {
    private final double weight;
    public CargoItem(String name, double weight){
        super(name);
        if(weight <= 0){
            throw new IllegalArgumentException("Cargo weight cannot be 0 or less!");
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return  '\'' + name + '\'';
    }

    public double getWeight() {
        return weight;
    }
}
