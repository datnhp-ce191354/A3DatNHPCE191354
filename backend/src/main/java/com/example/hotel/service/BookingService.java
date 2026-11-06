package com.example.hotel.service;

import com.example.hotel.dto.ApiDtos.*;
import com.example.hotel.entity.*;
import com.example.hotel.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class BookingService {
    private final BookingReservationRepository bookings;
    private final BookingDetailRepository details;
    private final RoomInformationRepository rooms;
    private final CustomerRepository customers;

    public BookingService(BookingReservationRepository bookings, BookingDetailRepository details,
                          RoomInformationRepository rooms, CustomerRepository customers) {
        this.bookings = bookings;
        this.details = details;
        this.rooms = rooms;
        this.customers = customers;
    }

    public Customer activeCustomer(Long id) {
        Customer c = customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        if (c.customerStatus != CustomerStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer account is inactive");
        }
        return c;
    }

    public List<BookingResponse> myBookings(Long customerId) {
        activeCustomer(customerId);
        return bookings.findByCustomer_IdOrderByBookingDateDesc(customerId).stream().map(this::response).toList();
    }

    public BookingResponse createForCustomer(Long customerId, BookingRequest request) {
        return create(activeCustomer(customerId), request.rooms());
    }

    public BookingResponse createForStaff(StaffBookingRequest request) {
        return create(activeCustomer(request.customerId()), request.rooms());
    }

    private BookingResponse create(Customer customer, List<RoomStayRequest> requestedRooms) {
        Map<Long, RoomInformation> locked = lockAndValidate(requestedRooms, null);
        BookingReservation reservation = new BookingReservation();
        reservation.customer = customer;
        reservation.bookingDate = LocalDateTime.now();
        reservation.bookingStatus = BookingStatus.PENDING;
        reservation.totalPrice = BigDecimal.ZERO;
        bookings.saveAndFlush(reservation);
        fillDetails(reservation, requestedRooms, locked);
        bookings.saveAndFlush(reservation);
        return response(reservation);
    }

    public List<BookingResponse> staffBookings(String search) {
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return bookings.findAll(Sort.by(Sort.Direction.DESC, "bookingDate")).stream()
                .filter(b -> term.isEmpty() || b.id.toString().contains(term)
                        || b.customer.customerFullName.toLowerCase(Locale.ROOT).contains(term)
                        || b.customer.emailAddress.toLowerCase(Locale.ROOT).contains(term))
                .map(this::response).toList();
    }

    public BookingResponse staffBooking(Long id) { return response(requireBooking(id)); }

    public BookingResponse updateForStaff(Long id, BookingUpdateRequest request) {
        BookingReservation reservation = requireBooking(id);
        BookingStatus nextStatus = request.bookingStatus() == null ? reservation.bookingStatus : request.bookingStatus();
        List<RoomStayRequest> requestedRooms = request.rooms();
        if (requestedRooms != null) {
            if (requestedRooms.isEmpty()) throw bad("Booking needs at least one room");
            Map<Long, RoomInformation> locked = lockAndValidate(requestedRooms,
                    nextStatus == BookingStatus.CANCELLED ? null : reservation.id,
                    nextStatus != BookingStatus.CANCELLED);
            reservation.details.clear();
            bookings.saveAndFlush(reservation);
            fillDetails(reservation, requestedRooms, locked);
        } else if (reservation.bookingStatus == BookingStatus.CANCELLED && nextStatus != BookingStatus.CANCELLED) {
            List<RoomStayRequest> existing = reservation.details.stream()
                    .map(d -> new RoomStayRequest(d.room.id, d.startDate, d.endDate)).toList();
            lockAndValidate(existing, reservation.id);
        }
        reservation.bookingStatus = nextStatus;
        bookings.saveAndFlush(reservation);
        return response(reservation);
    }

    public void deleteForStaff(Long id) { bookings.delete(requireBooking(id)); }

    private BookingReservation requireBooking(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private Map<Long, RoomInformation> lockAndValidate(List<RoomStayRequest> requested, Long excludeBookingId) {
        return lockAndValidate(requested, excludeBookingId, true);
    }

    private Map<Long, RoomInformation> lockAndValidate(List<RoomStayRequest> requested, Long excludeBookingId,
                                                        boolean checkOverlap) {
        if (requested == null || requested.isEmpty()) throw bad("Booking needs at least one room");
        Set<Long> ids = new HashSet<>();
        for (RoomStayRequest stay : requested) {
            if (stay == null || stay.roomId() == null || stay.startDate() == null || stay.endDate() == null) {
                throw bad("Each room needs roomId, startDate and endDate");
            }
            if (!ids.add(stay.roomId())) throw bad("A room can occur only once in a booking");
            if (stay.startDate().isBefore(LocalDate.now()) || !stay.endDate().isAfter(stay.startDate())) {
                throw bad("Stay dates must start today or later and end after startDate");
            }
        }
        Map<Long, RoomInformation> locked = new HashMap<>();
        ids.stream().sorted().forEach(id -> {
            RoomInformation room = rooms.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + id));
            if (room.roomStatus != RoomStatus.ACTIVE) throw bad("Room is inactive: " + room.roomNumber);
            locked.put(id, room);
        });
        if (checkOverlap) {
            for (RoomStayRequest stay : requested) {
                if (details.countOverlaps(stay.roomId(), stay.startDate(), stay.endDate(), excludeBookingId) > 0) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Room is already booked for these dates: " + locked.get(stay.roomId()).roomNumber);
                }
            }
        }
        return locked;
    }

    private void fillDetails(BookingReservation reservation, List<RoomStayRequest> requested,
                             Map<Long, RoomInformation> locked) {
        BigDecimal total = BigDecimal.ZERO;
        for (RoomStayRequest stay : requested) {
            RoomInformation room = locked.get(stay.roomId());
            BigDecimal price = room.roomPricePerDay.multiply(BigDecimal.valueOf(
                    ChronoUnit.DAYS.between(stay.startDate(), stay.endDate())));
            BookingDetail detail = new BookingDetail();
            detail.bookingReservationId = reservation.id;
            detail.roomId = room.id;
            detail.bookingReservation = reservation;
            detail.room = room;
            detail.startDate = stay.startDate();
            detail.endDate = stay.endDate();
            detail.actualPrice = price;
            reservation.details.add(detail);
            total = total.add(price);
        }
        reservation.totalPrice = total;
    }

    private BookingResponse response(BookingReservation b) {
        return new BookingResponse(b.id, b.bookingDate, b.totalPrice, b.bookingStatus,
                b.customer.id, b.customer.customerFullName,
                b.details.stream().map(d -> new BookingDetailResponse(d.room.id, d.room.roomNumber,
                        d.startDate, d.endDate, d.actualPrice)).toList());
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
