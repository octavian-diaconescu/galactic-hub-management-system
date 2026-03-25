package com.octavian.galactic.exception;

public class InsufficientContainmentException extends RuntimeException {

    public InsufficientContainmentException(String itemName, String containmentType) {
        super(String.format(
                "LOAD REJECTED: '%s' has a radiation level requiring Lead-lined containment, but '%s' was provided. " +
                        "Do not load until containment is upgraded",
                itemName, containmentType
        ));
    }
}
