package com.olg.services.booking.utils;

import com.olg.services.booking.api.seats.SeatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.olg.services.booking.utils.RedisKeyFactory.parseLockKey;

@Component
public class ExpiredKeyListener {

    private static final Logger log = LoggerFactory.getLogger(ExpiredKeyListener.class);
    @Autowired
    private SeatsService seatsService;

    public void handleMessage(String expiredKey) {

        try {
            RedisKeyFactory.SeatKeyDetails keyDetails = parseLockKey(expiredKey);
            log.info("ExpiredKey handled: {} keyDetails {}", expiredKey, keyDetails);

            if (keyDetails != null) {
                seatsService.unlock(keyDetails.eventId(),
                        keyDetails.venueId(),
                        keyDetails.rowNumber(),
                        keyDetails.seatNumber());
            }
        } catch (Exception e) {
            log.error("Failed on handleMessage for key: {}", expiredKey, e);
        }

    }
}
