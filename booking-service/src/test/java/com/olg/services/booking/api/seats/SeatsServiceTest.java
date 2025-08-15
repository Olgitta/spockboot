package com.olg.services.booking.api.seats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olg.postgressql.seats.Seat;
import com.olg.postgressql.seats.SeatRepository;

import com.olg.redis.service.RedisService;
import com.olg.services.booking.api.seats.dto.SeatResponse;
import com.olg.services.booking.exceptions.SeatErrorCodes;
import com.olg.services.booking.exceptions.SeatOperationException;

import com.olg.services.booking.utils.RedisKeyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
// Specific DataAccessException example
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatsServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock private RedisService redisService;
    @Mock private SeatRepository seatRepository;

//    @InjectMocks
    private SeatsService seatsService;

    @BeforeEach
    void setUp() {
        seatsService = new SeatsService(
                objectMapper,
                redisService,
                seatRepository,
                "kafka-topic",
                120L
        );
    }

    @Test
    void lock_successfulRedisCall_doesNotThrow() throws Exception {
        Long eventId = 1L, venueId = 2L;
        String row = "A", seat = "1", guestId = "abc";
        String jsonPayload = "[\"LOCKED\",\"A\",\"1\"]";

//        when(objectMapper.writeValueAsString(any())).thenReturn(jsonPayload);
        when(redisService.executeLuaScript(any(), anyList(), anyList())).thenReturn("OK");

        assertDoesNotThrow(() -> seatsService.lock(eventId, venueId, row, seat, guestId));
    }

//    @Test
//    void lock_redisFails_throwsSeatOperationException() throws Exception {
//        Long eventId = 1L, venueId = 2L;
//        String row = "B", seat = "2";
////        when(objectMapper.writeValueAsString(any())).thenReturn("msg");
//        when(redisService.executeLuaScript(any(), anyList(), anyList())).thenReturn("FAIL");
//
//        SeatOperationException ex = assertThrows(SeatOperationException.class, () ->
//                seatsService.lock(eventId, venueId, row, seat)
//        );
//
//        assertEquals(SeatErrorCodes.REDIS_SCRIPT_EXECUTION_ERROR.getCode(), ex.getErrorCode());
//    }

    @Test
    void unlock_successfulRedisCall_doesNotThrow() throws Exception {
        Long eventId = 1L, venueId = 2L;
        String row = "A", seat = "1";
        String jsonPayload = "[\"AVAILABLE\",\"A\",\"1\"]";

//        when(objectMapper.writeValueAsString(any())).thenReturn(jsonPayload);
        when(redisService.executeLuaScript(any(), anyList(), anyList())).thenReturn("OK");

        assertDoesNotThrow(() -> seatsService.unlock(eventId, venueId, row, seat));
    }

    @Test
    void getSeats_successfulReturnsMappedSeats() throws JsonProcessingException {
        Long eventId = 1L, venueId = 2L;

        List<Seat> seats = List.of(new Seat(/* mock your seat here */));

        List<String> hashFieldValuePayload2 = List.of(Instant.now().toString(), "id");
        String hashFieldValue2 = objectMapper.writeValueAsString(hashFieldValuePayload2);

        Map<String, String> locked = Map.of("A:1", hashFieldValue2);
        Map<String, String> booked = Map.of();

        String lockedHashKey = RedisKeyFactory.reservationHashKey(eventId, venueId);
        String bookedHashKey = RedisKeyFactory.bookingHashKey(eventId, venueId);

        when(seatRepository.findByVenueId(venueId)).thenReturn(seats);
        when(redisService.hgetall(startsWith(lockedHashKey))).thenReturn(locked);
        when(redisService.hgetall(startsWith(bookedHashKey))).thenReturn(booked);

        List<SeatResponse> result = seatsService.getSeats(eventId, venueId);

        assertNotNull(result);
    }

    @Test
    void getSeats_databaseAccessError_throwsSeatOperationException() {
        when(seatRepository.findByVenueId(any())).thenThrow(new DataAccessException("DB failed") {});

        SeatOperationException ex = assertThrows(SeatOperationException.class, () ->
                seatsService.getSeats(1L, 1L)
        );

        assertEquals(SeatErrorCodes.DATABASE_ACCESS_ERROR.getCode(), ex.getErrorCode());
    }

    @Test
    void getSeats_redisFails_throwsSeatOperationException() {
        when(seatRepository.findByVenueId(any())).thenReturn(List.of());
        when(redisService.hgetall(anyString())).thenThrow(new DataAccessException("Redis error") {});

        SeatOperationException ex = assertThrows(SeatOperationException.class, () ->
                seatsService.getSeats(1L, 1L)
        );

        assertEquals(SeatErrorCodes.REDIS_DATA_ACCESS_ERROR.getCode(), ex.getErrorCode());
    }

    @Test
    void validateLockedHash_skipsExpiredEntries() throws JsonProcessingException {
        List<String> hashFieldValuePayload1 = List.of(Instant.now().minusSeconds(200).toString(), "id");
        String hashFieldValue1 = objectMapper.writeValueAsString(hashFieldValuePayload1);

        List<String> hashFieldValuePayload2 = List.of(Instant.now().toString(), "id");
        String hashFieldValue2 = objectMapper.writeValueAsString(hashFieldValuePayload2);

        Map<String, String> input = Map.of(
                "A_1", hashFieldValue1,
                "A_2", hashFieldValue2
        );

        when(redisService.executeLuaScript(any(), anyList(), anyList())).thenReturn("OK");

        Map<String, String> result = ReflectionTestUtils.invokeMethod(
                seatsService, "validateLockedHash", "hashKey", input, 1L, 2L
        );

        assertTrue(result.containsKey("A_2"));
        assertFalse(result.containsKey("A_1"));
    }
}
