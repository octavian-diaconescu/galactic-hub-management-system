package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.station.CrewMember;

import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.UUID;

// The base for all flying vehicles
public abstract class SpaceShip extends SpaceEntity {
    private int fuelLevel; // 0 to 100 reinforced in the setter function
    private int hullIntegrity; // 0 to 100 reinforced in the setter function
    private final int maxCrewCapacity;

    private final Set<CrewMember> crewMembers = new TreeSet<>();

    public SpaceShip(String name, int fuelLevel, int hullIntegrity, int maxCrewCapacity) {
        super(name);
        setFuelLevel(fuelLevel);
        setHullIntegrity(hullIntegrity);
        this.maxCrewCapacity = maxCrewCapacity;
    }

    public void setFuelLevel(int fuelLevel) {
        if (fuelLevel < 0 || fuelLevel > 100) {
            throw new IllegalArgumentException("Fuel must be between 0 and 100");
        }
        this.fuelLevel = fuelLevel;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public int getHullIntegrity() {
        return hullIntegrity;
    }

    public void setHullIntegrity(int hIntegrity) {
        if (hIntegrity < 0 || hIntegrity > 100) {
            throw new IllegalArgumentException("Hull integrity must be between 0 and 100");
        }
        this.hullIntegrity = hIntegrity;
    }

    public void addCrewMember(CrewMember crew) {
        if (crew == null) {
            throw new IllegalArgumentException("Crew cannot be null");
        }
        // Check for capacity constraints
        if (crewMembers.size() + 1 > maxCrewCapacity) {
            System.out.printf("[%s] %s is full!%n", this.getClass().getSimpleName(), this.getName());
            return;
        }
        // Finally, add the crew member
        if (this.crewMembers.add(crew)) {
            System.out.printf("[MANIFEST] Welcome aboard '%s', %s!%n", this.getName(), crew.getName());
        }
        else{
            System.out.printf("[MANIFEST] %s is already registered on this ship.%n", crew.getName());
        }
    }

    public boolean removeCrewMember(UUID crewID) {
        // Remove the crew member by UUID
        return this.crewMembers.removeIf(m -> m.getId().equals(crewID));
    }

    public Set<CrewMember> getCrewMembers() {
        // Return an unmodifiable set to enforce the use of removeCrewMember and addCrewMember methods
        return Collections.unmodifiableSet(crewMembers);
    }
}
