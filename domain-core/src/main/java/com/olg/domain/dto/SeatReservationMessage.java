package com.olg.domain.dto;

public record SeatReservationMessage(Long eventId,
                                     Long venueId,
                                     String rowNumber,
                                     String seatNumber) {
}
