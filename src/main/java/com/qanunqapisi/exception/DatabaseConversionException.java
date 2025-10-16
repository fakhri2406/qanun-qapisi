package com.qanunqapisi.exception;

public class DatabaseConversionException extends RuntimeException {
    public DatabaseConversionException(String message) {
        super(message);
    }

    public DatabaseConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
