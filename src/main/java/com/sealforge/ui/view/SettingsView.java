package com.sealforge.ui.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class SettingsView {

    private final ScrollPane root = new ScrollPane();
    private final TextField kubesealExecutableField = new TextField();
    private final Button browseKubesealButton = new Button("Browse");
    private final TextField defaultSecretTypeField = new TextField();
    private final Label kubesealStatusLabel = new Label("-");
    private final Label saveStatusLabel = new Label("Settings are non-sensitive and saved locally.");
    private final Button saveButton = new Button("Save Settings");
    private final Button restoreDefaultsButton = new Button("Restore Defaults");

    public SettingsView() {
        buildLayout();
    }

    public Parent root() {
        return root;
    }

    public TextField kubesealExecutableField() {
        return kubesealExecutableField;
    }

    public Button browseKubesealButton() {
        return browseKubesealButton;
    }

    public TextField defaultSecretTypeField() {
        return defaultSecretTypeField;
    }

    public Button saveButton() {
        return saveButton;
    }

    public Button restoreDefaultsButton() {
        return restoreDefaultsButton;
    }

    public void setKubesealStatus(boolean available, String executablePath) {
        kubesealStatusLabel.setText(available
                ? "kubeseal ready at " + executablePath
                : "kubeseal could not be executed from " + executablePath);
    }

    public void setSaveStatus(String message) {
        saveStatusLabel.setText(message);
    }

    private void buildLayout() {
        kubesealExecutableField.setPromptText("kubeseal");
        defaultSecretTypeField.setPromptText("Opaque");

        VBox content = new VBox(
                16,
                section(
                        "Execution",
                        wrappedLabel("Configure the kubeseal executable path. Changes apply immediately to future generate and validate operations."),
                        fieldRow("kubeseal Executable", kubesealExecutableField, browseKubesealButton),
                        metadataRow("Current Status", kubesealStatusLabel)),
                section(
                        "Defaults",
                        wrappedLabel("The default Secret type is applied when you create a new draft or reset the current one."),
                        fieldRow("Default Secret Type", defaultSecretTypeField)),
                section(
                        "Persistence",
                        wrappedLabel("Only non-sensitive settings are stored locally. Secret values and generated plaintext are not persisted automatically."),
                        saveStatusLabel,
                        new HBox(8, saveButton, restoreDefaultsButton)));

        content.setPadding(new Insets(6, 0, 12, 0));
        root.setContent(content);
        root.setFitToWidth(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private VBox section(String title, javafx.scene.Node... children) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox container = new VBox(10, titleLabel);
        container.getChildren().addAll(children);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 16px; -fx-border-color: #d7e2f0; -fx-border-radius: 16px;");
        return container;
    }

    private HBox fieldRow(String label, TextField textField, javafx.scene.Node... trailingNodes) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(150);
        HBox row = new HBox(12, labelNode, textField);
        HBox.setHgrow(textField, Priority.ALWAYS);
        row.getChildren().addAll(trailingNodes);
        return row;
    }

    private HBox metadataRow(String label, Label valueLabel) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(150);
        valueLabel.setWrapText(true);
        HBox row = new HBox(12, labelNode, valueLabel);
        HBox.setHgrow(valueLabel, Priority.ALWAYS);
        return row;
    }

    private Label wrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        return label;
    }
}
