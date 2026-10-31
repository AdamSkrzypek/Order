package org.example.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.publish.timeout-seconds:5}")
    private long publishTimeoutSeconds;

    public void publishSync(String key, String payload, String topic) {
        try {
            kafkaTemplate.send(topic, key, payload).get(publishTimeoutSeconds, TimeUnit.SECONDS);
            log.info("Successfully sent message synchronously for key {}", key);
        } catch (TimeoutException e) {
            log.error("Timed out after {}s sending message synchronously for key {}", publishTimeoutSeconds, key, e);
            throw new RuntimeException("Timed out sending message synchronously to Kafka", e);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send message synchronously for key {}", key, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send message synchronously to Kafka", e);
        }
    }
}

