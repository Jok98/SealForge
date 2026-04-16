package com.sealforge.config;

public record ApplicationSettings(
        String kubesealExecutable,
        String defaultSecretType) {

    public static ApplicationSettings defaults() {
        return new ApplicationSettings("kubeseal", "Opaque");
    }

    public ApplicationSettings normalized() {
        String normalizedKubesealExecutable = kubesealExecutable == null || kubesealExecutable.isBlank()
                ? defaults().kubesealExecutable()
                : kubesealExecutable.strip();
        String normalizedDefaultSecretType = defaultSecretType == null || defaultSecretType.isBlank()
                ? defaults().defaultSecretType()
                : defaultSecretType.strip();
        return new ApplicationSettings(normalizedKubesealExecutable, normalizedDefaultSecretType);
    }
}
