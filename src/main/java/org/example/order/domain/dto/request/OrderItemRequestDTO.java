package org.example.order.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequestDTO(@NotNull UUID productId, @NotBlank String productName,
                                  @NotNull @Positive Integer quantity,
                                  @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal unitPrice) {}
