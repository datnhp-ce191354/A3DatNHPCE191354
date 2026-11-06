package com.example.hotel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "RoomType")
public class RoomType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomTypeID")
    public Long id;

    @Column(name = "RoomTypeName", nullable = false, unique = true, length = 100)
    public String roomTypeName;

    @Column(name = "TypeDescription", length = 500)
    public String typeDescription;

    @Column(name = "TypeNote", length = 500)
    public String typeNote;
}
