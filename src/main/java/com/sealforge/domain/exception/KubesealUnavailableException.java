package com.sealforge.domain.exception;

public final class KubesealUnavailableException extends TechnicalFailureException {

    public KubesealUnavailableException(String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, technicalDetails, cause);
    }
}

