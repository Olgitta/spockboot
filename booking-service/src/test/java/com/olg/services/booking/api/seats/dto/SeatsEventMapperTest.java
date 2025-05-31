package com.olg.services.booking.api.seats.dto;

import com.olg.domain.enums.SeatStatus;
import com.olg.mysql.seats.Seat;

import com.olg.services.booking.api.seats.dto.SeatResponse;
import com.olg.services.booking.api.seats.dto.SeatsEventMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//@ExtendWith(MockitoExtension.class)
class SeatsEventMapperTest {

    // Тест для метода map(Seat seat, SeatStatus statusId)
    @Test
    @DisplayName("Should map a single Seat to SeatResponse with given status")
    void shouldMapSingleSeatToSeatResponse() {
        // Given
        Seat seat = createSeat("A", "1", 1L);
        SeatStatus status = SeatStatus.BOOKED;

        // When
        SeatResponse response = SeatsEventMapper.map(seat, status);

        // Then
        assertNotNull(response);
        assertEquals("A", response.getRowNumber());
        assertEquals("1", response.getSeatNumber());
        assertEquals(SeatStatus.BOOKED, response.getStatusId());
    }


    // Тесты для метода map(List<Seat> seatList, Map<String, String> lockedSeats, Map<String, String> bookedSeats)
    @Test
    @DisplayName("Should map a list of seats with all AVAILABLE status")
    void shouldMapAllSeatsToAvailableStatus() {
        // Given
        Seat seatA1 = createSeat("A", "1", 1L);
        Seat seatA2 = createSeat("A", "2", 1L);
        List<Seat> seatList = Arrays.asList(seatA1, seatA2);

        Map<String, String> lockedSeats = Collections.emptyMap(); // Нет заблокированных мест
        Map<String, String> bookedSeats = Collections.emptyMap(); // Нет забронированных мест

//        // Mock RedisKeyFactory static methods for hash fields
//        // Мы мокаем только те методы, которые используются в SeatsEventMapper.map(List, Map, Map)
//        try (MockedStatic<RedisKeyFactory> mockedStatic = Mockito.mockStatic(RedisKeyFactory.class)) {
//            mockedStatic.when(() -> RedisKeyFactory.reservationHashField("A", "1"))
//                    .thenReturn("A_1"); // Возвращаем только поле хэша
//            mockedStatic.when(() -> RedisKeyFactory.bookingHashField("A", "1"))
//                    .thenReturn("A_1"); // Возвращаем только поле хэша
//            mockedStatic.when(() -> RedisKeyFactory.reservationHashField("A", "2"))
//                    .thenReturn("A_2"); // Возвращаем только поле хэша
//            mockedStatic.when(() -> RedisKeyFactory.bookingHashField("A", "2"))
//                    .thenReturn("A_2"); // Возвращаем только поле хэша

            // When
            List<SeatResponse> result = SeatsEventMapper.map(seatList, lockedSeats, bookedSeats);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            SeatResponse expected = createSeatResponse("A", "1", SeatStatus.AVAILABLE);
            assertEquals(expected, result.get(0));
            expected = createSeatResponse("A", "2", SeatStatus.AVAILABLE);
            assertEquals(expected, result.get(1));
    }

