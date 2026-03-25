package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.model.Size;

// A fast, light ship for exploration (doesn't store cargo)
public class ScoutShip extends SpaceShip {
    private int sensorRange; // How far it can detect other objects
    private boolean isStealthEnabled; // Toggle for radar visibility

    private ScoutShip(Builder builder) {
        super(builder);
        setSensorRange(builder.sensorRange);
        setStealthEnabled(false);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private int sensorRange = 100;
        private boolean isStealthEnabled = false;

        public Builder(String name, Size size) {
            super(name, size);
        }

        public Builder sensorRange(int sensorRange) {
            this.sensorRange = sensorRange;
            return this;
        }

        public Builder stealthState(boolean stealthState) {
            this.isStealthEnabled = stealthState;
            return this;
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public ScoutShip build() {
            return new ScoutShip(this);
        }
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
        if (sensorRange < 0)
            throw new IllegalArgumentException("Sensor range cannot be less than 0");
        this.sensorRange = sensorRange;
    }
}
