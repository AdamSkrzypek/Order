package org.example.order.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO( @NotNull @Valid CustomerDetailsRequestDTO customerDetailsRequest,
                             @NotEmpty List<@NotNull @Valid OrderItemRequestDTO> orderItems)  {}
