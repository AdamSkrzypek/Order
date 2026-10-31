package org.example.order.domain.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrdersListResponseDTO(UUID orderId, CustomerResponseDTO customerResponseDTO,
                                    List<OrderItemResponseDTO> orderItemResponseDTO,
                                    BigDecimal totalCost) {}
