package com.octavian.galactic.model.station;

import com.octavian.galactic.exception.InsufficientFuelException;
import com.octavian.galactic.model.Fuellable;
import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.spaceship.SpaceShip;

public class FuelDepot extends SpaceEntity implements Fuellable {
    private int fuelLevel;
    private final int fuelCapacity;

    public FuelDepot(String name, int fuelCapacity, int initialFuelLevel) {
        super(name);
        if (fuelCapacity <= 0)
            throw new IllegalArgumentException("Fuel capacity must be greater than 0");
        this.fuelCapacity = fuelCapacity;
        setFuelLevel(initialFuelLevel);
    }

    public void dispenseFuel(SpaceShip ship, int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Dispense amount must be positive");
        if (fuelLevel < amount)
            throw new InsufficientFuelException(this.name, amount, fuelLevel);

        fuelLevel -= amount;
        ship.setFuelLevel(ship.getFuelLevel() + amount);
        System.out.printf("[DEPOT] '%s' dispensed %d units to '%s'. Depot reserve: %d/%d%n",
                this.name, amount, ship.getName(), fuelLevel, fuelCapacity);
    }

    @Override
    public void refuel(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Restock amount must be positive");
        int space = fuelCapacity - fuelLevel;
        int actualAmount = Math.min(amount, space); // can't exceed capacity
        fuelLevel += actualAmount;
        System.out.printf("[DEPOT] '%s' restocked by %d units. Reserve: %d/%d%n",
                this.name, actualAmount, fuelLevel, fuelCapacity);
        if (actualAmount < amount)
            System.out.printf("[DEPOT] Warning: depot full, %d units discarded%n",
                    amount - actualAmount);
    }

    @Override
    public int getFuelLevel() { return fuelLevel; }

    @Override
    public int getFuelCapacity() { return fuelCapacity; }

    @Override
    public boolean fuelTankIsEmpty() { return fuelLevel == 0; }

    private void setFuelLevel(int fuelLevel) {
        if (fuelLevel < 0 || fuelLevel > fuelCapacity)
            throw new IllegalArgumentException(
                    "Fuel level must be between 0 and capacity (" + fuelCapacity + ")");
        this.fuelLevel = fuelLevel;
    }
}
