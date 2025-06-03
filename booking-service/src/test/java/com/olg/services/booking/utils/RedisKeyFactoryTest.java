package com.olg.services.booking.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RedisKeyFactory Tests") // Это будет красиво отображаться в отчетах
class RedisKeyFactoryTest {

    private static final Long TEST_EVENT_ID = 123L;
    private static final Long TEST_VENUE_ID = 456L;
    private static final String TEST_ROW_NUMBER = "A";
    private static final String TEST_SEAT_NUMBER = "7";

    // --- Тесты для методов формирования ключей ---

    @Test
    @DisplayName("Should create correct reservation hash key")
    void shouldCreateCorrectReservationHashKey() {
        String expectedKey = "seats:reservation:123:456";
        String actualKey = RedisKeyFactory.reservationHashKey(TEST_EVENT_ID, TEST_VENUE_ID);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    @DisplayName("Should create correct reservation hash field")
    void shouldCreateCorrectReservationHashField() {
        String expectedField = "A_7";
        String actualField = RedisKeyFactory.reservationHashField(TEST_ROW_NUMBER, TEST_SEAT_NUMBER);
        assertEquals(expectedField, actualField);
    }

    @Test
    @DisplayName("Should create correct booking hash key")
    void shouldCreateCorrectBookingHashKey() {
        String expectedKey = "seats:booking:123:456";
        String actualKey = RedisKeyFactory.bookingHashKey(TEST_EVENT_ID, TEST_VENUE_ID);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    @DisplayName("Should create correct booking hash field")
    void shouldCreateCorrectBookingHashField() {
        String expectedField = "A_7";
        String actualField = RedisKeyFactory.bookingHashField(TEST_ROW_NUMBER, TEST_SEAT_NUMBER);
        assertEquals(expectedField, actualField);
    }

    @Test
    @DisplayName("Should create correct lock key")
    void shouldCreateCorrectLockKey() {
        String expectedKey = "seat:lock:123_456_A_7";
        String actualKey = RedisKeyFactory.lockKey(TEST_EVENT_ID, TEST_VENUE_ID, TEST_ROW_NUMBER, TEST_SEAT_NUMBER);
        assertEquals(expectedKey, actualKey);
    }

    @Test
    @DisplayName("Should create correct channel name")
    void shouldCreateCorrectChannelName() {
        String expectedChannel = "seat:events:123_456";
        String actualChannel = RedisKeyFactory.channelName(TEST_EVENT_ID, TEST_VENUE_ID);
        assertEquals(expectedChannel, actualChannel);
    }

    // --- Тесты для методов парсинга ---

    @Test
    @DisplayName("parseLockKey: Should parse valid lock key successfully")
    void parseLockKey_shouldParseValidKey() {
        String lockKey = "seat:lock:123_456_A_7";
        RedisKeyFactory.SeatKeyDetails expectedDetails = new RedisKeyFactory.SeatKeyDetails(TEST_EVENT_ID, TEST_VENUE_ID, TEST_ROW_NUMBER, TEST_SEAT_NUMBER);
        RedisKeyFactory.SeatKeyDetails actualDetails = RedisKeyFactory.parseLockKey(lockKey);

        assertNotNull(actualDetails);
        assertEquals(expectedDetails, actualDetails); // Record's equals() method handles this nicely
    }

    @Test
    @DisplayName("parseLockKey: Should return null for invalid lock key format")
    void parseLockKey_shouldReturnNullForInvalidFormat() {
        assertNull(RedisKeyFactory.parseLockKey("invalid:key:format"));
        assertNull(RedisKeyFactory.parseLockKey("seat:lock:123_A")); // Missing parts
        assertNull(RedisKeyFactory.parseLockKey("seat:lock:abc_456_A_7")); // Invalid numbers
    }

    @Test
    @DisplayName("parseReservationField: Should parse valid reservation field successfully")
    void parseReservationField_shouldParseValidField() {
        String field = "A_7";
        // Здесь мы передаем eventId и venueId, так как они не парсятся из field
        RedisKeyFactory.SeatKeyDetails expectedDetails = new RedisKeyFactory.SeatKeyDetails(TEST_EVENT_ID, TEST_VENUE_ID, TEST_ROW_NUMBER, TEST_SEAT_NUMBER);
        RedisKeyFactory.SeatKeyDetails actualDetails = RedisKeyFactory.parseReservationField(field, TEST_EVENT_ID, TEST_VENUE_ID);

        assertNotNull(actualDetails);
        assertEquals(expectedDetails, actualDetails);
    }

    @Test
    @DisplayName("parseReservationField: Should return null for invalid reservation field format")
    void parseReservationField_shouldReturnNullForInvalidFormat() {
        assertNull(RedisKeyFactory.parseReservationField("A", TEST_EVENT_ID, TEST_VENUE_ID)); // Missing part
        assertNull(RedisKeyFactory.parseReservationField("A_7_extra", TEST_EVENT_ID, TEST_VENUE_ID)); // Extra part
        assertNull(RedisKeyFactory.parseReservationField("_", TEST_EVENT_ID, TEST_VENUE_ID)); // Empty parts
    }

}