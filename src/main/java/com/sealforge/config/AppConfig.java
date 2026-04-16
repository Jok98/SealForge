package com.sealforge.config;

public record AppConfig(String applicationName) {

    public static AppConfig load() {
        return new AppConfig("SealForge");
    }
}
