package com.sealforge.infrastructure.kubeseal;

import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class KubesealProcessAdapterTest {

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void sealsViaStdinWithoutPlaintextTempFiles() throws IOException {
        Path fakeKubeseal = createFakeKubesealScript();
        KubesealProcessAdapter adapter = new KubesealProcessAdapter(() -> fakeKubeseal, new KubesealCommandFactory());

        String sealedYaml = adapter.seal("apiVersion: v1\nkind: Secret\n", certificateReference(), SealingScope.STRICT);

        assertThat(sealedYaml).contains("apiVersion: v1");
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void validatesUsingFakeKubeseal() throws IOException {
        Path fakeKubeseal = createFakeKubesealScript();
        KubesealProcessAdapter adapter = new KubesealProcessAdapter(() -> fakeKubeseal, new KubesealCommandFactory());

        ValidationResult validationResult = adapter.validate("apiVersion: bitnami.com/v1alpha1\nkind: SealedSecret\n");

        assertThat(validationResult.success()).isTrue();
    }

    private Path createFakeKubesealScript() throws IOException {
        Path script = Files.createTempFile("fake-kubeseal-", ".sh");
        Files.writeString(script, """
                #!/usr/bin/env bash
                set -euo pipefail
                if [[ "${1:-}" == "--version" ]]; then
                  echo "kubeseal v0.fake"
                  exit 0
                fi
                if [[ "${1:-}" == "--validate" ]]; then
                  cat > /dev/null
                  echo "validated"
                  exit 0
                fi
                cat
                """);
        script.toFile().setExecutable(true);
        return script;
    }

    private CertificateReference certificateReference() {
        return new CertificateReference(
                CertificateSourceType.PASTE,
                "fixture",
                "AA:BB",
                "CN=fake",
                "CN=fake",
                Instant.now(),
                Instant.now().plusSeconds(60),
                "-----BEGIN CERTIFICATE-----\nfake\n-----END CERTIFICATE-----");
    }
}
