package com.example.hotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BookingReservation")
public class BookingReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingReservationID")
    public Long id;

    @Column(name = "BookingDate", nullable = false)
    public LocalDateTime bookingDate;

    @Column(name = "TotalPrice", nullable = false, precision = 18, scale = 2)
    public BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CustomerID", nullable = false)
    public Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "BookingStatus", nullable = false, length = 12)
    public BookingStatus bookingStatus = BookingStatus.PENDING;

    @OneToMany(mappedBy = "bookingReservation", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<BookingDetail> details = new ArrayList<>();
}
