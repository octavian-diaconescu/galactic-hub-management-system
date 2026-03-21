package com.octavian.galactic.model;

public interface Fuellable {
    void refuel(int amount);
    int getFuelLevel();
    boolean fuelTankIsEmpty();
}
