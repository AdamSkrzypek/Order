package org.example.order.repositories;

import jakarta.validation.constraints.Email;
import org.example.order.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findCustomerByEmail(@Email String email);
}
