package com.sealforge.domain.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesNameValidatorTest {

    @Test
    void acceptsValidDnsSubdomain() {
        assertThat(KubernetesNameValidator.isValid("payments-service.v2")).isTrue();
    }

    @Test
    void rejectsUppercaseCharacters() {
        assertThat(KubernetesNameValidator.isValid("Payments")).isFalse();
    }
}

