package org.example.order.domain.dto.request;

import org.example.order.domain.dto.response.OrdersListResponseDTO;

import java.util.List;

public record OrdersPageResponseDTO(
        List<OrdersListResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
