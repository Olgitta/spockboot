package com.olg.services.booking.utils;

import com.olg.services.booking.api.seats.SeatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpiredKeyListener {

    private static final Logger log = LoggerFactory.getLogger(ExpiredKeyListener.class);
    @Autowired
    private SeatsService seatsService;

    public void handleMessage(String expiredKey) {
        log.info("ExpiredKey handled: {}", expiredKey);

        try {
            if (expiredKey.startsWith("seat:lock:")) {
                // Extract event, venue, row, seat and unlock
                String[] parts = expiredKey.split(":");
                String[] seatParts = parts[2].split("_");
                Long eventId = Long.valueOf(seatParts[0]);
                Long venueId = Long.valueOf(seatParts[1]);
                String rowNumber = seatParts[2];
                String seatNumber = seatParts[3];

                seatsService.unlock(eventId, venueId, rowNumber, seatNumber);
            }
        } catch (Exception e) {
            log.error("Error on handling expiredKey: {} {}", expiredKey, e.getMessage());
        }

    }
}
