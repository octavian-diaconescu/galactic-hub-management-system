package com.octavian.galactic.model.station;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.spaceship.SpaceShip;

// A physical location where a ship can park
public class DockingBay extends SpaceEntity {
    private final Size baySize;
    private boolean isOccupied;
    private SpaceShip spaceShip; // Can be null if the respective bay is empty


    public DockingBay(String name, Size baySize, boolean isOccupied) {
        super(name);
        this.baySize = baySize;
        this.isOccupied = isOccupied;
    }

    public void dockSpaceShip(SpaceShip spaceShip) {
        if (spaceShip.getShipSize().ordinal() > baySize.ordinal()) {
            System.out.println("[BSP] Ship is too big for the current bay"); // BSP = Bay Service Provider
            return;
        }
        this.spaceShip = spaceShip;
        this.isOccupied = true;
    }

    public void undockSpaceShip() {
        this.spaceShip = null;
        System.out.println("[BSP] SpaceShip has been undocked");
    }

    public Size getBaySize() {
        return baySize;
    }

    public SpaceShip getSpaceShip() {
        return this.spaceShip;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }
}
