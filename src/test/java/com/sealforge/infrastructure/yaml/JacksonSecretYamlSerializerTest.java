package com.sealforge.infrastructure.yaml;

import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.SecretEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonSecretYamlSerializerTest {

    @Test
    void rendersDeterministicSecretYaml() {
        SecretDraft draft = new SecretDraft(
                "docker-credentials",
                "payments",
                "Opaque",
                SealingScope.STRICT,
                List.of(
                        new SecretEntry("username", "demo"),
                        new SecretEntry("password", "s3cret")));

        String yaml = new JacksonSecretYamlSerializer().render(draft);

        assertThat(yaml).isEqualTo("""
                apiVersion: "v1"
                kind: "Secret"
                metadata:
                  name: "docker-credentials"
                  namespace: "payments"
                type: "Opaque"
                stringData:
                  username: "demo"
                  password: "s3cret"
                """);
    }
}

