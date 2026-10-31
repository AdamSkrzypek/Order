package org.example.order.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.order.domain.dto.request.AddressRequestDTO;
import org.example.order.domain.dto.request.PersonRequestDTO;
import org.example.order.domain.entity.Address;
import org.example.order.domain.entity.Customer;
import org.example.order.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CustomerCreationService {

    private final CustomerRepository customerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Customer createCustomer(PersonRequestDTO personDTO, AddressRequestDTO addressDTO) {
        Address address = buildAddress(addressDTO);
        Customer newCustomer = Customer.builder()
                .firstName(personDTO.firstName())
                .lastName(personDTO.lastName())
                .email(personDTO.email())
                .phoneNumber(personDTO.phoneNumber())
                .customerAddress(address)
                .build();
        return customerRepository.save(newCustomer);
    }

    private Address buildAddress(AddressRequestDTO addressDTO) {
        return Address.builder()
                .street(addressDTO.street())
                .city(addressDTO.city())
                .postalCode(addressDTO.postalCode())
                .country(addressDTO.country())
                .build();
    }
}
