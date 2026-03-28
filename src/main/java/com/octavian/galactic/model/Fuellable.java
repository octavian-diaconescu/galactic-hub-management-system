package com.octavian.galactic.model;

public interface Fuellable {
    void refuel(int amount);
    int getFuelLevel();
    int getFuelCapacity();
    boolean fuelTankIsEmpty();
}
