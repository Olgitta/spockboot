package com.olg.services.booking.api.seats.dto;

public record SeatRequest(Long eventId, Long venueId, String rowNumber, String seatNumber, String lockerId) {}
