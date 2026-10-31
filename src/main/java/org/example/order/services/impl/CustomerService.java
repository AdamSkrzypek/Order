package org.example.order.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.dto.request.CustomerDetailsRequestDTO;
import org.example.order.domain.entity.Customer;
import org.example.order.exceptions.CustomerConflictException;
import org.example.order.repositories.CustomerRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private static final String EMAIL_CONSTRAINT = "uk_customers_email";

    private final CustomerRepository customerRepository;
    private final CustomerCreationService customerCreationService;

    @Transactional
    public Customer findOrCreate(CustomerDetailsRequestDTO dto) {
        String email = dto.personDTO().email();
        log.info("Searching for customer by email: {}", email);
        Customer customer = customerRepository.findCustomerByEmail(email)
                .orElseGet(() -> createCustomerSafely(dto));
        customer.updatePhoneNumber(dto.personDTO().phoneNumber());
        return customer;
    }

    private Customer createCustomerSafely(CustomerDetailsRequestDTO dto) {
        String email = dto.personDTO().email();
        log.info("Customer not found. Creating new one for email: {}", email);
        try {
            return customerCreationService.createCustomer(dto.personDTO(), dto.addressDTO());
        } catch (DataIntegrityViolationException ex) {
            if (!violatesEmailConstraint(ex)) {
                throw ex;
            }
            log.warn("Race condition detected for email {}. Fetching existing customer.", email);
            return customerRepository.findCustomerByEmail(email)
                    .orElseThrow(() -> new CustomerConflictException(
                            "Failed to create or find customer after race condition. Email: " + email));
        }
    }

    private boolean violatesEmailConstraint(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof ConstraintViolationException cve
                && EMAIL_CONSTRAINT.equals(cve.getConstraintName());
    }
}
