package org.example.order.services;

import org.example.order.domain.dto.response.OrderVersionsPageResponseDTO;
import org.example.order.web.request.DefaultPageRequest;
import org.example.order.web.request.FilterDto;

import java.util.UUID;

public interface OrderVersionService {

    OrderVersionsPageResponseDTO getOrderVersions(
            UUID id, FilterDto filters, DefaultPageRequest pageRequest);
}
