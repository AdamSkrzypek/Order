package org.example.order.exceptions;

public class InvalidOrderPageRequestException extends RuntimeException {

    public InvalidOrderPageRequestException(String message) {
        super(message);
    }
}
