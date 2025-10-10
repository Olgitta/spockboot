package com.olg.services.booking.api.seats.dto;

import com.olg.core.domain.enums.SeatStatus;

import java.util.Objects;

public class SeatResponse {
    private Long id;
    private String rowNumber;
    private String seatNumber;
    private SeatStatus statusId;
    private String guestId;

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

    public SeatStatus getStatusId() {
        return statusId;
    }

    public void setStatusId(SeatStatus statusId) {
        this.statusId = statusId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SeatResponse that = (SeatResponse) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getRowNumber(), that.getRowNumber()) && Objects.equals(getSeatNumber(), that.getSeatNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRowNumber(), getSeatNumber());
    }
}
