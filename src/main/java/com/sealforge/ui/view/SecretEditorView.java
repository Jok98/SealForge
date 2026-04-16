package com.sealforge.ui.view;

import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.model.CertificateReference;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class SecretEditorView {

    private static final String ERROR_LABEL_STYLE = "-fx-text-fill: #b42318; -fx-font-size: 12px;";
    private static final String INFO_LABEL_STYLE = "-fx-text-fill: #475467;";
    private static final String ERROR_FIELD_STYLE = "-fx-border-color: #d92d20; -fx-border-radius: 6px;";

    private final ScrollPane root = new ScrollPane();

    private final TextArea certificateTextArea = new TextArea();
    private final Button inspectCertificateButton = new Button("Inspect Certificate");
    private final Button loadCertificateFileButton = new Button("Load PEM File");
    private final Label certificateStatusLabel = new Label("No certificate loaded.");
    private final Label certificateFingerprintLabel = new Label("-");
    private final Label certificateSubjectLabel = new Label("-");
    private final Label certificateIssuerLabel = new Label("-");

    private final Label kubesealStatusLabel = new Label("-");
    private final TextField secretNameField = new TextField();
    private final TextField namespaceField = new TextField();
    private final TextField secretTypeField = new TextField();
    private final ComboBox<SealingScope> scopeComboBox = new ComboBox<>();
    private final Label scopeDescriptionLabel = new Label();
    private final Label certificateErrorLabel = validationLabel("certificate-error-label");
    private final Label secretNameErrorLabel = validationLabel("secret-name-error-label");
    private final Label namespaceErrorLabel = validationLabel("namespace-error-label");
    private final Label secretTypeErrorLabel = validationLabel("secret-type-error-label");
    private final Label entriesErrorLabel = validationLabel("entries-error-label");

    private final VBox entryRowsContainer = new VBox(8);
    private final Button addEntryButton = new Button("Add Entry");
    private final Button generateButton = new Button("Generate Preview");
    private final Button resetButton = new Button("Reset Draft");
    private final Label editorStatusLabel = new Label("Generate a preview to open the validation and export screen.");

    public SecretEditorView() {
        buildLayout();
    }

    public Parent root() {
        return root;
    }

    public TextArea certificateTextArea() {
        return certificateTextArea;
    }

    public Button inspectCertificateButton() {
        return inspectCertificateButton;
    }

    public Button loadCertificateFileButton() {
        return loadCertificateFileButton;
    }

    public TextField secretNameField() {
        return secretNameField;
    }

    public TextField namespaceField() {
        return namespaceField;
    }

    public TextField secretTypeField() {
        return secretTypeField;
    }

    public ComboBox<SealingScope> scopeComboBox() {
        return scopeComboBox;
    }

    public VBox entryRowsContainer() {
        return entryRowsContainer;
    }

    public Button addEntryButton() {
        return addEntryButton;
    }

    public Button generateButton() {
        return generateButton;
    }

    public Button resetButton() {
        return resetButton;
    }

    public void setKubesealStatus(boolean available, String executablePath) {
        kubesealStatusLabel.setText(available
                ? "kubeseal ready at " + executablePath
                : "kubeseal not found. Open Settings to configure an executable path.");
    }

    public void setCertificateDetails(CertificateReference certificateReference) {
        certificateStatusLabel.setText("Certificate loaded successfully.");
        certificateFingerprintLabel.setText(certificateReference.fingerprint());
        certificateSubjectLabel.setText(certificateReference.subject());
        certificateIssuerLabel.setText(certificateReference.issuer());
    }

    public void clearCertificateDetails() {
        certificateStatusLabel.setText("No certificate loaded.");
        certificateFingerprintLabel.setText("-");
        certificateSubjectLabel.setText("-");
        certificateIssuerLabel.setText("-");
    }

    public void setScopeDescription(SealingScope scope) {
        scopeDescriptionLabel.setText(scope == null ? "" : scope.description());
    }

    public void setEditorStatus(String message) {
        editorStatusLabel.setText(message);
        editorStatusLabel.setStyle(INFO_LABEL_STYLE);
    }

    public void setEditorError(String message) {
        editorStatusLabel.setText(message);
        editorStatusLabel.setStyle(ERROR_LABEL_STYLE);
    }

    public void clearInlineValidation() {
        setValidationState(certificateErrorLabel, null, certificateTextArea);
        setValidationState(secretNameErrorLabel, null, secretNameField);
        setValidationState(namespaceErrorLabel, null, namespaceField);
        setValidationState(secretTypeErrorLabel, null, secretTypeField);
        setValidationState(entriesErrorLabel, null, entryRowsContainer);
        setEditorStatus("Generate a preview to open the validation and export screen.");
    }

    public void clearCertificateError() {
        setValidationState(certificateErrorLabel, null, certificateTextArea);
    }

    public void clearSecretNameError() {
        setValidationState(secretNameErrorLabel, null, secretNameField);
    }

    public void clearNamespaceError() {
        setValidationState(namespaceErrorLabel, null, namespaceField);
    }

    public void clearSecretTypeError() {
        setValidationState(secretTypeErrorLabel, null, secretTypeField);
    }

    public void clearEntriesError() {
        setValidationState(entriesErrorLabel, null, entryRowsContainer);
    }

    public void showCertificateError(String message) {
        setValidationState(certificateErrorLabel, message, certificateTextArea);
    }

    public void showSecretNameError(String message) {
        setValidationState(secretNameErrorLabel, message, secretNameField);
    }

    public void showNamespaceError(String message) {
        setValidationState(namespaceErrorLabel, message, namespaceField);
    }

    public void showSecretTypeError(String message) {
        setValidationState(secretTypeErrorLabel, message, secretTypeField);
    }

    public void showEntriesError(String message) {
        setValidationState(entriesErrorLabel, message, entryRowsContainer);
    }

    private void buildLayout() {
        root.setId("secret-editor-root");
        certificateTextArea.setId("certificate-text-area");
        inspectCertificateButton.setId("inspect-certificate-button");
        loadCertificateFileButton.setId("load-certificate-file-button");
        secretNameField.setId("secret-name-field");
        namespaceField.setId("namespace-field");
        secretTypeField.setId("secret-type-field");
        scopeComboBox.setId("scope-combo-box");
        entryRowsContainer.setId("entry-rows-container");
        addEntryButton.setId("add-entry-button");
        generateButton.setId("generate-preview-button");
        resetButton.setId("reset-draft-button");
        editorStatusLabel.setId("editor-status-label");

        certificateTextArea.setPromptText("Paste a PEM encoded Sealed Secrets public certificate.");
        certificateTextArea.setPrefRowCount(10);

        secretNameField.setPromptText("my-secret");
        namespaceField.setPromptText("my-namespace");
        secretTypeField.setPromptText("Opaque");
        secretTypeField.setText("Opaque");

        scopeComboBox.getItems().setAll(SealingScope.values());
        scopeComboBox.setValue(SealingScope.STRICT);
        setScopeDescription(SealingScope.STRICT);

        VBox content = new VBox(
                16,
                section(
                        "Certificate",
                        wrappedLabel("Certificates are public cluster material. Secret entry values are not."),
                        certificateTextArea,
                        certificateErrorLabel,
                        new HBox(8, inspectCertificateButton, loadCertificateFileButton),
                        metadataRow("Status", certificateStatusLabel),
                        metadataRow("Fingerprint", certificateFingerprintLabel),
                        metadataRow("Subject", certificateSubjectLabel),
                        metadataRow("Issuer", certificateIssuerLabel)),
                section(
                        "Secret Metadata",
                        fieldRow("Secret Name", secretNameField, secretNameErrorLabel),
                        fieldRow("Namespace", namespaceField, namespaceErrorLabel),
                        fieldRow("Secret Type", secretTypeField, secretTypeErrorLabel),
                        fieldRow("Sealing Scope", scopeComboBox),
                        metadataRow("Scope Notes", scopeDescriptionLabel),
                        metadataRow("kubeseal", kubesealStatusLabel)),
                section(
                        "Secret Entries",
                        wrappedLabel("Duplicate keys are rejected. Secret values are masked by default."),
                        entryRowsContainer,
                        entriesErrorLabel,
                        addEntryButton),
                section(
                        "Actions",
                        new HBox(8, generateButton, resetButton),
                        editorStatusLabel));

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

    private VBox fieldRow(String label, javafx.scene.Node field, Label errorLabel) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(130);
        HBox row = new HBox(12, labelNode, field);
        HBox.setHgrow(field, Priority.ALWAYS);
        VBox container = new VBox(4, row, errorLabel);
        return container;
    }

    private HBox fieldRow(String label, javafx.scene.Node field) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(130);
        HBox row = new HBox(12, labelNode, field);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private HBox metadataRow(String label, Label value) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(130);
        value.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(value, Priority.ALWAYS);
        HBox row = new HBox(12, labelNode, value, spacer);
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

    private void setValidationState(Label label, String message, javafx.scene.Node target) {
        label.setText(message == null ? "" : message);
        target.setStyle(message == null || message.isBlank() ? "" : ERROR_FIELD_STYLE);
    }
}
