package com.olg.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SeatStatus {
    AVAILABLE(1),
    LOCKED(2),
    BOOKED(3);

    private final int value;

    SeatStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}

