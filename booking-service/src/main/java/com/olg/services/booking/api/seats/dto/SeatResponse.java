package com.olg.services.booking.api.seats.dto;

import com.olg.domain.enums.SeatStatus;

import java.util.Objects;

public class SeatResponse {
    private String rowNumber;
    private String seatNumber;
    private SeatStatus statusId;
    private String lockerId;

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

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatResponse that = (SeatResponse) o;
        return Objects.equals(rowNumber, that.rowNumber) &&
                Objects.equals(seatNumber, that.seatNumber) &&
                statusId == that.statusId; // Для enum можно сравнивать напрямую
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRowNumber(), getSeatNumber(), getStatusId());
    }
}
