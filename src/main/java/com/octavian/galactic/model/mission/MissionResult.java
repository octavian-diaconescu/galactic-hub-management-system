package com.octavian.galactic.model.mission;

public class MissionResult {
    private final Mission mission;
    private final SpaceEvent event;
    private final boolean success;
    private final double creditsEarned;
    private final int hullDamageTaken;
    private final int fuelConsumed;
    private final String narrative;

    private MissionResult(Builder builder) {
        this.mission = builder.mission;
        this.event = builder.event;
        this.success = builder.success;
        this.creditsEarned = builder.creditsEarned;
        this.hullDamageTaken = builder.hullDamageTaken;
        this.fuelConsumed = builder.fuelConsumed;
        this.narrative = builder.narrative;
    }

    public static class Builder {
        private final Mission mission;
        private final SpaceEvent event;
        private boolean success = true;
        private double creditsEarned = 100;
        private int hullDamageTaken = 0;
        private int fuelConsumed = 0;
        private String narrative = "";

        public Builder(Mission mission, SpaceEvent event) {
            this.mission = mission;
            this.event = event;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder creditsEarned(double creditsEarned) {
            this.creditsEarned = creditsEarned;
            return this;
        }

        public Builder hullDamageTaken(int damage) {
            this.hullDamageTaken = damage;
            return this;
        }

        public Builder fuelConsumed(int fuel) {
            this.fuelConsumed = fuel;
            return this;
        }

        public Builder narrative(String narrative) {
            this.narrative = narrative;
            return this;
        }

        public MissionResult build() {
            return new MissionResult(this);
        }
    }

    public Mission getMission() {
        return mission;
    }

    public SpaceEvent getEvent() {
        return event;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getCreditsEarned() {
        return creditsEarned;
    }

    public int getHullDamageTaken() {
        return hullDamageTaken;
    }

    public int getFuelConsumed() {
        return fuelConsumed;
    }

    public String getNarrative() {
        return narrative;
    }

    public void printSummary() {
        System.out.println("\n========== MISSION REPORT ==========");
        System.out.printf("Mission   : %s%n", mission.name());
        System.out.printf("Type      : %s%n", mission.type());
        System.out.printf("Event     : %s%n", event);
        System.out.printf("Outcome   : %s%n", success ? "SUCCESS" : "FAILED");
        System.out.printf("Credits   : +%.0f%n", creditsEarned);
        System.out.printf("Hull Dmg  : -%d%n", hullDamageTaken);
        System.out.printf("Fuel Used : -%d%n", fuelConsumed);
        System.out.printf("Log       : %s%n", narrative);
        System.out.println("====================================\n");
    }
}
