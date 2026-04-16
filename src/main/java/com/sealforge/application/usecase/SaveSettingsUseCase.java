package com.sealforge.application.usecase;

import com.sealforge.application.service.ApplicationSettingsStore;
import com.sealforge.config.ApplicationSettings;
import com.sealforge.config.RuntimeSettings;
import com.sealforge.domain.exception.ValidationException;

public final class SaveSettingsUseCase {

    private final RuntimeSettings runtimeSettings;
    private final ApplicationSettingsStore settingsStore;

    public SaveSettingsUseCase(RuntimeSettings runtimeSettings, ApplicationSettingsStore settingsStore) {
        this.runtimeSettings = runtimeSettings;
        this.settingsStore = settingsStore;
    }

    public ApplicationSettings execute(ApplicationSettings applicationSettings) {
        ApplicationSettings normalizedSettings = applicationSettings.normalized();
        if (normalizedSettings.defaultSecretType().isBlank()) {
            throw new ValidationException("Default secret type is required.");
        }

        settingsStore.save(normalizedSettings);
        runtimeSettings.update(normalizedSettings);
        return normalizedSettings;
    }
}
