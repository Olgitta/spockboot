package com.olg.mysql.seats;

import com.olg.mysql.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "venue_id", "row_number", "seat_number"}))
public class SeatsEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "row_number", length = 10, nullable = false)
    private String rowNumber;

    @Column(name = "seat_number", length = 10, nullable = false)
    private String seatNumber;

    @Column(name = "status_id", nullable = false)
    private Byte statusId;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

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

    public Byte getStatusId() {
        return statusId;
    }

    public void setStatusId(Byte statusId) {
        this.statusId = statusId;
    }
}

