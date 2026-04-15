package com.sealforge.infrastructure.certificate;

import com.sealforge.application.service.CertificateParser;
import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.exception.CertificateParseException;
import com.sealforge.domain.model.CertificateReference;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;

import java.io.StringReader;
import java.security.MessageDigest;

public final class PemCertificateParser implements CertificateParser {

    @Override
    public CertificateReference parse(String pemContent, CertificateSourceType sourceType, String sourceDescription) {
        if (pemContent == null || pemContent.isBlank()) {
            throw new CertificateParseException(
                    "A PEM encoded certificate is required.",
                    "No PEM content was provided.",
                    null);
        }

        try {
            String normalizedPem = pemContent.strip();
            X509CertificateHolder certificate = parseCertificate(normalizedPem);

            return new CertificateReference(
                    sourceType,
                    sourceDescription,
                    toSha256Fingerprint(certificate),
                    toDisplayName(certificate.getSubject().toString()),
                    toDisplayName(certificate.getIssuer().toString()),
                    certificate.getNotBefore().toInstant(),
                    certificate.getNotAfter().toInstant(),
                    normalizedPem);
        } catch (Exception exception) {
            throw new CertificateParseException(
                    "The certificate could not be parsed. Check that the PEM content is valid.",
                    exception.getMessage(),
                    exception);
        }
    }

    private X509CertificateHolder parseCertificate(String pemContent) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemContent))) {
            Object parsedObject = pemParser.readObject();
            if (parsedObject instanceof X509CertificateHolder certificateHolder) {
                return certificateHolder;
            }
            throw new CertificateParseException(
                    "The certificate could not be parsed. Check that the PEM content is valid.",
                    "PEM content did not contain an X.509 certificate.",
                    null);
        }
    }

    private String toDisplayName(String dn) {
        return dn == null || dn.isBlank() ? "(empty)" : dn;
    }

    private String toSha256Fingerprint(X509CertificateHolder certificate) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(certificate.getEncoded());
        StringBuilder fingerprint = new StringBuilder();
        for (int index = 0; index < hash.length; index++) {
            if (index > 0) {
                fingerprint.append(':');
            }
            fingerprint.append(String.format("%02X", hash[index]));
        }
        return fingerprint.toString();
    }
}
