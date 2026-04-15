package com.sealforge.application.service;

import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.model.CertificateReference;

public interface CertificateParser {

    CertificateReference parse(String pemContent, CertificateSourceType sourceType, String sourceDescription);
}

