package org.example.order.domain.dto.response;

import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderResponseDTO(UUID orderId, OrderStatus orderStatus, Instant createdAt,
                               PaymentStatus paymentStatus) {
}
