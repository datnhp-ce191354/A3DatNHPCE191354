package com.example.hotel.repository;

import com.example.hotel.entity.BookingDetail;
import com.example.hotel.entity.BookingDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, BookingDetailId> {
    boolean existsByRoom_Id(Long roomId);

    @Query("select count(d) from BookingDetail d join d.bookingReservation b " +
           "where d.room.id = :roomId and b.bookingStatus <> com.example.hotel.entity.BookingStatus.CANCELLED " +
           "and d.startDate < :endDate and d.endDate > :startDate " +
           "and (:excludeBookingId is null or b.id <> :excludeBookingId)")
    long countOverlaps(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,
                       @Param("excludeBookingId") Long excludeBookingId);
}
