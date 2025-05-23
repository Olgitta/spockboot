package com.olg.services.seats.exceptions;

public class SeatAlreadyLockedException extends RuntimeException {
    public SeatAlreadyLockedException(String seatKey) {
        super("Seat already locked: " + seatKey);
    }
}
