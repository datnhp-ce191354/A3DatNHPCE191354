package com.example.hotel.repository;

import com.example.hotel.entity.RoomInformation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoomInformationRepository extends JpaRepository<RoomInformation, Long> {
    boolean existsByRoomNumberIgnoreCase(String roomNumber);
    boolean existsByRoomType_Id(Long roomTypeId);

    @Query("select r from RoomInformation r join r.roomType t where r.roomStatus = com.example.hotel.entity.RoomStatus.ACTIVE " +
           "and (:search is null or lower(r.roomNumber) like lower(concat('%', :search, '%')) " +
           "or lower(r.roomDetailDescription) like lower(concat('%', :search, '%')) " +
           "or lower(t.roomTypeName) like lower(concat('%', :search, '%')))")
    Page<RoomInformation> searchActive(@Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomInformation r where r.id = :id")
    Optional<RoomInformation> findByIdForUpdate(@Param("id") Long id);
}
