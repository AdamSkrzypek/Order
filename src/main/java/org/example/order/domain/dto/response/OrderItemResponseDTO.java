package org.example.order.domain.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(UUID productId, String productName, int quantity, BigDecimal unitPrice) {}
