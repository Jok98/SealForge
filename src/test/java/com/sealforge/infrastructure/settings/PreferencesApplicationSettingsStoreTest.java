package com.sealforge.infrastructure.settings;

import com.sealforge.config.ApplicationSettings;
import org.junit.jupiter.api.Test;

import java.util.prefs.Preferences;

import static org.assertj.core.api.Assertions.assertThat;

class PreferencesApplicationSettingsStoreTest {

    @Test
    void loadsDefaultsWhenNothingWasSaved() throws Exception {
        Preferences preferences = Preferences.userRoot().node("/com/sealforge/test/defaults");
        try {
            preferences.clear();
            PreferencesApplicationSettingsStore store = new PreferencesApplicationSettingsStore(preferences);

            ApplicationSettings settings = store.load();

            assertThat(settings).isEqualTo(ApplicationSettings.defaults());
        } finally {
            preferences.removeNode();
            preferences.flush();
        }
    }

    @Test
    void savesAndLoadsSettings() throws Exception {
        Preferences preferences = Preferences.userRoot().node("/com/sealforge/test/save");
        try {
            preferences.clear();
            PreferencesApplicationSettingsStore store = new PreferencesApplicationSettingsStore(preferences);
            ApplicationSettings savedSettings = new ApplicationSettings("/opt/tools/kubeseal", "kubernetes.io/basic-auth");

            store.save(savedSettings);

            assertThat(store.load()).isEqualTo(savedSettings);
        } finally {
            preferences.removeNode();
            preferences.flush();
        }
    }
}
