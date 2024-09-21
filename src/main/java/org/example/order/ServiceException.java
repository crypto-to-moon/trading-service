package org.example.order;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
