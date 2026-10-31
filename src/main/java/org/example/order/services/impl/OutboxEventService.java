package org.example.order.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.order.domain.entity.OutboxEvent;
import org.example.order.domain.enums.AggregateType;
import org.example.order.domain.enums.EventType;
import org.example.order.domain.enums.OutboxEventStatus;
import org.example.order.repositories.OutboxEventRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public void recordEvent(AggregateType aggregateType, UUID aggregateId, EventType eventType, Object eventPayload) {
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(eventPayload, "eventPayload must not be null");

        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .eventType(eventType)
                .payload(serialize(eventType, eventPayload))
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        outboxEventRepository.save(event);
    }

    private String serialize(EventType eventType, Object eventPayload) {
        try {
            return objectMapper.writeValueAsString(eventPayload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Could not serialize outbox event " + eventType, e);
        }
    }
}
