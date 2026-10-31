package org.example.order.services;

import org.example.order.domain.dto.request.OrderRequestDTO;
import org.example.order.domain.entity.Order;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;

public interface OrderManagerService {

    Order persistNewOrder(OrderRequestDTO request);

    void updateAdditionalNotes(Order order, String orderNotes);

    void updateOrderStatus(Order order, OrderStatus orderStatus, PaymentStatus paymentStatus, SourceOfChange sourceOfChange);

    void cancelOrder(Order order);

    void restoreOrder(Order order);
}
