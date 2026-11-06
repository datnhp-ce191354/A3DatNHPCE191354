package com.example.hotel.web;

import com.example.hotel.dto.ApiDtos.*;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final HotelService hotel;
    private final BookingService bookings;
    public StaffController(HotelService hotel, BookingService bookings) {
        this.hotel = hotel;
        this.bookings = bookings;
    }

    @GetMapping("/customers")
    public List<CustomerResponse> customers(@RequestParam(required = false) String search) {
        return hotel.staffCustomers(search);
    }
    @GetMapping("/customers/{id}")
    public CustomerResponse customer(@PathVariable Long id) { return hotel.getCustomer(id); }
    @PostMapping("/customers")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel.createCustomer(request));
    }
    @PutMapping("/customers/{id}")
    public CustomerResponse updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return hotel.updateCustomer(id, request, false);
    }
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        hotel.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms")
    public List<RoomResponse> rooms(@RequestParam(required = false) String search) { return hotel.staffRooms(search); }
    @GetMapping("/rooms/{id}")
    public RoomResponse room(@PathVariable Long id) { return hotel.staffRoom(id); }
    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel.createRoom(request));
    }
    @PutMapping("/rooms/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return hotel.updateRoom(id, request);
    }
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        hotel.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/room-types")
    public List<RoomTypeResponse> roomTypes() { return hotel.roomTypes(); }
    @GetMapping("/room-types/{id}")
    public RoomTypeResponse roomType(@PathVariable Long id) { return hotel.roomType(id); }
    @PostMapping("/room-types")
    public ResponseEntity<RoomTypeResponse> createRoomType(@Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel.createRoomType(request));
    }
    @PutMapping("/room-types/{id}")
    public RoomTypeResponse updateRoomType(@PathVariable Long id, @Valid @RequestBody RoomTypeRequest request) {
        return hotel.updateRoomType(id, request);
    }
    @DeleteMapping("/room-types/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        hotel.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings(@RequestParam(required = false) String search) {
        return bookings.staffBookings(search);
    }
    @GetMapping("/bookings/{id}")
    public BookingResponse booking(@PathVariable Long id) { return bookings.staffBooking(id); }
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody StaffBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookings.createForStaff(request));
    }
    @PutMapping("/bookings/{id}")
    public BookingResponse updateBooking(@PathVariable Long id, @Valid @RequestBody BookingUpdateRequest request) {
        return bookings.updateForStaff(id, request);
    }
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookings.deleteForStaff(id);
        return ResponseEntity.noContent().build();
    }
}
