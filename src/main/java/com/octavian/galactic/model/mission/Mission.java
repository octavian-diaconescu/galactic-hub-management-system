package com.octavian.galactic.model.mission;

import java.util.Objects;

public record Mission(String name, MissionType type, int distance, double baseReward) {
    public Mission {
        Objects.requireNonNull(name, "Mission name cannot be null");
        Objects.requireNonNull(type, "Mission type cannot be null");

        if (distance < 0)
            throw new IllegalArgumentException("Mission distance must be positive");
        if (baseReward < 0)
            throw new IllegalArgumentException("Mission base reward must be positive");
    }

    @Override
    public String toString() {
        return String.format("Mission '%s' [%s] | Distance: %d | Reward: %.0f credits",
                name, type, distance, baseReward);
    }
}
