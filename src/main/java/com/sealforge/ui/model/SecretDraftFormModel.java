package com.sealforge.ui.model;

import com.sealforge.domain.enumtype.SealingScope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class SecretDraftFormModel {

    private final StringProperty certificatePem = new SimpleStringProperty("");
    private final StringProperty secretName = new SimpleStringProperty("");
    private final StringProperty namespace = new SimpleStringProperty("");
    private final StringProperty secretType = new SimpleStringProperty("Opaque");
    private final ObjectProperty<SealingScope> scope = new SimpleObjectProperty<>(SealingScope.STRICT);
    private final ObservableList<SecretEntryRowModel> entries = FXCollections.observableArrayList();

    public StringProperty certificatePemProperty() {
        return certificatePem;
    }

    public StringProperty secretNameProperty() {
        return secretName;
    }

    public StringProperty namespaceProperty() {
        return namespace;
    }

    public StringProperty secretTypeProperty() {
        return secretType;
    }

    public ObjectProperty<SealingScope> scopeProperty() {
        return scope;
    }

    public ObservableList<SecretEntryRowModel> entries() {
        return entries;
    }
}

