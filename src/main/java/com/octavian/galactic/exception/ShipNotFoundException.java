package com.octavian.galactic.exception;

import java.util.UUID;

public class ShipNotFoundException extends RuntimeException {

    public ShipNotFoundException(UUID id) {
        super("No registered ship found with ID: " + id.toString().substring(0, 8));
    }

    public ShipNotFoundException(String message) {
        super(message);
    }
}
