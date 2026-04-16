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

    private static final String ERROR_LABEL_STYLE = "-fx-text-fill: #b42318; -fx-font-size: 12px;";
    private static final String INFO_LABEL_STYLE = "-fx-text-fill: #475467;";
    private static final String ERROR_FIELD_STYLE = "-fx-border-color: #d92d20; -fx-border-radius: 6px;";

    private final ScrollPane root = new ScrollPane();
    private final TextField kubesealExecutableField = new TextField();
    private final Button browseKubesealButton = new Button("Browse");
    private final TextField defaultSecretTypeField = new TextField();
    private final Label kubesealStatusLabel = new Label("-");
    private final Label saveStatusLabel = new Label("Settings are non-sensitive and saved locally.");
    private final Label kubesealExecutableErrorLabel = validationLabel("kubeseal-executable-error-label");
    private final Label defaultSecretTypeErrorLabel = validationLabel("default-secret-type-error-label");
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
        saveStatusLabel.setStyle(INFO_LABEL_STYLE);
    }

    public void setSaveError(String message) {
        saveStatusLabel.setText(message);
        saveStatusLabel.setStyle(ERROR_LABEL_STYLE);
    }

    public void clearInlineValidation() {
        setValidationState(kubesealExecutableErrorLabel, null, kubesealExecutableField);
        setValidationState(defaultSecretTypeErrorLabel, null, defaultSecretTypeField);
        setSaveStatus("Settings are non-sensitive and saved locally.");
    }

    public void clearKubesealExecutableError() {
        setValidationState(kubesealExecutableErrorLabel, null, kubesealExecutableField);
    }

    public void clearDefaultSecretTypeError() {
        setValidationState(defaultSecretTypeErrorLabel, null, defaultSecretTypeField);
    }

    public void showKubesealExecutableError(String message) {
        setValidationState(kubesealExecutableErrorLabel, message, kubesealExecutableField);
    }

    public void showDefaultSecretTypeError(String message) {
        setValidationState(defaultSecretTypeErrorLabel, message, defaultSecretTypeField);
    }

    private void buildLayout() {
        root.setId("settings-root");
        kubesealExecutableField.setId("kubeseal-executable-field");
        browseKubesealButton.setId("browse-kubeseal-button");
        defaultSecretTypeField.setId("default-secret-type-field");
        kubesealStatusLabel.setId("settings-kubeseal-status-label");
        saveStatusLabel.setId("settings-save-status-label");
        saveButton.setId("save-settings-button");
        restoreDefaultsButton.setId("restore-default-settings-button");

        kubesealExecutableField.setPromptText("kubeseal");
        defaultSecretTypeField.setPromptText("Opaque");

        VBox content = new VBox(
                16,
                section(
                        "Execution",
                        wrappedLabel("Configure the kubeseal executable path. Changes apply immediately to future generate and validate operations."),
                        fieldRow("kubeseal Executable", kubesealExecutableField, kubesealExecutableErrorLabel, browseKubesealButton),
                        metadataRow("Current Status", kubesealStatusLabel)),
                section(
                        "Defaults",
                        wrappedLabel("The default Secret type is applied when you create a new draft or reset the current one."),
                        fieldRow("Default Secret Type", defaultSecretTypeField, defaultSecretTypeErrorLabel)),
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

    private VBox fieldRow(String label, TextField textField, Label errorLabel, javafx.scene.Node... trailingNodes) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(150);
        HBox row = new HBox(12, labelNode, textField);
        HBox.setHgrow(textField, Priority.ALWAYS);
        row.getChildren().addAll(trailingNodes);
        return new VBox(4, row, errorLabel);
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

    private Label validationLabel(String id) {
        Label label = new Label();
        label.setId(id);
        label.setWrapText(true);
        label.setStyle(ERROR_LABEL_STYLE);
        return label;
    }

    private void setValidationState(Label label, String message, TextField field) {
        label.setText(message == null ? "" : message);
        field.setStyle(message == null || message.isBlank() ? "" : ERROR_FIELD_STYLE);
    }
}
