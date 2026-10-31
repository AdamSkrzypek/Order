package org.example.order.exceptions;

public class CustomerConflictException extends RuntimeException {
    public CustomerConflictException(String message) {
        super(message);
    }
}