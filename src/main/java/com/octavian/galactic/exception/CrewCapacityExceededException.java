package com.octavian.galactic.exception;

public class CrewCapacityExceededException extends RuntimeException {

    public CrewCapacityExceededException(String shipName, int maxCapacity) {
        super(String.format(
                "'%s' is at maximum crew capacity (%d). Cannot board additional crew members",
                shipName, maxCapacity
        ));
    }
}
