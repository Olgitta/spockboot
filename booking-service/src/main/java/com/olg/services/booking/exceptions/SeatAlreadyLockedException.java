package com.olg.services.booking.exceptions;

public class SeatAlreadyLockedException extends RuntimeException {
    public SeatAlreadyLockedException(String seatKey) {
        super("Seat already locked: " + seatKey);
    }
}
