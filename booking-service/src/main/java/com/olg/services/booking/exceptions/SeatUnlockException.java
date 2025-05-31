package com.olg.services.booking.exceptions;

public class SeatUnlockException extends RuntimeException {
    public SeatUnlockException(String seatKey) {
        super("Seat unlock failed: " + seatKey);
    }
}
