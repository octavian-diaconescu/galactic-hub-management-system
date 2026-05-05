package com.octavian.galactic.model.station;

import com.octavian.galactic.exception.InsufficientFuelException;
import com.octavian.galactic.model.Fuellable;
import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.spaceship.SpaceShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: persist FuelDepot class
public class FuelDepot extends SpaceEntity implements Fuellable {
    private static final Logger logger = LoggerFactory.getLogger(FuelDepot.class);

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
            throw new InsufficientFuelException(this.getName(), amount, fuelLevel);

        fuelLevel -= amount;
        ship.refuel(amount);
        logger.info("[DEPOT] '{}' dispensed {} units to '{}'. Depot reserve: {}/{}",
                this.getName(), amount, ship.getName(), fuelLevel, fuelCapacity);
    }

    @Override
    public void refuel(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Restock amount must be positive");
        int space = fuelCapacity - fuelLevel;
        int actualAmount = Math.min(amount, space); // can't exceed capacity
        fuelLevel += actualAmount;
        logger.info("[DEPOT] '{}' restocked by {} units. Reserve: {}/{}",
                this.getName(), actualAmount, fuelLevel, fuelCapacity);
        if (actualAmount < amount)
            logger.warn("[DEPOT] Depot full, {} units discarded", amount - actualAmount);
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
