package com.sealforge.application.usecase;

import com.sealforge.application.dto.CertificateLoadRequest;
import com.sealforge.application.service.CertificateParser;
import com.sealforge.domain.model.CertificateReference;

public final class LoadCertificateUseCase {

    private final CertificateParser certificateParser;

    public LoadCertificateUseCase(CertificateParser certificateParser) {
        this.certificateParser = certificateParser;
    }

    public CertificateReference execute(CertificateLoadRequest request) {
        return certificateParser.parse(request.pemContent(), request.sourceType(), request.sourceDescription());
    }
}

