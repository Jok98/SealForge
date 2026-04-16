package com.sealforge.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class SettingsFormModel {

    private final StringProperty kubesealExecutable = new SimpleStringProperty("");
    private final StringProperty defaultSecretType = new SimpleStringProperty("Opaque");

    public StringProperty kubesealExecutableProperty() {
        return kubesealExecutable;
    }

    public StringProperty defaultSecretTypeProperty() {
        return defaultSecretType;
    }
}
