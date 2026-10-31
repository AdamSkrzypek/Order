package org.example.order.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequestDTO(@NotBlank String street, @NotBlank String city,
                                @NotBlank @Size(min = 6, max = 6) String postalCode,
                                @NotBlank String country) {
}
