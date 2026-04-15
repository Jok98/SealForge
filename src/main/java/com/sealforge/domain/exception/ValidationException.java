package com.sealforge.domain.exception;

public final class ValidationException extends UserInputException {

    public ValidationException(String userMessage) {
        super(userMessage);
    }
}

