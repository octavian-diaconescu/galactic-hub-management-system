package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.exception.CrewCapacityExceededException;
import com.octavian.galactic.model.Fuellable;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.station.CrewMember;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Entity
@Table(name = "spaceship")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ship_type", discriminatorType = DiscriminatorType.STRING)
// The base for all flying vehicles
public abstract class SpaceShip extends SpaceEntity implements Fuellable {
    private static final Logger logger = LoggerFactory.getLogger(SpaceShip.class);

    @Column(name = "fuel_level", nullable = false)
    private int fuelLevel; // 0 to 100 reinforced in the setter function

    @Column(name = "hull_integrity", nullable = false)
    private int hullIntegrity; // 0 to 100 reinforced in the setter function

    @Column(name = "max_crew_capacity", nullable = false)
    private int maxCrewCapacity;

    @Column(name = "is_docked", nullable = false)
    private boolean docked;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private Size shipSize;

    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CrewMember> crewMembers = new TreeSet<>();

    protected SpaceShip() {}

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

    public boolean isDocked() {
        return docked;
    }

    public void setDocked(boolean docked) {
        this.docked = docked;
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
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        setFuelLevel(Math.min(fuelLevel + amount, 100));
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
            throw new CrewCapacityExceededException(this.getName(), this.maxCrewCapacity);
        }
        // Finally, add the crew member
        if (this.crewMembers.add(crew)) {
            logger.info("[MANIFEST] Welcome aboard '{}', {}!", this.getName(), crew.getName());
            crew.setShip(this);
        } else {
            logger.warn("[MANIFEST] '{}' is already registered on this ship.", crew.getName());
        }
    }

    public void removeCrewMember(UUID crewID) {
        // Remove the crew member by UUID and nullify the link between the crew member and the ship(Hibernate logic)
        crewMembers.stream()
                .filter(c -> c.getId().equals(crewID))
                .findFirst()
                .ifPresent(crew -> {
                    crewMembers.remove(crew);
                    crew.setShip(null);
                });
    }

    public Set<CrewMember> getCrewMembers() {
        // Return an unmodifiable set to enforce the use of removeCrewMember and addCrewMember methods
        return Collections.unmodifiableSet(crewMembers);
    }

    public boolean travel(int distance) {
        if (distance <= 0) {
            throw new IllegalArgumentException("Distance must be positive");
        }

        int fuelCost = calculateFuelCost(distance);

        if (fuelLevel < fuelCost) {
            logger.info("[NAV] '{}' cannot travel {} units: needs {} fuel, has {}.",
                    this.getName(), distance, fuelCost, fuelLevel);
            return false;
        }

        setFuelLevel(fuelLevel - fuelCost);
        logger.info("[NAV] '{}' successfully traveled {} units. Fuel: {} -> {}",
                this.getName(), distance, fuelLevel + fuelCost, fuelLevel);
        return true;
    }

    public int calculateFuelCost(int distance) {
        // TODO: refactor for ship sizes AND different ship types
        int sizeMultiplier = switch (shipSize) {
            case SMALL -> 1;
            case MEDIUM -> 2;
            case LARGE -> 3;
        };

        return Math.max(1, (distance / 100) * sizeMultiplier);
    }

}
