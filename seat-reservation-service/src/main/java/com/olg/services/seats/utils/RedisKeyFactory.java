package com.olg.services.seats.utils;

public class RedisKeyFactory {
    private RedisKeyFactory() {
        // Utility class, prevent instantiation
    }

    public static String reservationHashKey(Long eventId, Long venueId) {
        return String.format("seats:reservation:%s:%s", eventId, venueId);
    }

    public static String reservationHashField(String rowNumber, String seatNumber) {
        return String.format("%s_%s", rowNumber, seatNumber);
    }

    public static String bookingHashKey(Long eventId, Long venueId) {
        return String.format("seats:booking:%s:%s", eventId, venueId);
    }

    public static String bookingHashField(String rowNumber, String seatNumber) {
        return String.format("%s_%s", rowNumber, seatNumber);
    }

    public static String lockKey(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        return String.format("seat:lock:%s_%s_%s_%s", eventId, venueId, rowNumber, seatNumber);
    }

    public static String channelName(Long eventId, Long venueId) {
        return String.format("seat:events:%s_%s", eventId, venueId);
    }
}
