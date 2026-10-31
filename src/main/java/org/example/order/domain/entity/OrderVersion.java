package org.example.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.OrderVersionType;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;

@Entity
@Table(name = "order_versions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@FieldNameConstants
public class OrderVersion extends AbstractEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_order_status", nullable = false)
    private OrderStatus previousOrderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_order_status", nullable = false)
    private OrderStatus nextOrderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_payment_status", nullable = false)
    private PaymentStatus previousPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_payment_status", nullable = false)
    private PaymentStatus nextPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_of_version")
    private SourceOfChange sourceOfChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "version_type", nullable = false, length = 32)
    private OrderVersionType versionType = OrderVersionType.STATUS_CHANGE;

    private OrderVersion(OrderStatus previousOrderStatus, PaymentStatus previousPaymentStatus,
            OrderStatus nextOrderStatus, PaymentStatus nextPaymentStatus,
            SourceOfChange sourceOfChange, OrderVersionType versionType) {
        this.previousOrderStatus = previousOrderStatus;
        this.previousPaymentStatus = previousPaymentStatus;
        this.nextOrderStatus = nextOrderStatus;
        this.nextPaymentStatus = nextPaymentStatus;
        this.sourceOfChange = sourceOfChange;
        this.versionType = versionType;
    }

    public static OrderVersion initial(OrderStatus status, PaymentStatus paymentStatus,
            SourceOfChange sourceOfChange) {
        return create(status, paymentStatus, status, paymentStatus,
                sourceOfChange, OrderVersionType.INITIAL);
    }

    public static OrderVersion statusChanged(OrderStatus previousOrderStatus,
            PaymentStatus previousPaymentStatus, OrderStatus nextOrderStatus,
            PaymentStatus nextPaymentStatus, SourceOfChange sourceOfChange) {
        return create(previousOrderStatus, previousPaymentStatus,
                nextOrderStatus, nextPaymentStatus,
                sourceOfChange, OrderVersionType.STATUS_CHANGE);
    }

    public static OrderVersion restored(OrderStatus status, PaymentStatus paymentStatus,
            SourceOfChange sourceOfChange) {
        return create(status, paymentStatus, status, paymentStatus,
                sourceOfChange, OrderVersionType.ORDER_RESTORED);
    }

    private static OrderVersion create(OrderStatus previousOrderStatus,
            PaymentStatus previousPaymentStatus, OrderStatus nextOrderStatus,
            PaymentStatus nextPaymentStatus, SourceOfChange sourceOfChange,
            OrderVersionType versionType) {
        return new OrderVersion(previousOrderStatus, previousPaymentStatus,
                nextOrderStatus, nextPaymentStatus, sourceOfChange, versionType);
    }

}
