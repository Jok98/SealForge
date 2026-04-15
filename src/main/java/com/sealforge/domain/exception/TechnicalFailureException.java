package com.sealforge.domain.exception;

public class TechnicalFailureException extends SealForgeException {

    public TechnicalFailureException(String userMessage, String technicalDetails) {
        super(userMessage, technicalDetails);
    }

    public TechnicalFailureException(String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, technicalDetails, cause);
    }
}

