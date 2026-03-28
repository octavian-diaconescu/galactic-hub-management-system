package com.octavian.galactic.exception;

public class InsufficientFuelException extends RuntimeException {
    public InsufficientFuelException(String depotName, int requested, int available) {
        super(String.format(
                "Depot '%s' cannot dispense %d units — only %d available. Restock required.",
                depotName, requested, available
        ));
    }
}