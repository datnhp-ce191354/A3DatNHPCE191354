package com.example.hotel.repository;

import com.example.hotel.entity.BookingReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingReservationRepository extends JpaRepository<BookingReservation, Long> {
    List<BookingReservation> findByCustomer_IdOrderByBookingDateDesc(Long customerId);
    boolean existsByCustomer_Id(Long customerId);
}
