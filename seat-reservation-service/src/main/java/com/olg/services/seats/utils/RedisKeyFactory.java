package com.olg.services.seats.utils;

public class RedisKeyFactory {
    private RedisKeyFactory() {
        // Utility class, prevent instantiation
    }

    public static String reservationKey(Long eventId) {
        return String.format("seats:reservation:%s", eventId);
    }

    public static String reservationField(Long venueId, String rowNumber, String seatNumber) {
        return String.format("%s_%s_%s", venueId, rowNumber, seatNumber);
    }

    public static String lockKey(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        return String.format("seat:lock:%s_%s_%s_%s", eventId, venueId, rowNumber, seatNumber);
    }

    public static String channel(Long eventId, String op) {
        return String.format("seats:%s:event:%s", op, eventId);
    }
}
