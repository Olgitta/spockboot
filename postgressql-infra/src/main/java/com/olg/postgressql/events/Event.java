package com.olg.postgressql.events;

import com.olg.postgressql.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "event_type_id", nullable = false)
    private byte typeId;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getType() {
        return typeId;
    }

    public void setType(byte typeId) {
        this.typeId = typeId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

}

