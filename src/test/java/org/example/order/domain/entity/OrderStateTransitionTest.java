package org.example.order.domain.entity;

import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.exceptions.OrderStateConflictException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateTransitionTest {

    @Test
    void allowsValidLifecycleAndRequiresPaymentBeforeShipping() {
        Order order = new Order();

        order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.NOT_PAID);
        order.applyStatusUpdate(OrderStatus.APPROVED, PaymentStatus.PAID);
        assertThatThrownBy(() -> order.applyStatusUpdate(OrderStatus.SHIPPED, PaymentStatus.NOT_PAID))
                .isInstanceOf(OrderStateConflictException.class);

        order.applyStatusUpdate(OrderStatus.SHIPPED, PaymentStatus.PAID);
        order.applyStatusUpdate(OrderStatus.DELIVERED, PaymentStatus.PAID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void rejectsBackwardTransitionsAndUpdatesAfterTerminalState() {
        Order order = new Order();
        order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.NOT_PAID);
        order.applyStatusUpdate(OrderStatus.APPROVED, PaymentStatus.NOT_PAID);

        assertThatThrownBy(() -> order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.NOT_PAID))
                .isInstanceOf(OrderStateConflictException.class);

        order.applyStatusUpdate(OrderStatus.CANCELLED, PaymentStatus.NOT_PAID);
        assertThatThrownBy(() -> order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.NOT_PAID))
                .isInstanceOf(OrderStateConflictException.class);
    }

    @Test
    void rejectsCancellingOrRejectingPaidOrder() {
        Order order = new Order();
        order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.PAID);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderStateConflictException.class);
        assertThatThrownBy(() -> order.applyStatusUpdate(OrderStatus.REJECTED, PaymentStatus.PAID))
                .isInstanceOf(OrderStateConflictException.class);
    }

    @Test
    void acceptsDuplicateStatusEventIdempotently() {
        Order order = new Order();
        order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.NOT_PAID);

        order.applyStatusUpdate(OrderStatus.PENDING, PaymentStatus.NOT_PAID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.NOT_PAID);
    }
}
