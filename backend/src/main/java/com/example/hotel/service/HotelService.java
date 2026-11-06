package com.example.hotel.service;

import com.example.hotel.dto.ApiDtos.*;
import com.example.hotel.entity.*;
import com.example.hotel.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class HotelService {
    private final CustomerRepository customers;
    private final RoomTypeRepository roomTypes;
    private final RoomInformationRepository rooms;
    private final BookingReservationRepository bookings;
    private final BookingDetailRepository details;
    private final PasswordEncoder encoder;
    private final String staffEmail;

    public HotelService(CustomerRepository customers, RoomTypeRepository roomTypes,
                        RoomInformationRepository rooms, BookingReservationRepository bookings,
                        BookingDetailRepository details, PasswordEncoder encoder,
                        @Value("${app.staff.email}") String staffEmail) {
        this.customers = customers;
        this.roomTypes = roomTypes;
        this.rooms = rooms;
        this.bookings = bookings;
        this.details = details;
        this.encoder = encoder;
        this.staffEmail = staffEmail.trim().toLowerCase(Locale.ROOT);
    }

    public Customer requireCustomer(Long id) {
        return customers.findById(id).orElseThrow(() -> missing("Customer"));
    }

    public CustomerResponse getCustomer(Long id) { return customerResponse(requireCustomer(id)); }

    public List<CustomerResponse> staffCustomers(String search) {
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return customers.findAll(Sort.by("id")).stream()
                .filter(c -> term.isEmpty() || c.customerFullName.toLowerCase(Locale.ROOT).contains(term)
                        || c.emailAddress.toLowerCase(Locale.ROOT).contains(term)
                        || c.telephone.contains(term))
                .map(this::customerResponse).toList();
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        Customer customer = new Customer();
        applyCustomer(customer, request, true);
        customers.save(customer);
        return customerResponse(customer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request, boolean self) {
        Customer customer = requireCustomer(id);
        applyCustomer(customer, request, !self);
        return customerResponse(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = requireCustomer(id);
        if (bookings.existsByCustomer_Id(id)) customer.customerStatus = CustomerStatus.INACTIVE;
        else customers.delete(customer);
    }

    private void applyCustomer(Customer customer, CustomerRequest request, boolean allowStatus) {
        String email = request.emailAddress().trim().toLowerCase(Locale.ROOT);
        if (email.equals(staffEmail) || customers.findByEmailAddressIgnoreCase(email)
                .filter(other -> !other.id.equals(customer.id)).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        customer.customerFullName = request.customerFullName().trim();
        customer.telephone = request.telephone().trim();
        customer.emailAddress = email;
        customer.customerBirthday = request.customerBirthday();
        if (allowStatus && request.customerStatus() != null) customer.customerStatus = request.customerStatus();
        if (request.password() != null && !request.password().isBlank()) customer.password = encoder.encode(request.password());
    }

    public Page<RoomResponse> publicRooms(String search, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 0 and size must be 1..100");
        }
        return rooms.searchActive(search == null || search.isBlank() ? null : search.trim(),
                PageRequest.of(page, size, Sort.by("roomNumber"))).map(this::roomResponse);
    }

    public RoomResponse publicRoom(Long id) {
        RoomInformation room = requireRoom(id);
        if (room.roomStatus != RoomStatus.ACTIVE) throw missing("Room");
        return roomResponse(room);
    }

    public List<RoomResponse> staffRooms(String search) {
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return rooms.findAll(Sort.by("roomNumber")).stream()
                .filter(r -> term.isEmpty() || r.roomNumber.toLowerCase(Locale.ROOT).contains(term)
                        || (r.roomDetailDescription != null && r.roomDetailDescription.toLowerCase(Locale.ROOT).contains(term))
                        || r.roomType.roomTypeName.toLowerCase(Locale.ROOT).contains(term))
                .map(this::roomResponse).toList();
    }

    public RoomResponse staffRoom(Long id) { return roomResponse(requireRoom(id)); }

    public RoomResponse createRoom(RoomRequest request) {
        RoomInformation room = new RoomInformation();
        applyRoom(room, request);
        rooms.save(room);
        return roomResponse(room);
    }

    public RoomResponse updateRoom(Long id, RoomRequest request) {
        RoomInformation room = requireRoom(id);
        applyRoom(room, request);
        return roomResponse(room);
    }

    public void deleteRoom(Long id) {
        RoomInformation room = requireRoom(id);
        if (details.existsByRoom_Id(id)) room.roomStatus = RoomStatus.INACTIVE;
        else rooms.delete(room);
    }

    private void applyRoom(RoomInformation room, RoomRequest request) {
        String number = request.roomNumber().trim();
        if (rooms.findAll().stream().anyMatch(other -> other.roomNumber.equalsIgnoreCase(number)
                && !other.id.equals(room.id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room number already exists");
        }
        room.roomNumber = number;
        room.roomDetailDescription = request.roomDetailDescription();
        room.roomMaxCapacity = request.roomMaxCapacity();
        room.roomType = requireRoomType(request.roomTypeId());
        room.roomPricePerDay = request.roomPricePerDay();
        if (request.roomStatus() != null) room.roomStatus = request.roomStatus();
    }

    public List<RoomTypeResponse> roomTypes() {
        return roomTypes.findAll(Sort.by("roomTypeName")).stream().map(this::roomTypeResponse).toList();
    }

    public RoomTypeResponse roomType(Long id) { return roomTypeResponse(requireRoomType(id)); }

    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        RoomType type = new RoomType();
        applyRoomType(type, request);
        roomTypes.save(type);
        return roomTypeResponse(type);
    }

    public RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request) {
        RoomType type = requireRoomType(id);
        applyRoomType(type, request);
        return roomTypeResponse(type);
    }

    public void deleteRoomType(Long id) {
        RoomType type = requireRoomType(id);
        if (rooms.existsByRoomType_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room type is used by rooms");
        }
        roomTypes.delete(type);
    }

    private void applyRoomType(RoomType type, RoomTypeRequest request) {
        String name = request.roomTypeName().trim();
        if (roomTypes.findAll().stream().anyMatch(other -> other.roomTypeName.equalsIgnoreCase(name)
                && !other.id.equals(type.id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room type name already exists");
        }
        type.roomTypeName = name;
        type.typeDescription = request.typeDescription();
        type.typeNote = request.typeNote();
    }

    public RoomInformation requireRoom(Long id) {
        return rooms.findById(id).orElseThrow(() -> missing("Room"));
    }

    private RoomType requireRoomType(Long id) {
        return roomTypes.findById(id).orElseThrow(() -> missing("Room type"));
    }

    private CustomerResponse customerResponse(Customer c) {
        return new CustomerResponse(c.id, c.customerFullName, c.telephone,
                c.emailAddress, c.customerBirthday, c.customerStatus);
    }

    private RoomTypeResponse roomTypeResponse(RoomType t) {
        return new RoomTypeResponse(t.id, t.roomTypeName, t.typeDescription, t.typeNote);
    }

    private RoomResponse roomResponse(RoomInformation r) {
        return new RoomResponse(r.id, r.roomNumber, r.roomDetailDescription, r.roomMaxCapacity,
                r.roomStatus, r.roomPricePerDay, roomTypeResponse(r.roomType));
    }

    private static ResponseStatusException missing(String thing) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, thing + " not found");
    }
}
