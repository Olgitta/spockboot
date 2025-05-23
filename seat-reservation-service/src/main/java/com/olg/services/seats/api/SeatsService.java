package com.olg.services.seats.api;

import com.olg.mysql.seats.Seat;
import com.olg.mysql.seats.SeatRepository;
import com.olg.mysql.seats.SeatsEventRepository;
import com.olg.redis.service.RedisService;
import com.olg.services.seats.api.dto.SeatsEventMapper;
import com.olg.services.seats.api.dto.SeatResponse;
import com.olg.services.seats.exceptions.SeatAlreadyLockedException;
import com.olg.services.seats.exceptions.SeatUnlockException;
import com.olg.services.seats.utils.RedisKeyFactory;
import com.olg.services.seats.utils.RedisLuaScripts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SeatsService {

    private static final Logger log = LoggerFactory.getLogger(SeatsService.class);
    private final RedisService redisService;
    private SeatsEventRepository seatsEventRepository;
    private SeatRepository seatRepository;

    public SeatsService(RedisService redisService,
                        SeatsEventRepository seatsEventRepository,
                        SeatRepository seatRepository) {
        this.redisService = redisService;
        this.seatsEventRepository = seatsEventRepository;
        this.seatRepository = seatRepository;
    }

    public void lock(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        String hashKey = RedisKeyFactory.reservationKey(eventId);
        String field = RedisKeyFactory.reservationField(venueId, rowNumber, seatNumber);
        String lockKey = RedisKeyFactory.lockKey(eventId, venueId, rowNumber, seatNumber);
        String timestamp = Instant.now().toString();
        String channelName = RedisKeyFactory.channel(eventId, "lock");
        String channelMessage = lockKey;

        try {
            List<String> keys = Arrays.asList(lockKey, hashKey, field, channelName);
            List<String> args = Arrays.asList(
                    "2",  // seconds
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
            // todo: send to kafka
        } catch (DataAccessException dataAccessException) {
            log.error("Error on lock lockKey, hashKey: {}, {}, error: {}",
                    lockKey, hashKey, dataAccessException.getMessage());
            throw dataAccessException;
        }
    }

    public void unlock(Long eventId, Long venueId, String rowNumber, String seatNumber) {
        String hashKey = RedisKeyFactory.reservationKey(eventId);
        String field = RedisKeyFactory.reservationField(venueId, rowNumber, seatNumber);
        String lockKey = RedisKeyFactory.lockKey(eventId, venueId, rowNumber, seatNumber);
        String channelName = RedisKeyFactory.channel(eventId, "unlock");
        String channelMessage = lockKey;

        try {
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
            // todo: send to kafka
        } catch (DataAccessException dataAccessException) {
            log.error("Error on unlock lockKey, hashKey: {}, {}, error: {}",
                    lockKey, hashKey, dataAccessException.getMessage());
            throw dataAccessException;
        }
    }

//    public List<SeatResponse> getSeats(Long eventId, Long venueId) {
//        List<SeatsEvent> found = seatsEventRepository.findAllByEventIdAndVenueId(eventId, venueId);
//
//        return SeatsEventMapper.map(found);
//    }

    public List<SeatResponse> getSeats(Long eventId, Long venueId) {
        String hashKey = RedisKeyFactory.reservationKey(eventId);
        List<Seat> all = seatRepository.findByVenueId(venueId);
        Map<String, String> locked = redisService.hgetall(hashKey);
        return SeatsEventMapper.map(all, locked);
    }
}
