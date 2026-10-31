package org.example.order.services.impl;

import java.util.UUID;

public class OrderExpirationException extends RuntimeException {

    public OrderExpirationException(UUID orderId, Throwable cause) {
        super("Failed to expire order " + orderId, cause);
    }
}
