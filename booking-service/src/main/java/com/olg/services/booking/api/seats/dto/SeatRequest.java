package com.olg.services.booking.api.seats.dto;

public record SeatRequest(
        Long id,
        Long eventId,
        Long venueId,
        String rowNumber,
        String seatNumber,
        String guestId) {}
