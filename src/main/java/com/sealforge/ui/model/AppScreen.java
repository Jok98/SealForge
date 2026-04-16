package com.sealforge.ui.model;

public enum AppScreen {
    HOME("Home", "Workspace overview and safe starting points."),
    SECRET_EDITOR("Secret Editor", "Enter certificate material, secret metadata, and sensitive key/value entries."),
    PREVIEW("Preview", "Review generated YAML, validate through kubeseal, and export deliberately."),
    SETTINGS("Settings", "Configure non-sensitive application defaults and the kubeseal executable path."),
    ABOUT("About", "Product scope, security posture, and release intent.");

    private final String title;
    private final String subtitle;

    AppScreen(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }
}
