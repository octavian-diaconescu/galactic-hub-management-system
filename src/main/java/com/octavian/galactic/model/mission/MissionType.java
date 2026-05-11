package com.octavian.galactic.model.mission;

public enum MissionType {
    PATROL("FighterShip"),
    EXPLORE("ScoutShip"),
    HAUL("CargoShip");

    public final String shipRecommendation;

    MissionType(String description) {
        this.shipRecommendation = description;
    }
}
