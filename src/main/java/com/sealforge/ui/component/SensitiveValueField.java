package com.sealforge.ui.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public final class SensitiveValueField extends HBox {

    private final StringProperty text = new SimpleStringProperty("");
    private final PasswordField maskedField = new PasswordField();
    private final TextField plainField = new TextField();
    private final Button revealButton = new Button("Show");
    private boolean revealed;

    public SensitiveValueField() {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        maskedField.textProperty().bindBidirectional(text);
        plainField.textProperty().bindBidirectional(text);
        maskedField.setPromptText("Sensitive value");
        plainField.setPromptText("Sensitive value");

        plainField.setManaged(false);
        plainField.setVisible(false);

        StackPane inputHolder = new StackPane(maskedField, plainField);
        HBox.setHgrow(inputHolder, Priority.ALWAYS);
        HBox.setHgrow(maskedField, Priority.ALWAYS);
        HBox.setHgrow(plainField, Priority.ALWAYS);

        revealButton.setOnAction(event -> toggleReveal());
        getChildren().addAll(inputHolder, revealButton);
    }

    public StringProperty textProperty() {
        return text;
    }

    public void clear() {
        text.set("");
    }

    private void toggleReveal() {
        revealed = !revealed;
        plainField.setManaged(revealed);
        plainField.setVisible(revealed);
        maskedField.setManaged(!revealed);
        maskedField.setVisible(!revealed);
        revealButton.setText(revealed ? "Hide" : "Show");
    }
}

