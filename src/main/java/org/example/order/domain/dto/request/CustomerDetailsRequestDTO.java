package org.example.order.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CustomerDetailsRequestDTO(@NotNull @Valid PersonRequestDTO personDTO, @NotNull @Valid AddressRequestDTO addressDTO) {
}
