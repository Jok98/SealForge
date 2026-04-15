package com.sealforge.domain.model;

import com.sealforge.domain.enumtype.CertificateSourceType;

import java.time.Instant;

public record CertificateReference(
        CertificateSourceType sourceType,
        String sourceDescription,
        String fingerprint,
        String subject,
        String issuer,
        Instant notBefore,
        Instant notAfter,
        String pemContent) {
}

