package com.olg.services.booking.api.seats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olg.kafka.producers.GenericKafkaProducer;
import com.olg.mysql.seats.Seat;
import com.olg.mysql.seats.SeatRepository;

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
    @Mock private GenericKafkaProducer kafkaProducer;

//    @InjectMocks
    private SeatsService seatsService;

    @BeforeEach
    void setUp() {
        seatsService = new SeatsService(
                objectMapper,
                redisService,
                seatRepository,
                kafkaProducer,
                "kafka-topic",
                120L
        );
    }

    @Test
    void lock_successfulRedisCall_doesNotThrow() throws Exception {
        Long eventId = 1L, venueId = 2L;
        String row = "A", seat = "1";
        String jsonPayload = "[\"LOCKED\",\"A\",\"1\"]";

//        when(objectMapper.writeValueAsString(any())).thenReturn(jsonPayload);
        when(redisService.executeLuaScript(any(), anyList(), anyList())).thenReturn("OK");

        assertDoesNotThrow(() -> seatsService.lock(eventId, venueId, row, seat));
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
    void getSeats_successfulReturnsMappedSeats() {
        Long eventId = 1L, venueId = 2L;

        List<Seat> seats = List.of(new Seat(/* mock your seat here */));
        Map<String, String> locked = Map.of("A:1", Instant.now().toString());
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
    void validateLockedHash_skipsExpiredEntries() {
        Map<String, String> input = Map.of(
                "A_1", Instant.now().minusSeconds(200).toString(),
                "A_2", Instant.now().toString()
        );

        when(redisService.executeLuaScript(any(), anyList(), anyList())).thenReturn("OK");

        Map<String, String> result = ReflectionTestUtils.invokeMethod(
                seatsService, "validateLockedHash", "hashKey", input, 1L, 2L
        );

        assertTrue(result.containsKey("A_2"));
        assertFalse(result.containsKey("A_1"));
    }
}
