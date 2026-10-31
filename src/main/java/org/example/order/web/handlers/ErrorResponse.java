package org.example.order.web.handlers;

import java.time.Instant;

public record ErrorResponse(String message, int status, Instant timestamp) {}