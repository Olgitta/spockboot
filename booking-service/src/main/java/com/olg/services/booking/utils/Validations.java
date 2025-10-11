package com.olg.services.booking.utils;

import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for validation-related methods.
 */
public class Validations {

    /**
     * Checks if a given timestamp has expired based on the specified time-to-live (TTL) in seconds.
     *
     * @param timestampString The timestamp string in ISO-8601 format representing the lock time.
     * @param lockTtlSeconds  The time-to-live (TTL) in seconds for the lock.
     * @return {@code true} if the timestamp has expired, {@code false} otherwise.
     */
    public static boolean isExpired(String timestampString, long lockTtlSeconds) {
        Instant now = Instant.now();
        // Parse the timestamp when the lock was created
        Instant lockTimestamp = Instant.parse(timestampString);
        // Calculate the elapsed time since the lock was created
        long elapsedSeconds = ChronoUnit.SECONDS.between(lockTimestamp, now);

        return (elapsedSeconds >= lockTtlSeconds);
    }
}