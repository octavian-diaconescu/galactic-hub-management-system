package com.octavian.galactic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class AuditService {
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private AuditService() {}

    public enum Action{
        BAY_ADDED,
        BAY_REMOVED,
        SHIP_REGISTERED,
        SHIP_DOCKED,
        SHIP_UNDOCKED,
        CREW_ONBOARDED,
        CREW_TRANSFERRED,
        CARGO_LOADED,
        HAZARD_SCAN,
        MISSION_DISPATCHED,
        BILLING_GENERATED,
        EMERGENCY_EVACUATION
    }

    /**
     * Writes one audit record to the CSV log
     * @param action what happened
     * @param entityId the primary entity involved(e.g: shipName, bayNumber, etc.)
     * @param detail any extra details
     */
    public static void log(Action action, String entityId, String detail){
        // Format: timestamp, action, entityId, detail
        audit.info("{},{},{},\"{}\"",
                Instant.now(),
                action,
                entityId,
                detail != null ? detail.replace("\"","\"\"" ) : ""
    );
    }
    public static void log(Action action, String entityId){
        log(action, entityId, null);
    }
}
