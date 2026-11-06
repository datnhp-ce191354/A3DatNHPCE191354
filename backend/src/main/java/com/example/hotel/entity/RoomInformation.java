package com.example.hotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RoomInformation")
public class RoomInformation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    public Long id;

    @Column(name = "RoomNumber", nullable = false, unique = true, length = 30)
    public String roomNumber;

    @Column(name = "RoomDetailDescription", length = 1000)
    public String roomDetailDescription;

    @Column(name = "RoomMaxCapacity", nullable = false)
    public Integer roomMaxCapacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RoomTypeID", nullable = false)
    public RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "RoomStatus", nullable = false, length = 10)
    public RoomStatus roomStatus = RoomStatus.ACTIVE;

    @Column(name = "RoomPricePerDay", nullable = false, precision = 18, scale = 2)
    public BigDecimal roomPricePerDay;
}
