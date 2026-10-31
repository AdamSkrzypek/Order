package org.example.order.testsupport;

import org.example.order.domain.enums.AggregateType;
import org.example.order.domain.enums.EventType;
import org.example.order.repositories.OutboxEventRepository;
import org.example.order.services.impl.OutboxEventService;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

public class FailingOutboxEventService extends OutboxEventService {

    private volatile UUID failingOrderId;

    public FailingOutboxEventService(ObjectMapper objectMapper, OutboxEventRepository outboxEventRepository) {
        super(objectMapper, outboxEventRepository);
    }

    public void failForOrder(UUID orderId) {
        this.failingOrderId = orderId;
    }

    @Override
    public void recordEvent(
            AggregateType aggregateType,
            UUID aggregateId,
            EventType eventType,
            Object eventPayload) {
        super.recordEvent(aggregateType, aggregateId, eventType, eventPayload);
        if (aggregateId.equals(failingOrderId)) {
            throw new IllegalStateException("Simulated outbox write failure");
        }
    }
}
