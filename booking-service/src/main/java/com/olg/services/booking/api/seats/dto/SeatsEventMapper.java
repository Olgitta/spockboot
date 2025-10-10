package com.olg.services.booking.api.seats.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olg.core.dbsql.seats.Seat;
import com.olg.core.domain.enums.SeatStatus;
import com.olg.services.booking.utils.RedisKeyFactory;

import java.util.List;
import java.util.Map;

public class SeatsEventMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SeatResponse map(Seat seat, SeatStatus statusId, String guestId) {
        SeatResponse response = new SeatResponse();

        response.setId(seat.getId());
        response.setRowNumber(seat.getRowNumber());
        response.setSeatNumber(seat.getSeatNumber());
        response.setStatusId(statusId);
        response.setGuestId(guestId);

        return response;
    }

    public static List<SeatResponse> map(List<Seat> seatList,
                                         Map<String, String> lockedSeats,
                                         Map<String, String> bookedSeats) {
        return seatList.stream()
                .map((seat) -> {
                    Long id = seat.getId();

                    String lockedKey = RedisKeyFactory.reservationHashField(
                            seat.getRowNumber(),
                            seat.getSeatNumber()
                    );
                    String bookedKey = RedisKeyFactory.bookingHashField(
                            seat.getRowNumber(),
                            seat.getSeatNumber()
                    );

                    SeatStatus statusId = null;
                    String guestId = null;

                    try {
                        // 1. Проверяем, забронировано ли место (наивысший приоритет)
                        if (bookedSeats.get(bookedKey) != null) {
                            List<String> valuePayload = objectMapper.readValue(bookedSeats.get(bookedKey), new TypeReference<>() {
                            });
                            guestId = valuePayload.get(1);
                            statusId = SeatStatus.BOOKED;
                        }
                        // 2. Если не забронировано, проверяем, заблокировано ли место
                        else if (lockedSeats.get(lockedKey) != null) {
                            List<String> valuePayload = objectMapper.readValue(lockedSeats.get(lockedKey), new TypeReference<>() {
                            });
                            guestId = valuePayload.get(1);
                            statusId = SeatStatus.LOCKED;
                        }
                        // 3. Если ни то, ни другое, место доступно
                        else {
                            statusId = SeatStatus.AVAILABLE;
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e.getMessage());
                    }

                    return SeatsEventMapper.map(seat, statusId, guestId);
                })
                .toList();
    }
}
