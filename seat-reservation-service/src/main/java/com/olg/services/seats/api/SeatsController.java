package com.olg.services.seats.api;

import com.olg.services.seats.api.dto.SeatRequest;
import com.olg.services.seats.api.dto.SeatResponse;
import com.olg.services.seats.exceptions.SeatAlreadyLockedException;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
public class SeatsController {

    private final SeatsService seatsService;

    public SeatsController(SeatsService seatsService) {
        this.seatsService = seatsService;
    }

    @GetMapping("/{eventId}/{venueId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByEventIdAndVenueId(@PathVariable Long eventId, @PathVariable Long venueId) {

        try {
            List<SeatResponse> response = seatsService.getSeats(eventId, venueId);

            return ResponseEntity.ok(new ApiResponse<>(MDC.get("requestId"), response));
        } catch (Exception e) {
//            log.error("Something went wrong", e);
            return ResponseEntity.badRequest().body(null);
        }

    }

    @PostMapping("locks")
    public ResponseEntity<Void> lock(@RequestBody SeatRequest request) {
        try {
            seatsService.lock(
                    request.eventId(),
                    request.venueId(),
                    request.rowNumber(),
                    request.seatNumber()
            );

            return ResponseEntity.ok().build();
        } catch (SeatAlreadyLockedException seatAlreadyLockedException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (DataAccessException dataAccessException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("locks")
    public ResponseEntity<Void> unlock(@RequestBody SeatRequest request) {
        seatsService.unlock(request.eventId(),
                request.venueId(),
                request.rowNumber(),
                request.seatNumber());
        return ResponseEntity.ok().build();
    }
}
