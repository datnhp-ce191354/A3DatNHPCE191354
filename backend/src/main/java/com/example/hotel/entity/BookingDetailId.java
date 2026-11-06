package com.example.hotel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BookingDetailId implements Serializable {
    @Column(name = "BookingReservationID")
    public Long bookingReservationId;

    @Column(name = "RoomID")
    public Long roomId;

    public BookingDetailId() {}

    public BookingDetailId(Long bookingReservationId, Long roomId) {
        this.bookingReservationId = bookingReservationId;
        this.roomId = roomId;
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BookingDetailId that)) return false;
        return Objects.equals(bookingReservationId, that.bookingReservationId) && Objects.equals(roomId, that.roomId);
    }

    @Override public int hashCode() { return Objects.hash(bookingReservationId, roomId); }
}
