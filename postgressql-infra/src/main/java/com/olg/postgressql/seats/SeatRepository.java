package com.olg.postgressql.seats;

import com.olg.postgressql.seats.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenueId (Long venueId);
}
