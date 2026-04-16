package com.sealforge.infrastructure.settings;

import com.sealforge.application.service.ApplicationSettingsStore;
import com.sealforge.config.ApplicationSettings;
import com.sealforge.domain.exception.TechnicalFailureException;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class PreferencesApplicationSettingsStore implements ApplicationSettingsStore {

    private static final String KUBESEAL_EXECUTABLE_KEY = "kubeseal.executable";
    private static final String DEFAULT_SECRET_TYPE_KEY = "default.secret.type";

    private final Preferences preferences;

    public PreferencesApplicationSettingsStore() {
        this(Preferences.userNodeForPackage(PreferencesApplicationSettingsStore.class).node("sealforge"));
    }

    public PreferencesApplicationSettingsStore(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public ApplicationSettings load() {
        ApplicationSettings defaults = ApplicationSettings.defaults();
        String kubesealExecutable = System.getProperty(
                "sealforge.kubeseal.path",
                preferences.get(KUBESEAL_EXECUTABLE_KEY, defaults.kubesealExecutable()));
        String defaultSecretType = preferences.get(DEFAULT_SECRET_TYPE_KEY, defaults.defaultSecretType());
        return new ApplicationSettings(kubesealExecutable, defaultSecretType).normalized();
    }

    @Override
    public void save(ApplicationSettings applicationSettings) {
        ApplicationSettings normalizedSettings = applicationSettings.normalized();
        preferences.put(KUBESEAL_EXECUTABLE_KEY, normalizedSettings.kubesealExecutable());
        preferences.put(DEFAULT_SECRET_TYPE_KEY, normalizedSettings.defaultSecretType());
        try {
            preferences.flush();
        } catch (BackingStoreException exception) {
            throw new TechnicalFailureException(
                    "The application settings could not be saved.",
                    exception.getMessage(),
                    exception);
        }
    }
}
