package com.olg.mysql.venues;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface VenueRepository extends JpaRepository<Venue, Long> {

}
