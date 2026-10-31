package org.example.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.order.domain.enums.AggregateType;
import org.example.order.domain.enums.EventType;
import org.example.order.domain.enums.OutboxEventStatus;


import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends AbstractEntity {

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;
}
