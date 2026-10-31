package org.example.order.domain.dto.response;

public record AddressResponseDTO(
        String street,
        String city,
        String postalCode,
        String country
) {
}
