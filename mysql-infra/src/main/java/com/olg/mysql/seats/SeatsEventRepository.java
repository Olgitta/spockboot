package com.olg.mysql.seats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatsEventRepository extends JpaRepository<SeatsEvent, Long> {
    List<SeatsEvent> findAllByEventIdAndVenueId(Long eventId, Long venueId);
}
