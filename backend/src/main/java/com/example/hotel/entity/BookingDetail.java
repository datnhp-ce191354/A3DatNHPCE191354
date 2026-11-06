package com.example.hotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BookingDetail")
@IdClass(BookingDetailId.class)
public class BookingDetail {
    @Id
    @Column(name = "BookingReservationID")
    public Long bookingReservationId;

    @Id
    @Column(name = "RoomID")
    public Long roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BookingReservationID", nullable = false, insertable = false, updatable = false)
    public BookingReservation bookingReservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RoomID", nullable = false, insertable = false, updatable = false)
    public RoomInformation room;

    @Column(name = "StartDate", nullable = false)
    public LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    public LocalDate endDate;

    @Column(name = "ActualPrice", nullable = false, precision = 18, scale = 2)
    public BigDecimal actualPrice;
}
