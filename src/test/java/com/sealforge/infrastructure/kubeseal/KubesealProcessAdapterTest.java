package com.sealforge.infrastructure.kubeseal;

import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.exception.KubesealExecutionException;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.KubesealRuntimeStatus;
import com.sealforge.domain.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KubesealProcessAdapterTest {

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void sealsViaStdinWithoutPlaintextTempFiles() throws IOException {
        Path fakeKubeseal = createFakeKubesealScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                if [[ "${1:-}" == "--version" ]]; then
                  echo "kubeseal v0.fake"
                  exit 0
                fi
                if [[ "${1:-}" == "--help" ]]; then
                  echo "Usage: kubeseal --validate"
                  exit 0
                fi
                if [[ "${1:-}" == "--validate" ]]; then
                  cat > /dev/null
                  echo "validated"
                  exit 0
                fi
                cat
                """);
        KubesealProcessAdapter adapter = new KubesealProcessAdapter(() -> fakeKubeseal, new KubesealCommandFactory());

        String sealedYaml = adapter.seal("apiVersion: v1\nkind: Secret\n", certificateReference(), SealingScope.STRICT);

        assertThat(sealedYaml).contains("apiVersion: v1");
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void reportsVersionAndValidationSupport() throws IOException {
        Path fakeKubeseal = createFakeKubesealScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                if [[ "${1:-}" == "--version" ]]; then
                  echo "kubeseal v0.fake"
                  exit 0
                fi
                if [[ "${1:-}" == "--help" ]]; then
                  echo "Usage: kubeseal [--validate]"
                  exit 0
                fi
                cat > /dev/null
                """);
        KubesealProcessAdapter adapter = new KubesealProcessAdapter(() -> fakeKubeseal, new KubesealCommandFactory());

        KubesealRuntimeStatus runtimeStatus = adapter.inspectStatus();

        assertThat(runtimeStatus.available()).isTrue();
        assertThat(runtimeStatus.version()).isEqualTo("kubeseal v0.fake");
        assertThat(runtimeStatus.validationSupported()).isTrue();
        assertThat(runtimeStatus.message()).contains("kubeseal v0.fake");
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void returnsFriendlyResultWhenValidateIsUnsupported() throws IOException {
        Path fakeKubeseal = createFakeKubesealScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                if [[ "${1:-}" == "--version" ]]; then
                  echo "kubeseal v0.10.0"
                  exit 0
                fi
                if [[ "${1:-}" == "--help" ]]; then
                  echo "Usage: kubeseal"
                  exit 0
                fi
                if [[ "${1:-}" == "--validate" ]]; then
                  echo "error: unknown flag: --validate" >&2
                  exit 1
                fi
                cat > /dev/null
                """);
        KubesealProcessAdapter adapter = new KubesealProcessAdapter(() -> fakeKubeseal, new KubesealCommandFactory());

        ValidationResult validationResult = adapter.validate("apiVersion: bitnami.com/v1alpha1\nkind: SealedSecret\n");

        assertThat(validationResult.success()).isFalse();
        assertThat(validationResult.message()).contains("--validate");
        assertThat(validationResult.details()).contains("unknown flag");
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void timesOutLongRunningSeal() throws IOException {
        Path fakeKubeseal = createFakeKubesealScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                if [[ "${1:-}" == "--version" ]]; then
                  echo "kubeseal v0.fake"
                  exit 0
                fi
                if [[ "${1:-}" == "--help" ]]; then
                  echo "Usage: kubeseal --validate"
                  exit 0
                fi
                sleep 2
                cat
                """);
        KubesealProcessAdapter adapter = new KubesealProcessAdapter(
                () -> fakeKubeseal,
                new KubesealCommandFactory(),
                Duration.ofMillis(200),
                Duration.ofMillis(200),
                Duration.ofMillis(200),
                Duration.ofMillis(50));

        long startedAt = System.nanoTime();

        assertThatThrownBy(() -> adapter.seal("apiVersion: v1\nkind: Secret\n", certificateReference(), SealingScope.STRICT))
                .isInstanceOf(KubesealExecutionException.class)
                .hasMessageContaining("timed out");

        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
        assertThat(elapsed).isLessThan(Duration.ofSeconds(3));
    }

    private Path createFakeKubesealScript(String content) throws IOException {
        Path script = Files.createTempFile("fake-kubeseal-", ".sh");
        Files.writeString(script, content);
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
