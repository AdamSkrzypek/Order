package org.example.order.web.request;

import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;

public record FilterDto(
        OrderStatus previousOrderStatus,
        OrderStatus nextOrderStatus,
        PaymentStatus previousPaymentStatus,
        PaymentStatus nextPaymentStatus,
        SourceOfChange sourceOfChange
) {
}
