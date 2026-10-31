package org.example.order.services.impl;


import lombok.RequiredArgsConstructor;
import org.example.order.domain.dto.request.OrderRequestDTO;
import org.example.order.domain.dto.response.OrderResponseDTO;
import org.example.order.domain.dto.response.OrdersListResponseDTO;
import org.example.order.domain.dto.response.OrdersPageResponseDTO;
import org.example.order.domain.entity.*;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;
import org.example.order.exceptions.OrderNotFoundException;
import org.example.order.mappers.OrderMapper;
import org.example.order.services.OrderManagerService;
import org.example.order.web.request.DefaultPageRequest;
import org.example.order.repositories.OrderRepository;
import org.example.order.web.request.PagingUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final OrderManagerService orderManagerService;
    private final OrderDeletionService orderDeletionService;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Order saved = orderManagerService.persistNewOrder(request);
        return orderMapper.toOrderDto(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(UUID id) {
        return orderMapper.toOrderDto(getByIdAndDeletedFalse(id));
    }

    @Transactional
    public void cancelOrder(UUID id) {
        Order order = getByIdAndDeletedFalse(id);
        orderManagerService.cancelOrder(order);
    }

    @Transactional
    public void updateOrderAdditionalNotes(UUID id, String orderNotes) {
        Order order = getByIdAndDeletedFalse(id);
        orderManagerService.updateAdditionalNotes(order, orderNotes);
    }

    @Transactional
    public void updateOrderStatus(UUID id, OrderStatus orderStatus, PaymentStatus paymentStatus,
            SourceOfChange sourceOfChange) {
        Order order = getByIdAndDeletedFalse(id);
        orderManagerService.updateOrderStatus(order,orderStatus,paymentStatus, sourceOfChange);
    }

    @Transactional(readOnly = true)
    public OrdersPageResponseDTO getAllOrders(DefaultPageRequest request) {
        Pageable page = PagingUtils.toPageable(request);

        Page<OrdersListResponseDTO> result = orderRepository.findByDeletedFalse(page)
                .map(orderMapper::toOrderListDto);

        return OrdersPageResponseDTO.from(result);
    }
    @Transactional
    public void softDeleteOrder(UUID id) {
        orderDeletionService.deleteOrder(id);
    }
    @Transactional
    public void restoreCanceledOrder(UUID id) {
        var order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        orderManagerService.restoreOrder(order);
    }

    private Order getByIdAndDeletedFalse(UUID id) {
        return orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id));
    }
}

