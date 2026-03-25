package com.octavian.galactic.exception;

import java.util.UUID;

public class DockingBayNotFoundException extends RuntimeException {

    public DockingBayNotFoundException(int bayNumber) {
        super("Docking bay " + bayNumber + " does not exist");
    }

    public DockingBayNotFoundException(UUID id) {
        super("No docking bay found with ID: " + id.toString().substring(0, 8));
    }
}
