package com.olg.mysql.seats;

import com.olg.mysql.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"venue_id", "row_number", "seat_number"})
})
public class Seat extends BaseEntity {

    @Column(name = "row_number", nullable = false, length = 10)
    private String rowNumber;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    public String getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }
}

