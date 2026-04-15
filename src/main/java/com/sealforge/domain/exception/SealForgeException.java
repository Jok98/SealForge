package com.sealforge.domain.exception;

public class SealForgeException extends RuntimeException {

    private final String userMessage;
    private final String technicalDetails;

    public SealForgeException(String userMessage) {
        this(userMessage, userMessage, null);
    }

    public SealForgeException(String userMessage, String technicalDetails) {
        this(userMessage, technicalDetails, null);
    }

    public SealForgeException(String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
    }

    public String userMessage() {
        return userMessage;
    }

    public String technicalDetails() {
        return technicalDetails;
    }
}

