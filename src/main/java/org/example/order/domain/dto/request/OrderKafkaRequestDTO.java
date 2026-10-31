package org.example.order.domain.dto.request;

import java.util.UUID;

public record OrderKafkaRequestDTO(UUID customerId, UUID orderId, OrderRequestDTO request) {
}
