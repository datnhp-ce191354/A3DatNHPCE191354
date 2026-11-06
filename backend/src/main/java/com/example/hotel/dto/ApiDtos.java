package com.example.hotel.dto;

import com.example.hotel.entity.BookingStatus;
import com.example.hotel.entity.CustomerStatus;
import com.example.hotel.entity.RoomStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String customerFullName,
            @NotBlank @Pattern(regexp = "^[0-9+() .-]{7,20}$") String telephone,
            @NotBlank @Email @Size(max = 150) String emailAddress,
            @Past LocalDate customerBirthday,
            @NotBlank @Size(min = 8, max = 100) String password) {}

    public record AuthResponse(String token, String role, String name, String email) {}

    public record CustomerRequest(
            @NotBlank @Size(max = 100) String customerFullName,
            @NotBlank @Pattern(regexp = "^[0-9+() .-]{7,20}$") String telephone,
            @NotBlank @Email @Size(max = 150) String emailAddress,
            @Past LocalDate customerBirthday,
            CustomerStatus customerStatus,
            @Size(min = 8, max = 100) String password) {}

    public record CustomerResponse(Long id, String customerFullName, String telephone,
                                   String emailAddress, LocalDate customerBirthday,
                                   CustomerStatus customerStatus) {}

    public record RoomTypeRequest(@NotBlank @Size(max = 100) String roomTypeName,
                                  @Size(max = 500) String typeDescription,
                                  @Size(max = 500) String typeNote) {}

    public record RoomTypeResponse(Long id, String roomTypeName, String typeDescription, String typeNote) {}

    public record RoomRequest(@NotBlank @Size(max = 30) String roomNumber,
                              @Size(max = 1000) String roomDetailDescription,
                              @NotNull @Min(1) Integer roomMaxCapacity,
                              @NotNull Long roomTypeId,
                              RoomStatus roomStatus,
                              @NotNull @DecimalMin("0.01") BigDecimal roomPricePerDay) {}

    public record RoomResponse(Long id, String roomNumber, String roomDetailDescription,
                               Integer roomMaxCapacity, RoomStatus roomStatus,
                               BigDecimal roomPricePerDay, RoomTypeResponse roomType) {}

    public record RoomStayRequest(@NotNull Long roomId, @NotNull LocalDate startDate,
                                  @NotNull LocalDate endDate) {}

    public record BookingRequest(@NotEmpty List<@Valid RoomStayRequest> rooms) {}

    public record StaffBookingRequest(@NotNull Long customerId,
                                      @NotEmpty List<@Valid RoomStayRequest> rooms) {}

    public record BookingUpdateRequest(BookingStatus bookingStatus,
                                       List<@Valid RoomStayRequest> rooms) {}

    public record BookingDetailResponse(Long roomId, String roomNumber, LocalDate startDate,
                                        LocalDate endDate, BigDecimal actualPrice) {}

    public record BookingResponse(Long id, LocalDateTime bookingDate, BigDecimal totalPrice,
                                  BookingStatus bookingStatus, Long customerId, String customerName,
                                  List<BookingDetailResponse> details) {}

    public record ErrorResponse(String message, java.util.Map<String, String> fields) {}
}
