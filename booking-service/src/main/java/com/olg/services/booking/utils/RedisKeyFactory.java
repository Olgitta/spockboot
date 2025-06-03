package com.olg.services.booking.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedisKeyFactory {
    public static String REDIS_SEAT_LOCK_PREFIX = "seat:lock:";
    // Regex для ключа блокировки: seat:lock:{eventId}_{venueId}_{rowNumber}_{seatNumber}
    public static final Pattern LOCK_KEY_PATTERN = Pattern.compile("^seat:lock:(\\d+)_(\\d+)_([^_]+)_([^_]+)$");

    public static String REDIS_SEAT_RESERVATION_PREFIX = "seats:reservation:";
    // {rowNumber}_{seatNumber}
    public static final Pattern SEAT_RESERVATION_FIELD_PATTERN = Pattern.compile("^([^_]+)_([^_]+)$");

    public static String REDIS_SEAT_BOOKING_PREFIX = "seats:booking:";
    public static String REDIS_SEAT_EVENTS_PREFIX = "seat:events:";

    private RedisKeyFactory() {
        // Utility class, prevent instantiation
    }

    public static String reservationHashKey(Long eventId, Long venueId) {
        return String.format(REDIS_SEAT_RESERVATION_PREFIX + "%s:%s", eventId, venueId);
    }

    public static String reservationHashField(String rowNumber, String seatNumber) {
        return String.format("%s_%s", rowNumber, seatNumber);
    }

    public static String bookingHashKey(Long eventId, Long venueId) {
        return String.format(REDIS_SEAT_BOOKING_PREFIX + "%s:%s", eventId, venueId);
    }

    public static String bookingHashField(String rowNumber, String seatNumber) {
        return String.format("%s_%s", rowNumber, seatNumber);
    }

    public static String lockKey(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        return String.format(REDIS_SEAT_LOCK_PREFIX + "%s_%s_%s_%s", eventId, venueId, rowNumber, seatNumber);
    }

    public static SeatKeyDetails parseLockKey(String key) {
        Matcher matcher = LOCK_KEY_PATTERN.matcher(key);
        if (matcher.matches() && matcher.groupCount() == 4) {
            Long eventId = Long.valueOf(matcher.group(1));
            Long venueId = Long.valueOf(matcher.group(2));
            String rowNumber = matcher.group(3);
            String seatNumber = matcher.group(4);
            return new SeatKeyDetails(eventId, venueId, rowNumber, seatNumber);
        }
        return null;
    }

    public static SeatKeyDetails parseReservationField(String key, Long eventId, Long venueId) {
        Matcher matcher = SEAT_RESERVATION_FIELD_PATTERN.matcher(key);
        if (matcher.matches() && matcher.groupCount() == 2) {
            String rowNumber = matcher.group(1);
            String seatNumber = matcher.group(2);
            return new SeatKeyDetails(eventId, venueId, rowNumber, seatNumber);
        }
        return null;
    }

    public static String channelName(Long eventId, Long venueId) {
        return String.format(REDIS_SEAT_EVENTS_PREFIX + "%s_%s", eventId, venueId);
    }

    public record SeatKeyDetails(Long eventId, Long venueId, String rowNumber, String seatNumber) {}

}
