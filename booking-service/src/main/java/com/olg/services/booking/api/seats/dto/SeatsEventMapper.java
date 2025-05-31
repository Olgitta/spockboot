package com.olg.services.booking.api.seats.dto;

import com.olg.domain.enums.SeatStatus;
import com.olg.mysql.seats.Seat;
import com.olg.services.booking.utils.RedisKeyFactory;

import java.util.List;
import java.util.Map;

public class SeatsEventMapper {

    public static SeatResponse map(Seat seat, SeatStatus statusId) {
        SeatResponse response = new SeatResponse();

        response.setRowNumber(seat.getRowNumber());
        response.setSeatNumber(seat.getSeatNumber());
        response.setStatusId(statusId);

        return response;
    }

    public static List<SeatResponse> map(List<Seat> seatList, Map<String, String> lockedSeats, Map<String, String> bookedSeats) {
        return seatList.stream()
                .map((seat) -> {
                    String lockedKey = RedisKeyFactory.reservationHashField(
                            seat.getRowNumber(),
                            seat.getSeatNumber()
                    );
                    String bookedKey = RedisKeyFactory.bookingHashField(
                            seat.getRowNumber(),
                            seat.getSeatNumber()
                    );

                    SeatStatus statusId;

                    // 1. Проверяем, забронировано ли место (наивысший приоритет)
                    if (bookedSeats.get(bookedKey) != null) {
                        statusId = SeatStatus.BOOKED;
                    }
                    // 2. Если не забронировано, проверяем, заблокировано ли место
                    else if (lockedSeats.get(lockedKey) != null) {
                        statusId = SeatStatus.LOCKED;
                    }
                    // 3. Если ни то, ни другое, место доступно
                    else {
                        statusId = SeatStatus.AVAILABLE;
                    }

                    return SeatsEventMapper.map(seat, statusId);
                })
                .toList();
    }
}
