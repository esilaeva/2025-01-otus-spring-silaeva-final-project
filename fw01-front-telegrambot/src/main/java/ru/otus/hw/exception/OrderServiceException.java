package ru.otus.hw.exception;

/**
 * Base exception for product service operations.
 * Provides specific exception types for different error scenarios.
 */
public class OrderServiceException extends RuntimeException {

    public OrderServiceException(String message) {
        super(message);
    }

    public OrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ServiceUnavailableException extends OrderServiceException {
        public ServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TimeoutException extends OrderServiceException {
        public TimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidArgumentException extends OrderServiceException {
        public InvalidArgumentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PermissionDeniedException extends OrderServiceException {
        public PermissionDeniedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AuthenticationException extends OrderServiceException {
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
