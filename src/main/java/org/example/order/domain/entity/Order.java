package org.example.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.exceptions.OrderStateConflictException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

import static org.example.order.domain.enums.OrderStatus.*;


@Entity
@AllArgsConstructor
@Table(name = "orders")
@NoArgsConstructor
@Getter
public class Order extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @Setter
    private Customer customer;

    @Embedded
    @Setter
    private ShippingAddress shippingAddress;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = NEW;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.NEW;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal totalAmount;

    @Column
    @Setter
    private String orderAdditionalNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderVersion> orderVersions = new HashSet<>();

    @Column(nullable = false)
    @Setter
    @Getter
    private Boolean deleted = Boolean.FALSE;

    public Set<OrderVersion> getOrderVersions() {
        return Collections.unmodifiableSet(orderVersions);
    }


    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public void addOrderVersion(OrderVersion orderVersion) {
        orderVersion.setOrder(this);
        orderVersions.add(orderVersion);
    }

    public void addOrderItem(OrderItem item) {
        item.setOrder(this);
        this.orderItems.add(item);
    }

    public void cancel() {
        if (status == CANCELLED) {
            return;
        }
        if (!canTransitionTo(status, CANCELLED)) {
            throw new OrderStateConflictException("Cannot cancel an order in status: " + status);
        }
        if (paymentStatus == PaymentStatus.PAID) {
            throw new OrderStateConflictException("A paid order cannot be cancelled without a refund");
        }
        this.status = CANCELLED;
    }

    public void applyStatusUpdate(OrderStatus newStatus, PaymentStatus resultingPaymentStatus) {
        if (newStatus == null || resultingPaymentStatus == null) {
            throw new OrderStateConflictException("Order and payment statuses must not be null");
        }
        if (!canTransitionTo(status, newStatus)) {
            throw new OrderStateConflictException(
                    "Cannot change order status from " + status + " to " + newStatus);
        }
        if (!canTransitionPaymentTo(paymentStatus,
                resultingPaymentStatus)) {
            throw new OrderStateConflictException(
                    "Cannot change payment status from " + paymentStatus + " to " + resultingPaymentStatus);
        }

        if ((newStatus == CANCELLED || newStatus == REJECTED)
                && resultingPaymentStatus == PaymentStatus.PAID) {
            throw new OrderStateConflictException(
                    "A paid order cannot be cancelled or rejected without a refund");
        }
        if ((newStatus == SHIPPED || newStatus == DELIVERED)
                && resultingPaymentStatus != PaymentStatus.PAID) {
            throw new OrderStateConflictException("An order must be paid before shipping or delivery");
        }

        this.status = newStatus;
        this.paymentStatus = resultingPaymentStatus;
    }

    private boolean canTransitionTo(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return true;
        }
        return switch (current) {
            case NEW -> EnumSet.of(PENDING, CANCELLED).contains(next);
            case PENDING -> EnumSet.of(APPROVED, REJECTED, CANCELLED).contains(next);
            case APPROVED -> EnumSet.of(SHIPPED, CANCELLED).contains(next);
            case SHIPPED -> next == DELIVERED;
            case REJECTED, CANCELLED, DELIVERED -> false;
        };
    }

    private boolean canTransitionPaymentTo(PaymentStatus current, PaymentStatus next) {
        if (current == next) {
            return true; // Idempotent duplicate payment event.
        }
        return switch (current) {
            case NEW -> true;
            case NOT_PAID -> next == PaymentStatus.PAID || next == PaymentStatus.FAILED;
            case FAILED -> next == PaymentStatus.NOT_PAID || next == PaymentStatus.PAID;
            case PAID -> false; // Refunds are not modeled yet.
        };
    }

    public void updateOrderWithAdditionalNotes (String orderNotes) {
        if(this.getStatus() == OrderStatus.SHIPPED || this.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderStateConflictException("Cannot update additional notes for an order that has been shipped or delivered.");
        }
        this.setOrderAdditionalNotes(orderNotes);
    }
    public BigDecimal calculateTotal() {
        return this.orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
