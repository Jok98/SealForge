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
    }

    private void buildLayout() {
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
                        new HBox(8, inspectCertificateButton, loadCertificateFileButton),
                        metadataRow("Status", certificateStatusLabel),
                        metadataRow("Fingerprint", certificateFingerprintLabel),
                        metadataRow("Subject", certificateSubjectLabel),
                        metadataRow("Issuer", certificateIssuerLabel)),
                section(
                        "Secret Metadata",
                        fieldRow("Secret Name", secretNameField),
                        fieldRow("Namespace", namespaceField),
                        fieldRow("Secret Type", secretTypeField),
                        fieldRow("Sealing Scope", scopeComboBox),
                        metadataRow("Scope Notes", scopeDescriptionLabel),
                        metadataRow("kubeseal", kubesealStatusLabel)),
                section(
                        "Secret Entries",
                        wrappedLabel("Duplicate keys are rejected. Secret values are masked by default."),
                        entryRowsContainer,
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
}
