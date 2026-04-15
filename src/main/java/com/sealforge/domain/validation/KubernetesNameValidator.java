package com.sealforge.domain.validation;

import java.util.regex.Pattern;

public final class KubernetesNameValidator {

    private static final Pattern DNS_SUBDOMAIN = Pattern.compile(
            "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    private KubernetesNameValidator() {
    }

    public static boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.length() <= 253
                && DNS_SUBDOMAIN.matcher(value).matches();
    }
}

