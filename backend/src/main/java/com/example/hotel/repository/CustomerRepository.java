package com.example.hotel.repository;

import com.example.hotel.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAddressIgnoreCase(String emailAddress);
    boolean existsByEmailAddressIgnoreCase(String emailAddress);
}
