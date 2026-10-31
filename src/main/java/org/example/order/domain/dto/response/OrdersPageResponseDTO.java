package org.example.order.domain.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record OrdersPageResponseDTO(
        List<OrdersListResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static OrdersPageResponseDTO from(Page<OrdersListResponseDTO> page) {
        return new OrdersPageResponseDTO(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
