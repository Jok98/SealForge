package com.sealforge.application.dto;

import com.sealforge.domain.enumtype.CertificateSourceType;

public record CertificateLoadRequest(
        CertificateSourceType sourceType,
        String sourceDescription,
        String pemContent) {
}

