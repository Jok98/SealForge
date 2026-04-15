package com.sealforge.domain.exception;

public class UserInputException extends SealForgeException {

    public UserInputException(String userMessage) {
        super(userMessage);
    }

    public UserInputException(String userMessage, String technicalDetails) {
        super(userMessage, technicalDetails);
    }

    public UserInputException(String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, technicalDetails, cause);
    }
}

