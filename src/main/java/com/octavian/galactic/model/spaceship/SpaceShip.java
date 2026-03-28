package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.exception.CrewCapacityExceededException;
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

    protected SpaceShip(AbstractBuilder<?> builder) {
        super(builder.name);
        setFuelLevel(builder.fuelLevel);
        setHullIntegrity(builder.hullIntegrity);
        if (builder.maxCrewCapacity <= 0) {
            throw new IllegalArgumentException("maxCrewCapacity must be greater than 0");
        }
        this.maxCrewCapacity = builder.maxCrewCapacity;
        this.shipSize = builder.shipSize;

    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private final String name;
        private final Size shipSize;
        private int fuelLevel = 100;
        private int hullIntegrity = 100;
        private int maxCrewCapacity = 5;

        public AbstractBuilder(String name, Size shipSize) {
            this.name = name;
            this.shipSize = shipSize;
        }

        public T fuelLevel(int fuelLevel) {
            this.fuelLevel = fuelLevel;
            return self();
        }

        public T hullIntegrity(int hullIntegrity) {
            this.hullIntegrity = hullIntegrity;
            return self();
        }

        public T maxCrewCapacity(int maxCrewCapacity) {
            this.maxCrewCapacity = maxCrewCapacity;
            return self();
        }

        protected abstract T self();

        public abstract SpaceShip build();
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
    public int getFuelCapacity() {
        return 100;
    }

    @Override
    public boolean fuelTankIsEmpty() {
        return fuelLevel == 0;
    }

    @Override
    public void refuel(int amount) {
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
            throw new CrewCapacityExceededException(this.name, this.maxCrewCapacity);
        }
        // Finally, add the crew member
        if (this.crewMembers.add(crew)) {
            System.out.printf("[MANIFEST] Welcome aboard '%s', %s!%n", this.getName(), crew.getName());
        } else {
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
