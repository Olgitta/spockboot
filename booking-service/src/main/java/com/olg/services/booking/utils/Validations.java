package com.olg.services.booking.utils;

import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Validations {

    public static boolean isExpired(String timestampString, long lockTtlSeconds) {
        Instant now = Instant.now();
        // Парсим временную метку, когда была сделана блокировка
        Instant lockTimestamp = Instant.parse(timestampString);
        // Считаем, сколько времени прошло с момента блокировки
        long elapsedSeconds = ChronoUnit.SECONDS.between(lockTimestamp, now);

        return (elapsedSeconds >= lockTtlSeconds);
    }
}
