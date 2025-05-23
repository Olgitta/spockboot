package com.olg.services.seats.api.dto;

import com.olg.mysql.seats.Seat;
import com.olg.mysql.seats.SeatsEvent;
import com.olg.services.seats.utils.RedisKeyFactory;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;

public class SeatsEventMapper {

    public static SeatResponse map(SeatsEvent seat) {
        SeatResponse response = new SeatResponse();
//
//        response.setEventId(seatsEvent.getEventId());
//        response.setVenueId(seatsEvent.getVenueId());
        response.setRowNumber(seat.getRowNumber());
        response.setSeatNumber(seat.getSeatNumber());
        response.setStatusId(seat.getStatusId());

        return response;
    }

    public static SeatResponse map(Seat seat, byte statusId) {
        SeatResponse response = new SeatResponse();
//
//        response.setEventId(seatsEvent.getEventId());
//        response.setVenueId(seatsEvent.getVenueId());
        response.setRowNumber(seat.getRowNumber());
        response.setSeatNumber(seat.getSeatNumber());
        response.setStatusId(statusId);

        return response;
    }

//    public static List<SeatResponse> map(List<SeatsEvent> seats) {
//        return seats.stream()
//                .map(SeatsEventMapper::map)
//                .toList();
//    }
//
//    public static List<SeatResponse> map(List<Seat> seats) {
//        return seats.stream()
//                .map(SeatsEventMapper::map)
//                .toList();
//    }

    public static List<SeatResponse> map(List<Seat> seats, Map<String, String> locked) {
        return seats.stream()
                .map((seat) -> {
                    String field = RedisKeyFactory.reservationField(
                            seat.getVenueId(),
                            seat.getRowNumber(),
                            seat.getSeatNumber()
                    );
                    byte statusId = (locked.get(field) != null) ? (byte) 2 : (byte) 1;

                    return SeatsEventMapper.map(seat, statusId);
                })
                .toList();
    }
}
