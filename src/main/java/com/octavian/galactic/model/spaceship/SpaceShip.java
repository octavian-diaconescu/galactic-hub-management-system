package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.model.Fuellable;
import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.Size;

import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.UUID;

//TODO: implement builder pattern

// The base for all flying vehicles
public abstract class SpaceShip extends SpaceEntity implements Fuellable {
    private int fuelLevel; // 0 to 100 reinforced in the setter function
    private int hullIntegrity; // 0 to 100 reinforced in the setter function
    private final int maxCrewCapacity;
    private final Size shipSize;

    private final Set<CrewMember> crewMembers = new TreeSet<>();

    public SpaceShip(String name, int fuelLevel, int hullIntegrity, int maxCrewCapacity, Size shipSize) {
        super(name);
        setFuelLevel(fuelLevel);
        setHullIntegrity(hullIntegrity);
        this.maxCrewCapacity = maxCrewCapacity;
        this.shipSize = shipSize;
    }

    public Size getShipSize() {
        return shipSize;
    }

    public void setFuelLevel(int fuelLevel) {
        if (fuelLevel < 0 || fuelLevel > 100) {
            throw new IllegalArgumentException("Fuel must be between 0 and 100");
        }
        this.fuelLevel = fuelLevel;
    }

    @Override
    public int getFuelLevel() {
        return fuelLevel;
    }
    @Override
    public boolean fuelTankIsEmpty(){
        return fuelLevel == 0;
    }
    @Override
    public void refuel(int amount){
        this.fuelLevel += amount;
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
            System.out.printf("[MANIFEST] (%s)'%s' is at max crew capacity%n", this.getClass().getSimpleName(), this.getName());
            return;
        }
        // Finally, add the crew member
        if (this.crewMembers.add(crew)) {
            System.out.printf("[MANIFEST] Welcome aboard '%s', %s!%n", this.getName(), crew.getName());
        }
        else{
            System.out.printf("[MANIFEST] '%s' is already registered on this ship.%n", crew.getName());
        }
    }

    public void removeCrewMember(UUID crewID) {
        // Remove the crew member by UUID
        this.crewMembers.removeIf(m -> m.getId().equals(crewID));
    }

    public Set<CrewMember> getCrewMembers() {
        // Return an unmodifiable set to enforce the use of removeCrewMember and addCrewMember methods
        return Collections.unmodifiableSet(crewMembers);
    }

}
