package com.sealforge.infrastructure.kubeseal;

import com.sealforge.domain.enumtype.SealingScope;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KubesealCommandFactoryTest {

    private final KubesealCommandFactory commandFactory = new KubesealCommandFactory();

    @Test
    void buildsSealCommandWithCertAndScope() {
        assertThat(commandFactory.buildSealCommand(Path.of("/usr/local/bin/kubeseal"), Path.of("/tmp/cert.pem"), SealingScope.NAMESPACE_WIDE))
                .containsExactly("/usr/local/bin/kubeseal", "--cert", "/tmp/cert.pem", "--scope", "namespace-wide");
    }

    @Test
    void buildsValidateCommand() {
        assertThat(commandFactory.buildValidateCommand(Path.of("/usr/local/bin/kubeseal")))
                .containsExactly("/usr/local/bin/kubeseal", "--validate");
    }
}

