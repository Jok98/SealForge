package com.sealforge.application.service;

import com.sealforge.config.ApplicationSettings;

public interface ApplicationSettingsStore {

    ApplicationSettings load();

    void save(ApplicationSettings applicationSettings);
}
