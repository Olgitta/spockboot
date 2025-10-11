package com.olg.services.booking.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for generating and parsing Redis keys related to seat reservations, bookings, and events.
 * This class provides methods to construct Redis keys and parse them back into their components.
 */
public class RedisKeyFactory {

    // Prefix for Redis keys used for seat locks
    public static String REDIS_SEAT_LOCK_PREFIX = "seat:lock:";
    // Regex pattern for parsing seat lock keys: seat:lock:{eventId}_{venueId}_{rowNumber}_{seatNumber}
    public static final Pattern LOCK_KEY_PATTERN = Pattern.compile("^seat:lock:(\\d+)_(\\d+)_([^_]+)_([^_]+)$");

    // Prefix for Redis keys used for seat reservations
    public static String REDIS_SEAT_RESERVATION_PREFIX = "seats:reservation:";
    // Regex pattern for parsing seat reservation fields: {rowNumber}_{seatNumber}
    public static final Pattern SEAT_RESERVATION_FIELD_PATTERN = Pattern.compile("^([^_]+)_([^_]+)$");

    // Prefix for Redis keys used for seat bookings
    public static String REDIS_SEAT_BOOKING_PREFIX = "seats:booking:";
    // Prefix for Redis keys used for seat events
    public static String REDIS_SEAT_EVENTS_PREFIX = "seat:events:";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RedisKeyFactory() {
        // Utility class, prevent instantiation
    }

    /**
     * Generates a Redis hash key for seat reservations based on event and venue IDs.
     *
     * @param eventId The ID of the event.
     * @param venueId The ID of the venue.
     * @return The Redis hash key for seat reservations.
     */
    public static String reservationHashKey(Long eventId, Long venueId) {
        return String.format(REDIS_SEAT_RESERVATION_PREFIX + "%s:%s", eventId, venueId);
    }

    /**
     * Generates a Redis hash field for a specific seat based on its row and seat numbers.
     *
     * @param rowNumber  The row number of the seat.
     * @param seatNumber The seat number.
     * @return The Redis hash field for the seat.
     */
    public static String reservationHashField(String rowNumber, String seatNumber) {
        return String.format("%s_%s", rowNumber, seatNumber);
    }

    /**
     * Generates a Redis hash key for seat bookings based on event and venue IDs.
     *
     * @param eventId The ID of the event.
     * @param venueId The ID of the venue.
     * @return The Redis hash key for seat bookings.
     */
    public static String bookingHashKey(Long eventId, Long venueId) {
        return String.format(REDIS_SEAT_BOOKING_PREFIX + "%s:%s", eventId, venueId);
    }

    /**
     * Generates a Redis hash field for a specific booked seat based on its row and seat numbers.
     *
     * @param rowNumber  The row number of the seat.
     * @param seatNumber The seat number.
     * @return The Redis hash field for the booked seat.
     */
    public static String bookingHashField(String rowNumber, String seatNumber) {
        return String.format("%s_%s", rowNumber, seatNumber);
    }

    /**
     * Generates a Redis key for locking a specific seat based on event, venue, row, and seat details.
     *
     * @param eventId    The ID of the event.
     * @param venueId    The ID of the venue.
     * @param rowNumber  The row number of the seat.
     * @param seatNumber The seat number.
     * @return The Redis lock key for the seat.
     */
    public static String lockKey(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        return String.format(REDIS_SEAT_LOCK_PREFIX + "%s_%s_%s_%s", eventId, venueId, rowNumber, seatNumber);
    }

    /**
     * Parses a Redis lock key into its components (event ID, venue ID, row number, seat number).
     *
     * @param key The Redis lock key to parse.
     * @return A SeatKeyDetails object containing the parsed components, or null if the key does not match the pattern.
     */
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

    /**
     * Parses a Redis reservation field into its components (row number, seat number).
     *
     * @param key     The Redis reservation field to parse.
     * @param eventId The ID of the event (for context).
     * @param venueId The ID of the venue (for context).
     * @return A SeatKeyDetails object containing the parsed components, or null if the field does not match the pattern.
     */
    public static SeatKeyDetails parseReservationField(String key, Long eventId, Long venueId) {
        Matcher matcher = SEAT_RESERVATION_FIELD_PATTERN.matcher(key);
        if (matcher.matches() && matcher.groupCount() == 2) {
            String rowNumber = matcher.group(1);
            String seatNumber = matcher.group(2);
            return new SeatKeyDetails(eventId, venueId, rowNumber, seatNumber);
        }
        return null;
    }

    /**
     * Generates a Redis channel name for seat events based on event and venue IDs.
     *
     * @param eventId The ID of the event.
     * @param venueId The ID of the venue.
     * @return The Redis channel name for seat events.
     */
    public static String channelName(Long eventId, Long venueId) {
        return String.format(REDIS_SEAT_EVENTS_PREFIX + "%s_%s", eventId, venueId);
    }

    /**
     * Record class representing the details of a seat key.
     */
    public record SeatKeyDetails(Long eventId, Long venueId, String rowNumber, String seatNumber) {}
}