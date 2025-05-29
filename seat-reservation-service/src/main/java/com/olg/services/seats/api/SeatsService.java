package com.olg.services.seats.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olg.domain.dto.SeatReservationMessage;
import com.olg.domain.enums.SeatStatus;
import com.olg.kafka.BaseKafkaMessage;
import com.olg.kafka.producers.GenericKafkaProducer;
import com.olg.mysql.seats.Seat;
import com.olg.mysql.seats.SeatRepository;
import com.olg.redis.service.RedisService;
import com.olg.services.seats.api.dto.SeatsEventMapper;
import com.olg.services.seats.api.dto.SeatResponse;
import com.olg.services.seats.exceptions.SeatAlreadyLockedException;
import com.olg.services.seats.exceptions.SeatUnlockException;
import com.olg.services.seats.utils.RedisKeyFactory;
import com.olg.services.seats.utils.RedisLuaScripts;
import com.olg.services.seats.utils.SeatKafkaMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SeatsService {

    private static final Logger log = LoggerFactory.getLogger(SeatsService.class);
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final SeatRepository seatRepository;
    private final GenericKafkaProducer kafkaProducer;
    @Value("${app.kafka-topic}")
    private String kafkaTopic;
    @Value(("${app.seat-lock-ttl-seconds}"))
    private String lockTtlSeconds;

    public SeatsService(ObjectMapper objectMapper, RedisService redisService,
                        SeatRepository seatRepository,
                        GenericKafkaProducer kafkaProducer) {
        this.objectMapper = objectMapper;
        this.redisService = redisService;
        this.seatRepository = seatRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void lock(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        String hashKey = RedisKeyFactory.reservationHashKey(eventId, venueId);
        String field = RedisKeyFactory.reservationHashField(rowNumber, seatNumber);
        String lockKey = RedisKeyFactory.lockKey(eventId, venueId, rowNumber, seatNumber);
        String timestamp = Instant.now().toString();
        String channelName = RedisKeyFactory.channelName(eventId, venueId);
        List<Object> messagePayload = List.of(SeatStatus.LOCKED, rowNumber, seatNumber);

        try {
            String channelMessage = objectMapper.writeValueAsString(messagePayload);
            // set and publish redis
            List<String> keys = Arrays.asList(lockKey, hashKey, field, channelName);
            List<String> args = Arrays.asList(
                    lockTtlSeconds,
                    timestamp,
                    channelMessage
            );
            String result = redisService.executeLuaScript(
                    RedisLuaScripts.LUA_LOCK_SCRIPT,
                    keys,
                    args
            );

            if (!"OK".equals(result)) {
                throw new SeatAlreadyLockedException(lockKey);
            }

            // send to kafka
            BaseKafkaMessage<SeatReservationMessage> message = SeatKafkaMessageBuilder.buildLockMessage(eventId, venueId, rowNumber, seatNumber);
            kafkaProducer.send(kafkaTopic, message);

        } catch (DataAccessException | JsonProcessingException exception) {
            log.error("Error on lock lockKey, hashKey: {}, {}, error: {}",
                    lockKey, hashKey, exception.getMessage());
            // todo: throw exception;
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
                throw new SeatUnlockException(lockKey);
            }

            // send to kafka
            BaseKafkaMessage<SeatReservationMessage> message = SeatKafkaMessageBuilder.buildUnlockMessage(eventId, venueId, rowNumber, seatNumber);
            kafkaProducer.send(kafkaTopic, message);

        }  catch (DataAccessException | JsonProcessingException exception) {
            log.error("Error on unlock lockKey: {}, hashKey: {}, error: {}",
                    lockKey, hashKey, exception.getMessage());
            // todo: throw exception;
        }
    }

    public List<SeatResponse> getSeats(Long eventId, Long venueId) {
        String lockedHashKey = RedisKeyFactory.reservationHashKey(eventId, venueId);
        String bookedHashKey = RedisKeyFactory.bookingHashKey(eventId, venueId);
        List<Seat> all = seatRepository.findByVenueId(venueId);
        Map<String, String> locked = redisService.hgetall(lockedHashKey);
        Map<String, String> booked = redisService.hgetall(bookedHashKey);
        return SeatsEventMapper.map(all, locked, booked);
    }
}