    @Test
    @DisplayName("Should map a list of seats with some LOCKED status")
    void shouldMapSeatsToLockedStatus() {
        // Given
        Seat seatA1 = createSeat("A", "1", 1L);
        Seat seatA2 = createSeat("A", "2", 1L);
        List<Seat> seatList = Arrays.asList(seatA1, seatA2);

        Map<String, String> lockedSeats = new HashMap<>();
        lockedSeats.put("A_1", "some_lock_token"); // Ключ в Map - это поле хэша
        Map<String, String> bookedSeats = Collections.emptyMap();

//        try (MockedStatic<RedisKeyFactory> mockedStatic = Mockito.mockStatic(RedisKeyFactory.class)) {
//            mockedStatic.when(() -> RedisKeyFactory.reservationHashField("A", "1"))
//                    .thenReturn("A_1");
//            mockedStatic.when(() -> RedisKeyFactory.bookingHashField("A", "1"))
//                    .thenReturn("A_1");
//            mockedStatic.when(() -> RedisKeyFactory.reservationHashField("A", "2"))
//                    .thenReturn("A_2");
//            mockedStatic.when(() -> RedisKeyFactory.bookingHashField("A", "2"))
//                    .thenReturn("A_2");

            // When
            List<SeatResponse> result = SeatsEventMapper.map(seatList, lockedSeats, bookedSeats);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            SeatResponse expected = createSeatResponse("A", "1", SeatStatus.LOCKED);
            assertEquals(expected, result.get(0));
            expected = createSeatResponse("A", "2", SeatStatus.AVAILABLE);
            assertEquals(expected, result.get(1));
    }

    @Test
    @DisplayName("Should map a list of seats with some BOOKED status (highest priority)")
    void shouldMapSeatsToBookedStatus() {
        // Given
        Seat seatA1 = createSeat("A", "1", 1L);
        Seat seatA2 = createSeat("A", "2", 1L);
        List<Seat> seatList = Arrays.asList(seatA1, seatA2);

        Map<String, String> lockedSeats = new HashMap<>();
        lockedSeats.put("A_1", "some_lock_token"); // Место A1 заблокировано
        lockedSeats.put("A_2", "some_lock_token"); // Место A2 заблокировано

        Map<String, String> bookedSeats = new HashMap<>();
        bookedSeats.put("A_1", "some_booking_ref"); // Место A1 забронировано (должно иметь приоритет)

//        try (MockedStatic<RedisKeyFactory> mockedStatic = Mockito.mockStatic(RedisKeyFactory.class)) {
//            mockedStatic.when(() -> RedisKeyFactory.reservationHashField("A", "1"))
//                    .thenReturn("A_1");
//            mockedStatic.when(() -> RedisKeyFactory.bookingHashField("A", "1"))
//                    .thenReturn("A_1");
//            mockedStatic.when(() -> RedisKeyFactory.reservationHashField("A", "2"))
//                    .thenReturn("A_2");
//            mockedStatic.when(() -> RedisKeyFactory.bookingHashField("A", "2"))
//                    .thenReturn("A_2");

            // When
            List<SeatResponse> result = SeatsEventMapper.map(seatList, lockedSeats, bookedSeats);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            SeatResponse expected = createSeatResponse("A", "1", SeatStatus.BOOKED);
            assertEquals(expected, result.get(0));
            expected = createSeatResponse("A", "2", SeatStatus.LOCKED);
            assertEquals(expected, result.get(1));

    }

    @Test
    @DisplayName("Should handle empty seat list gracefully")
    void shouldHandleEmptySeatList() {
        // Given
        List<Seat> seatList = Collections.emptyList();
        Map<String, String> lockedSeats = new HashMap<>();
        Map<String, String> bookedSeats = new HashMap<>();

        // No need to mock RedisKeyFactory for empty list as map method won't be called
        // for each seat.

        // When
        List<SeatResponse> result = SeatsEventMapper.map(seatList, lockedSeats, bookedSeats);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private Seat createSeat(String rowNumber, String seatNumber, long venueId) {
        Seat seat = new Seat();
        seat.setRowNumber(rowNumber);
        seat.setSeatNumber(seatNumber);
        seat.setVenueId(venueId);

        return seat;
    }

    private SeatResponse createSeatResponse(String rowNumber, String seatNumber, SeatStatus seatStatus) {
        SeatResponse seatResponse = new SeatResponse();
        seatResponse.setRowNumber(rowNumber);
        seatResponse.setSeatNumber(seatNumber);
        seatResponse.setStatusId(seatStatus);
        return seatResponse;
    }

}