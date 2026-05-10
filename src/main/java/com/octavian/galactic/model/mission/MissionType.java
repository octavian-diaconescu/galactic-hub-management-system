package com.octavian.galactic.model.mission;

public enum MissionType {
    PATROL("FighterShip"),
    EXPLORE("ScoutShip"),
    HAUL("CargoShip");

    public final String description;

    private MissionType(String description) {
        this.description = description;
    }
}
