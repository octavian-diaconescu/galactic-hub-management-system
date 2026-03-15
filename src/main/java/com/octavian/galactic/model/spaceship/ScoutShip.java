package com.octavian.galactic.model.spaceship;

// A fast, light ship for exploration (doesn't store cargo)
public class ScoutShip extends SpaceShip {
    private int sensorRange; // How far it can detect other objects
    private boolean isStealthEnabled; // Toggle for radar visibility

    public ScoutShip(String name, int fuelLevel, int hullIntegrity, int maxCrewCapacity, int sensorRange){
        super(name, fuelLevel, hullIntegrity, maxCrewCapacity);
        setSensorRange(sensorRange);
        setStealthEnabled(false);
    }

    public boolean isStealthEnabled() {
        return isStealthEnabled;
    }

    public void setStealthEnabled(boolean stealthEnabled) {
        isStealthEnabled = stealthEnabled;
    }

    public int getSensorRange() {
        return sensorRange;
    }

    public void setSensorRange(int sensorRange) {
        if(sensorRange < 0)
            throw new IllegalArgumentException("Sensor range cannot be less than 0");
        this.sensorRange = sensorRange;
    }
}
