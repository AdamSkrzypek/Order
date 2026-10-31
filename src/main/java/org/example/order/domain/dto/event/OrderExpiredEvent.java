package org.example.order.domain.dto.event;

import java.util.UUID;

public record OrderExpiredEvent(UUID orderIdDeleted) {
}
