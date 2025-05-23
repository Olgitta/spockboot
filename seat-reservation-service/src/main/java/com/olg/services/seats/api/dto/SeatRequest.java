package com.olg.services.seats.api.dto;

public record SeatRequest(Long eventId, Long venueId, String rowNumber, String seatNumber) {}
