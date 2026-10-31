package org.example.order.services.impl;

import lombok.AllArgsConstructor;
import org.example.order.domain.dto.response.OrderVersionResponseDTO;
import org.example.order.domain.dto.response.OrderVersionsPageResponseDTO;
import org.example.order.exceptions.OrderNotFoundException;
import org.example.order.mappers.OrderMapper;
import org.example.order.repositories.OrderRepository;
import org.example.order.repositories.OrderVersionRepository;
import org.example.order.services.OrderVersionService;
import org.example.order.specifications.OrderVersionSpecification;
import org.example.order.web.request.DefaultPageRequest;
import org.example.order.web.request.FilterDto;
import org.example.order.web.request.PagingUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderVersionServiceImpl implements OrderVersionService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final OrderVersionRepository orderVersionRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderVersionsPageResponseDTO getOrderVersions(UUID id, FilterDto filters, DefaultPageRequest pageRequest) {

        if (!orderRepository.existsByIdAndDeletedFalse(id)) {
            throw new OrderNotFoundException("Order not found with id: " + id);
        }

        Pageable pageable = PagingUtils.toPageable(pageRequest);
        Page<OrderVersionResponseDTO> result = orderVersionRepository
                .findAll(new OrderVersionSpecification(id, filters), pageable)
                .map(orderMapper::toOrderVersionDto);
        return OrderVersionsPageResponseDTO.from(result);
    }

}
