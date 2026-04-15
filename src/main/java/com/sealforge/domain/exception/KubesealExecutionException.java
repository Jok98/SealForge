package com.sealforge.domain.exception;

public final class KubesealExecutionException extends TechnicalFailureException {

    public KubesealExecutionException(String userMessage, String technicalDetails) {
        super(userMessage, technicalDetails);
    }

    public KubesealExecutionException(String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, technicalDetails, cause);
    }
}

