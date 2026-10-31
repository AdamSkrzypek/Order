package org.example.order.domain.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record OrderVersionsPageResponseDTO(
        List<OrderVersionResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static OrderVersionsPageResponseDTO from(Page<OrderVersionResponseDTO> page) {
        return new OrderVersionsPageResponseDTO(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
