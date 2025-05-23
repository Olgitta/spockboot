package com.olg.services.seats.exceptions;

public class SeatUnlockException extends RuntimeException {
    public SeatUnlockException(String seatKey) {
        super("Seat unlock failed: " + seatKey);
    }
}
