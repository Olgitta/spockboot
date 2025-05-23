package com.olg.services.seats.api.dto;

public class SeatResponse {
//    private Long eventId;
//    private Long venueId;
    private String rowNumber;
    private String seatNumber;
    private byte statusId;

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

    public byte getStatusId() {
        return statusId;
    }

    public void setStatusId(byte statusId) {
        this.statusId = statusId;
    }
}
