package com.example.hotel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Customer", uniqueConstraints = @UniqueConstraint(name = "uk_customer_email", columnNames = "EmailAddress"))
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    public Long id;

    @Column(name = "CustomerFullName", nullable = false, length = 100)
    public String customerFullName;

    @Column(name = "Telephone", nullable = false, length = 20)
    public String telephone;

    @Column(name = "EmailAddress", nullable = false, length = 150)
    public String emailAddress;

    @Column(name = "CustomerBirthday")
    public LocalDate customerBirthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "CustomerStatus", nullable = false, length = 10)
    public CustomerStatus customerStatus = CustomerStatus.ACTIVE;

    @Column(name = "Password", nullable = false, length = 100)
    public String password;
}
