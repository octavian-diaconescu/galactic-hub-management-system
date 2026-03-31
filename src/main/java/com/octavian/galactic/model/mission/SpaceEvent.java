package com.octavian.galactic.model.mission;

public enum SpaceEvent {
    CLEAR,               // Safe passage
    PIRATE_ATTACK,       // Combat encounter: FighterShip can absorbDamage() + fire(), other ships take hull dagame
    NEBULA_INTERFERENCE, // Navigation disruption: ScoutShip mitigates with sensorRange
    DEBRIS_FIELD,        // CargoShip risks cargo loss
    DERELICT_SHIP        // sensorRange determines what's recovered
}
