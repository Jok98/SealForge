package com.sealforge.domain.exception;

public final class CertificateParseException extends UserInputException {

    public CertificateParseException(String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage, technicalDetails, cause);
    }
}

