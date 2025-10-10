package com.olg.services.booking.api.seats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olg.core.dbsql.seats.Seat;
import com.olg.core.dbsql.seats.SeatRepository;
import com.olg.core.domain.enums.SeatStatus;
import com.olg.redis.service.RedisService;
import com.olg.services.booking.api.seats.dto.SeatsEventMapper;
import com.olg.services.booking.api.seats.dto.SeatResponse;
import com.olg.services.booking.exceptions.SeatErrorCodes;
import com.olg.services.booking.exceptions.SeatOperationException;
import com.olg.services.booking.utils.RedisKeyFactory;
import com.olg.services.booking.utils.RedisLuaScripts;
import com.olg.services.booking.utils.Validations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class SeatsService {

    private static final Logger log = LoggerFactory.getLogger(SeatsService.class);
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final SeatRepository seatRepository;
    private final String kafkaTopic;
    private final long lockTtlSeconds;

    public SeatsService(ObjectMapper objectMapper,
                        RedisService redisService,
                        SeatRepository seatRepository,
                        @Value("${app.kafka-topic}") String kafkaTopic,
                        @Value(("${app.seat-lock-ttl-seconds}")) long lockTtlSeconds) {
        this.objectMapper = objectMapper;
        this.redisService = redisService;
        this.seatRepository = seatRepository;
        this.kafkaTopic = kafkaTopic;
        this.lockTtlSeconds = lockTtlSeconds;
    }

    public void lock(Long eventId, Long venueId, String rowNumber, String seatNumber, String guestId) {
        String hashKey = RedisKeyFactory.reservationHashKey(eventId, venueId);
        String field = RedisKeyFactory.reservationHashField(rowNumber, seatNumber);
        String lockKey = RedisKeyFactory.lockKey(eventId, venueId, rowNumber, seatNumber);
        String timestamp = Instant.now().toString();
        String channelName = RedisKeyFactory.channelName(eventId, venueId);
        List<Object> messagePayload = List.of(SeatStatus.LOCKED, rowNumber, seatNumber, guestId);
        List<String> hashFieldValuePayload = List.of(timestamp, guestId);

        try {
            String channelMessage = objectMapper.writeValueAsString(messagePayload);
            String hashFieldValue = objectMapper.writeValueAsString(hashFieldValuePayload);
            // set and publish redis
            List<String> keys = Arrays.asList(lockKey, hashKey, field, channelName);
            List<String> args = Arrays.asList(
                    String.valueOf(lockTtlSeconds),
                    hashFieldValue,
                    channelMessage,
                    guestId
            );
            String result = redisService.executeLuaScript(
                    RedisLuaScripts.LUA_LOCK_SCRIPT,
                    keys,
                    args
            );

            if (!"OK".equals(result)) {
                throw new SeatOperationException(SeatErrorCodes.REDIS_SCRIPT_EXECUTION_ERROR,
                        "Lock failed: " + lockKey);
            }

            // todo: think about it:
            // send to kafka
//            BaseKafkaMessage<SeatReservationMessage> message = SeatKafkaMessageBuilder.buildLockMessage(eventId, venueId, rowNumber, seatNumber);
//            kafkaProducer.send(kafkaTopic, message);

        } catch (DataAccessException exception) {
            throw new SeatOperationException(SeatErrorCodes.REDIS_DATA_ACCESS_ERROR,
                    "Lock failed: " + exception.getMessage(), exception);
        } catch (JsonProcessingException exception) {
            throw new SeatOperationException(SeatErrorCodes.JSON_PROCESSING_ERROR,
                    "Lock failed: " + exception.getMessage(), exception);
        } catch (Exception e) {
            throw new SeatOperationException(SeatErrorCodes.SEAT_LOCK_FAILED,
                    "Lock failed: " + e.getMessage(), e);
        }
    }

    public void unlock(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        String hashKey = RedisKeyFactory.reservationHashKey(eventId, venueId);
        String field = RedisKeyFactory.reservationHashField(rowNumber, seatNumber);
        String lockKey = RedisKeyFactory.lockKey(eventId, venueId, rowNumber, seatNumber);
        String channelName = RedisKeyFactory.channelName(eventId, venueId);
        List<Object> messagePayload = List.of(SeatStatus.AVAILABLE, rowNumber, seatNumber);

        try {
            String channelMessage = objectMapper.writeValueAsString(messagePayload);
            // set and publish redis
            List<String> keys = Arrays.asList(lockKey, hashKey, field, channelName);
            List<String> args = Arrays.asList(
                    channelMessage
            );
            String result = redisService.executeLuaScript(
                    RedisLuaScripts.LUA_UNLOCK_SCRIPT,
                    keys,
                    args
            );

            if (!"OK".equals(result)) {
                throw new SeatOperationException(SeatErrorCodes.REDIS_SCRIPT_EXECUTION_ERROR,
                        "Unlock failed: " + lockKey);
            }

            // todo: think about it:
            // send to kafka
//            BaseKafkaMessage<SeatReservationMessage> message = SeatKafkaMessageBuilder.buildUnlockMessage(eventId, venueId, rowNumber, seatNumber);
//            kafkaProducer.send(kafkaTopic, message);

        } catch (DataAccessException exception) {
            throw new SeatOperationException(SeatErrorCodes.REDIS_DATA_ACCESS_ERROR,
                    "Unlock failed: " + exception.getMessage(), exception);
        } catch (JsonProcessingException exception) {
            throw new SeatOperationException(SeatErrorCodes.JSON_PROCESSING_ERROR,
                    "Unlock failed: " + exception.getMessage(), exception);
        } catch (Exception e) {
            throw new SeatOperationException(SeatErrorCodes.SEAT_UNLOCK_FAILED,
                    "Unlock failed: " + e.getMessage(), e);
        }
    }

    public List<SeatResponse> getSeats(Long eventId, Long venueId) {
        String lockedHashKey = RedisKeyFactory.reservationHashKey(eventId, venueId);
        String bookedHashKey = RedisKeyFactory.bookingHashKey(eventId, venueId);
        List<Seat> all;

        try {
            all = seatRepository.findByVenueId(venueId);
        } catch (DataAccessException exception) {
            throw new SeatOperationException(SeatErrorCodes.DATABASE_ACCESS_ERROR,
                    "Failed to retrieve seats: " + exception.getMessage(), exception);
        } catch (Exception e) {
            throw new SeatOperationException(SeatErrorCodes.SEAT_DATA_RETRIEVAL_FAILED,
                    "Failed to retrieve seats: " + e.getMessage(), e);
        }

        try {
            Map<String, String> locked = validateLockedHash(lockedHashKey, redisService.hgetall(lockedHashKey), eventId, venueId);
            Map<String, String> booked = redisService.hgetall(bookedHashKey);
            return SeatsEventMapper.map(all, locked, booked);
        } catch (DataAccessException exception) {
            throw new SeatOperationException(SeatErrorCodes.REDIS_DATA_ACCESS_ERROR,
                    "Failed to retrieve seats: " + exception.getMessage(), exception);
        } catch (Exception e) {
            throw new SeatOperationException(SeatErrorCodes.SEAT_DATA_RETRIEVAL_FAILED,
                    "Failed to retrieve seats: " + e.getMessage(), e);
        }

    }

    private Map<String, String> validateLockedHash(String lockedHashKey, Map<String, String> locked, Long eventId, Long venueId) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : locked.entrySet()) {
            List<String> valuePayload = null;
            try {
                valuePayload = objectMapper.readValue(entry.getValue(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            boolean expired = Validations.isExpired(valuePayload.get(0), lockTtlSeconds);
            if (expired) {
                RedisKeyFactory.SeatKeyDetails keyDetails = RedisKeyFactory.parseReservationField(entry.getKey(), eventId, venueId);
                unlock(eventId, venueId, keyDetails.rowNumber(), keyDetails.seatNumber());
                log.debug("validateLockedHash, expired found: {}", keyDetails);
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
