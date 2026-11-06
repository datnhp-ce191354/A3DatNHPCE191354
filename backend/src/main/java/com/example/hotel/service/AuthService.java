package com.example.hotel.service;

import com.example.hotel.dto.ApiDtos.*;
import com.example.hotel.entity.Customer;
import com.example.hotel.entity.CustomerStatus;
import com.example.hotel.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.hotel.config.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@Service
public class AuthService {
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final String staffEmail;
    private final String staffPassword;
    private final String staffName;

    public AuthService(CustomerRepository customers, PasswordEncoder encoder, JwtService jwtService,
                       @Value("${app.staff.email}") String staffEmail,
                       @Value("${app.staff.password}") String staffPassword,
                       @Value("${app.staff.name}") String staffName) {
        this.customers = customers;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.staffEmail = staffEmail.trim().toLowerCase(Locale.ROOT);
        this.staffPassword = staffPassword;
        this.staffName = staffName;
    }

    @Transactional
    public CustomerResponse register(RegisterRequest request) {
        String email = normalize(request.emailAddress());
        if (email.equals(staffEmail) || customers.existsByEmailAddressIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        var customer = new Customer();
        customer.customerFullName = request.customerFullName().trim();
        customer.telephone = request.telephone().trim();
        customer.emailAddress = email;
        customer.customerBirthday = request.customerBirthday();
        customer.customerStatus = CustomerStatus.ACTIVE;
        customer.password = encoder.encode(request.password());
        customers.save(customer);
        return new CustomerResponse(customer.id, customer.customerFullName, customer.telephone,
                customer.emailAddress, customer.customerBirthday, customer.customerStatus);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        if (email.equals(staffEmail) && MessageDigest.isEqual(
                request.password().getBytes(StandardCharsets.UTF_8), staffPassword.getBytes(StandardCharsets.UTF_8))) {
            return new AuthResponse(jwtService.issue("staff", "STAFF", email), "STAFF", staffName, email);
        }
        Customer customer = customers.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (customer.customerStatus != CustomerStatus.ACTIVE || !encoder.matches(request.password(), customer.password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new AuthResponse(jwtService.issue(customer.id.toString(), "CUSTOMER", email),
                "CUSTOMER", customer.customerFullName, email);
    }

    private static String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
}
