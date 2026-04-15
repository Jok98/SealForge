package com.sealforge.infrastructure.certificate;

import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.model.CertificateReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PemCertificateParserTest {

    @Test
    void parsesCertificateMetadataAndFingerprint() throws IOException {
        String pemContent = readResource("/test-certificate.pem");

        CertificateReference certificateReference = new PemCertificateParser()
                .parse(pemContent, CertificateSourceType.FILE, "fixture");

        assertThat(certificateReference.fingerprint())
                .isEqualTo("92:B9:F4:5D:94:31:A5:C4:02:99:AD:46:F1:04:8B:E3:45:A8:71:B0:2E:64:9F:FC:36:75:12:30:9E:9F:8F:56");
        assertThat(certificateReference.subject()).contains("CN=sealforge-test");
        assertThat(certificateReference.issuer()).contains("CN=sealforge-test");
    }

    @Test
    void parsesCertificateWithEmptyIssuerAndSubjectDn() throws IOException {
        String pemContent = readResource("/empty-dn-certificate.pem");

        CertificateReference certificateReference = new PemCertificateParser()
                .parse(pemContent, CertificateSourceType.FILE, "empty-dn-fixture");

        assertThat(certificateReference.fingerprint())
                .isEqualTo("54:13:1B:A5:EE:1E:24:0B:85:01:E3:CC:C6:D4:1B:52:06:6E:36:0B:6F:E3:0F:AB:A4:BD:B8:20:4A:B2:2C:EB");
        assertThat(certificateReference.subject()).isEqualTo("(empty)");
        assertThat(certificateReference.issuer()).isEqualTo("(empty)");
    }

    private String readResource(String resourcePath) throws IOException {
        return new String(
                getClass().getResourceAsStream(resourcePath).readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
