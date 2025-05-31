package com.olg.services.booking.utils;

import com.olg.domain.dto.SeatReservationMessage;
import com.olg.domain.enums.SeatReservationEvent;
import com.olg.kafka.BaseKafkaMessage;

public class SeatKafkaMessageBuilder {

    public static BaseKafkaMessage<SeatReservationMessage> buildLockMessage(Long eventId,
                                                                            Long venueId,
                                                                            String rowNumber,
                                                                            String seatNumber) {

        return new BaseKafkaMessage<>(SeatReservationEvent.LOCK.name(),
                new SeatReservationMessage(eventId,
                        venueId,
                        rowNumber,
                        seatNumber));
    }

    public static BaseKafkaMessage<SeatReservationMessage> buildUnlockMessage(Long eventId,
                                                                              Long venueId,
                                                                              String rowNumber,
                                                                              String seatNumber) {

        return new BaseKafkaMessage<>(SeatReservationEvent.UNLOCK.name(),
                new SeatReservationMessage(eventId,
                        venueId,
                        rowNumber,
                        seatNumber));
    }
}
