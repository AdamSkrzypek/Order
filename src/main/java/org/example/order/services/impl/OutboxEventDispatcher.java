package org.example.order.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.entity.OutboxEvent;
import org.example.order.domain.enums.EventType;
import org.example.order.domain.enums.OutboxEventStatus;
import org.example.order.kafka.OrderEventPublisher;
import org.example.order.repositories.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventDispatcher {

    private static final int MAX_RETRY_COUNT = 3;
    private static final int BATCH_SIZE = 10;

    @Value("${topics.order.request}")
    private String orderRequestTopic;

    @Value("${topics.order.expired}")
    private String orderExpiredTopic;

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void dispatchPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findAndLockByStatus(
                OutboxEventStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE));

        events.forEach(this::dispatch);
    }

    private void dispatch(OutboxEvent event) {
        try {
            orderEventPublisher.publishSync(
                    event.getAggregateId().toString(),
                    event.getPayload(),
                    topicFor(event.getEventType()));
            outboxEventRepository.delete(event);
            log.info("Published outbox event {}", event.getId());
        } catch (Exception e) {
            markFailed(event, e);
        }
    }

    private String topicFor(EventType eventType) {
        return switch (eventType) {
            case ORDER_CREATED -> orderRequestTopic;
            case ORDER_EXPIRED -> orderExpiredTopic;
        };
    }

    private void markFailed(OutboxEvent event, Exception exception) {
        log.error("Failed to publish outbox event {}", event.getId(), exception);

        int retryCount = event.getRetryCount() + 1;
        event.setRetryCount(retryCount);
        if (retryCount >= MAX_RETRY_COUNT) {
            event.setStatus(OutboxEventStatus.FAILED);
            log.error("Outbox event {} exceeded max retries", event.getId());
        }
    }
}
