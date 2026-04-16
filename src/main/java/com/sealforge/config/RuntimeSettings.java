package com.sealforge.config;

import java.nio.file.Path;

public final class RuntimeSettings {

    private ApplicationSettings currentSettings;

    public RuntimeSettings(ApplicationSettings initialSettings) {
        this.currentSettings = initialSettings.normalized();
    }

    public synchronized void update(ApplicationSettings updatedSettings) {
        this.currentSettings = updatedSettings.normalized();
    }

    public synchronized ApplicationSettings snapshot() {
        return currentSettings;
    }

    public synchronized Path kubesealExecutable() {
        return Path.of(currentSettings.kubesealExecutable());
    }

    public synchronized String kubesealExecutableText() {
        return currentSettings.kubesealExecutable();
    }

    public synchronized String defaultSecretType() {
        return currentSettings.defaultSecretType();
    }
}
