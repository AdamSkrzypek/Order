package org.example.order.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.order.domain.dto.request.OrderKafkaRequestDTO;
import org.example.order.domain.dto.request.OrderRequestDTO;
import org.example.order.domain.entity.Customer;
import org.example.order.domain.entity.Order;
import org.example.order.domain.entity.OrderVersion;
import org.example.order.domain.enums.AggregateType;
import org.example.order.domain.enums.EventType;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;
import org.example.order.exceptions.OrderStateConflictException;
import org.example.order.mappers.OrderMapper;
import org.example.order.repositories.OrderRepository;
import org.example.order.services.OrderManagerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class OrderManagerServiceImpl implements OrderManagerService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final OutboxEventService outboxEventService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order persistNewOrder(OrderRequestDTO request) {

        Customer customer = customerService.findOrCreate(request.customerDetailsRequest());
        Order order = orderMapper.toEntity(request);
        order.setCustomer(customer);
        order.setTotalAmount(order.calculateTotal());
        order.addOrderVersion(OrderVersion.initial(
                order.getStatus(), order.getPaymentStatus(), SourceOfChange.API));
        Order saved = orderRepository.save(order);
        OrderKafkaRequestDTO eventPayload = orderMapper.toKafkaRequest(saved, request);
        outboxEventService.recordEvent(AggregateType.ORDER, saved.getId(), EventType.ORDER_CREATED, eventPayload);
        return saved;
    }

    public void updateAdditionalNotes(Order order, String orderNotes) {
        order.updateOrderWithAdditionalNotes(orderNotes);
    }

    public void updateOrderStatus(Order order, OrderStatus newOrderStatus, PaymentStatus newPaymentStatus,
            SourceOfChange sourceOfChange) {
        OrderStatus previousOrderStatus = order.getStatus();
        PaymentStatus previousPaymentStatus = order.getPaymentStatus();

        if (previousOrderStatus == newOrderStatus
                && previousPaymentStatus == newPaymentStatus) {
            return;
        }

        order.applyStatusUpdate(newOrderStatus, newPaymentStatus);
        order.addOrderVersion(OrderVersion.statusChanged(previousOrderStatus,
                previousPaymentStatus,
                newOrderStatus,
                newPaymentStatus,
                sourceOfChange));

    }

    @Override
    public void cancelOrder(Order order) {
        OrderStatus oldValue = order.getStatus();
        if (oldValue == OrderStatus.CANCELLED) {
            throw new OrderStateConflictException("Can't cancel order because order has been cancelled");
        }
        order.cancel();
        order.addOrderVersion(OrderVersion.statusChanged(
                oldValue, order.getPaymentStatus(), order.getStatus(),
                order.getPaymentStatus(), SourceOfChange.API));
    }

    @Override
    public void restoreOrder(Order order) {
        if (Boolean.FALSE.equals(order.getDeleted())) {
            throw new OrderStateConflictException("Can't restore order because order is not deleted");
        }
        order.setDeleted(Boolean.FALSE);
        order.addOrderVersion(OrderVersion.restored(
                order.getStatus(),
                order.getPaymentStatus(),
                SourceOfChange.API));
    }
}
