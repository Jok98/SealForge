package com.sealforge.domain.validation;

import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.exception.ValidationException;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.SecretEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretDraftValidatorTest {

    private final SecretDraftValidator validator = new SecretDraftValidator();

    @Test
    void acceptsValidDraft() {
        SecretDraft draft = new SecretDraft(
                "docker-credentials",
                "payments",
                "Opaque",
                SealingScope.STRICT,
                List.of(new SecretEntry("username", "demo")));

        assertThatCode(() -> validator.validate(draft)).doesNotThrowAnyException();
    }

    @Test
    void rejectsDuplicateKeys() {
        SecretDraft draft = new SecretDraft(
                "docker-credentials",
                "payments",
                "Opaque",
                SealingScope.STRICT,
                List.of(
                        new SecretEntry("username", "demo"),
                        new SecretEntry("username", "another")));

        assertThatThrownBy(() -> validator.validate(draft))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Duplicate secret entry keys");
    }

    @Test
    void rejectsInvalidNamespace() {
        SecretDraft draft = new SecretDraft(
                "docker-credentials",
                "Payments",
                "Opaque",
                SealingScope.STRICT,
                List.of(new SecretEntry("username", "demo")));

        assertThatThrownBy(() -> validator.validate(draft))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Namespace");
    }
}

