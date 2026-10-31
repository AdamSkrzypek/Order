package org.example.order.domain.dto.response;

import org.example.order.domain.enums.OrderStatus;

import java.util.UUID;

public record OrderStatusResponseDTO(UUID orderId, UUID customerId, OrderStatus orderStatus) { }
