package com.octavian.galactic.exception;

public class CargoCapacityExceededException extends RuntimeException {

    public CargoCapacityExceededException(String shipName, String itemName, int quantity, double overBy) {
        super(String.format(
                "Cannot load '%s' x [%d] onto '%s' — would exceed max cargo weight by %.2f tonnes",
                itemName, quantity, shipName, overBy
        ));
    }
}
