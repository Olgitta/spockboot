package com.olg.qweb.api.events.dto;

import java.time.LocalDateTime;

public class EventResponse {

    private Long id;
    private String name;
    private byte type;
    private LocalDateTime dateTime;
    private Long venueId;

    public EventResponse(Long id, String name, byte type, LocalDateTime dateTime, Long venueId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.dateTime = dateTime;
        this.venueId = venueId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
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
