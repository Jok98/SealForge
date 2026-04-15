package com.sealforge.config;

import java.nio.file.Path;

public record AppConfig(
        String applicationName,
        Path kubesealExecutable,
        String defaultSecretType) {

    public static AppConfig load() {
        String configuredPath = System.getProperty("sealforge.kubeseal.path", "kubeseal");
        return new AppConfig("SealForge", Path.of(configuredPath), "Opaque");
    }
}

