package com.aibert.dosw.domain.exception;

public class InsufficientHistoryException extends RuntimeException {
    public InsufficientHistoryException(String message) {
        super(message);
    }
}
