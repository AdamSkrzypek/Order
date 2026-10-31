package org.example.order.domain.dto.response;

public record PersonResponseDTO(
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
