package com.octavian.galactic.model.station;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.spaceship.SpaceShip;
import jakarta.persistence.*;

// A physical location where a ship can park
@Entity
@Table(name = "docking_bay")
public class DockingBay extends SpaceEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "bay_size", nullable = false)
    private Size baySize;

    @Column(name = "is_occupied", nullable = false)
    private boolean isOccupied;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "spaceship_id", unique = true)
    @Column(nullable = true)
    private SpaceShip spaceShip; // Can be null if the respective bay is empty

    protected DockingBay() {}

    public DockingBay(String name, Size baySize, boolean isOccupied) {
        super(name);
        this.baySize = baySize;
        this.isOccupied = isOccupied;
    }

    public void dockSpaceShip(SpaceShip spaceShip) {
        if(spaceShip.isDocked()){
            throw new IllegalStateException("Ship is already docked");
        }
        if (spaceShip.getShipSize().ordinal() > baySize.ordinal()) {
            throw new IllegalArgumentException(
                    String.format("[BSP] '%s' (%s) is too large for bay '%s' (%s)", // BSP = Bay Service Provider
                            spaceShip.getName(), spaceShip.getShipSize(), this.getName(), baySize)
            );
        }
        this.spaceShip = spaceShip;
        this.isOccupied = true;
        this.spaceShip.setDocked(true);
    }

    public void undockSpaceShip() {
        if (!isOccupied || spaceShip == null) {
            System.out.printf("[BSP] Warning: Attempted to undock from already-empty bay '%s'%n", this.getName());
            return;
        }
        System.out.printf("[BSP] SpaceShip '%s' has been undocked%n", this.spaceShip.getName());
        this.spaceShip.setDocked(false);
        this.spaceShip = null;
        this.isOccupied = false;
    }

    public Size getBaySize() {
        return baySize;
    }

    public SpaceShip getSpaceShip() {
        return this.spaceShip;
    }

    // Returns the occupation state
    public boolean isOccupied() {
        return isOccupied;
    }

//    public void setOccupied(boolean occupied) {
//        isOccupied = occupied;
//    }
}
