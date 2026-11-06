package com.example.hotel.web;

import com.example.hotel.dto.ApiDtos.*;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class CustomerController {
    private final HotelService hotel;
    private final BookingService bookings;
    public CustomerController(HotelService hotel, BookingService bookings) {
        this.hotel = hotel;
        this.bookings = bookings;
    }

    @GetMapping("/api/customers/me")
    public CustomerResponse me(Authentication authentication) {
        Long id = Long.valueOf(authentication.getName());
        bookings.activeCustomer(id);
        return hotel.getCustomer(id);
    }

    @PutMapping("/api/customers/me")
    public CustomerResponse updateMe(Authentication authentication,
                                     @Valid @RequestBody CustomerRequest request) {
        Long id = Long.valueOf(authentication.getName());
        bookings.activeCustomer(id);
        return hotel.updateCustomer(id, request, true);
    }

    @GetMapping("/api/bookings/me")
    public List<BookingResponse> myBookings(Authentication authentication) {
        return bookings.myBookings(Long.valueOf(authentication.getName()));
    }

    @PostMapping("/api/bookings/me")
    public ResponseEntity<BookingResponse> createBooking(Authentication authentication,
                                                          @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookings.createForCustomer(Long.valueOf(authentication.getName()), request));
    }
}
