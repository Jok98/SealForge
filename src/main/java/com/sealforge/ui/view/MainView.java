package com.sealforge.ui.view;

import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.GeneratedYaml;
import com.sealforge.domain.model.ValidationResult;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class MainView {

    private final BorderPane root = new BorderPane();

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

    private final Button generateButton = new Button("Generate");
    private final Button validateButton = new Button("Validate SealedSecret");
    private final Button copySecretButton = new Button("Copy Secret YAML");
    private final Button copySealedButton = new Button("Copy SealedSecret YAML");
    private final Button exportSecretButton = new Button("Export Secret YAML");
    private final Button exportSealedButton = new Button("Export SealedSecret YAML");
    private final Button resetButton = new Button("Reset");

    private final TextArea secretYamlArea = readOnlyArea();
    private final TextArea sealedSecretYamlArea = readOnlyArea();
    private final Label validationStatusLabel = new Label("Validation not run yet.");
    private final TextArea technicalDetailsArea = readOnlyArea();

    public MainView() {
        configureUi();
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

    public Button addEntryButton() {
        return addEntryButton;
    }

    public Button generateButton() {
        return generateButton;
    }

    public Button validateButton() {
        return validateButton;
    }

    public Button copySecretButton() {
        return copySecretButton;
    }

    public Button copySealedButton() {
        return copySealedButton;
    }

    public Button exportSecretButton() {
        return exportSecretButton;
    }

    public Button exportSealedButton() {
        return exportSealedButton;
    }

    public Button resetButton() {
        return resetButton;
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

    public TextArea secretYamlArea() {
        return secretYamlArea;
    }

    public TextArea sealedSecretYamlArea() {
        return sealedSecretYamlArea;
    }

    public void setKubesealStatus(boolean available, String executablePath) {
        kubesealStatusLabel.setText(available
                ? "kubeseal available at " + executablePath
                : "kubeseal not found. Configure -Dsealforge.kubeseal.path or install it on PATH.");
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

    public void setGeneratedYaml(GeneratedYaml generatedYaml) {
        secretYamlArea.setText(generatedYaml.plainSecretYaml());
        sealedSecretYamlArea.setText(generatedYaml.sealedSecretYaml());
    }

    public void clearGeneratedYaml() {
        secretYamlArea.clear();
        sealedSecretYamlArea.clear();
    }

    public void setValidationResult(ValidationResult validationResult) {
        validationStatusLabel.setText(validationResult.message());
        technicalDetailsArea.setText(validationResult.details() == null ? "" : validationResult.details());
    }

    public void setTechnicalDetails(String details) {
        technicalDetailsArea.setText(details == null ? "" : details);
    }

    private void configureUi() {
        certificateTextArea.setPromptText("Paste a PEM encoded Sealed Secrets public certificate.");
        certificateTextArea.setPrefRowCount(10);

        secretNameField.setPromptText("my-secret");
        namespaceField.setPromptText("my-namespace");
        secretTypeField.setPromptText("Opaque");
        secretTypeField.setText("Opaque");

        scopeComboBox.getItems().setAll(SealingScope.values());
        scopeComboBox.setValue(SealingScope.STRICT);
        setScopeDescription(SealingScope.STRICT);

        technicalDetailsArea.setPrefRowCount(12);
        validationStatusLabel.setWrapText(true);
    }

    private void buildLayout() {
        root.setPadding(new Insets(16));

        Label titleLabel = new Label("SealForge");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(
                "Local-first authoring for Kubernetes Sealed Secrets. Plaintext values stay local and are never persisted by default.");
        subtitleLabel.setWrapText(true);

        VBox header = new VBox(6, titleLabel, subtitleLabel);
        header.setPadding(new Insets(0, 0, 16, 0));
        root.setTop(header);

        VBox formColumn = new VBox(
                16,
                certificateSection(),
                metadataSection(),
                entriesSection(),
                actionsSection());
        formColumn.setPadding(new Insets(0, 12, 0, 0));

        ScrollPane formScrollPane = new ScrollPane(formColumn);
        formScrollPane.setFitToWidth(true);
        formScrollPane.setPrefViewportWidth(620);

        TabPane previewTabs = previewTabs();
        HBox.setHgrow(previewTabs, Priority.ALWAYS);

        HBox content = new HBox(16, formScrollPane, previewTabs);
        HBox.setHgrow(formScrollPane, Priority.SOMETIMES);
        HBox.setHgrow(previewTabs, Priority.ALWAYS);
        root.setCenter(content);
    }

    private VBox certificateSection() {
        HBox actions = new HBox(8, inspectCertificateButton, loadCertificateFileButton);
        VBox details = metadataRows(
                metadataRow("Status", certificateStatusLabel),
                metadataRow("Fingerprint", certificateFingerprintLabel),
                metadataRow("Subject", certificateSubjectLabel),
                metadataRow("Issuer", certificateIssuerLabel));

        return section(
                "Certificate",
                new Label("Certificates are public cluster material. Secret entry values are not."),
                certificateTextArea,
                actions,
                details);
    }

    private VBox metadataSection() {
        VBox rows = new VBox(
                10,
                fieldRow("Secret Name", secretNameField),
                fieldRow("Namespace", namespaceField),
                fieldRow("Secret Type", secretTypeField),
                fieldRow("Sealing Scope", scopeComboBox),
                metadataRow("Scope Notes", scopeDescriptionLabel),
                metadataRow("kubeseal", kubesealStatusLabel));
        return section("Secret Metadata", rows);
    }

    private VBox entriesSection() {
        return section(
                "Secret Entries",
                new Label("Use explicit copy/export actions for plaintext. Duplicate keys are rejected."),
                entryRowsContainer,
                addEntryButton);
    }

    private VBox actionsSection() {
        HBox rowOne = new HBox(8, generateButton, validateButton, resetButton);
        HBox rowTwo = new HBox(8, copySecretButton, copySealedButton, exportSecretButton, exportSealedButton);
        return section("Actions", rowOne, rowTwo);
    }

    private TabPane previewTabs() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(secretTab());
        tabPane.getTabs().add(sealedSecretTab());
        tabPane.getTabs().add(diagnosticsTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    private Tab secretTab() {
        VBox content = new VBox(8, new Label("Plain Kubernetes Secret YAML"), secretYamlArea);
        VBox.setVgrow(secretYamlArea, Priority.ALWAYS);
        return new Tab("Secret YAML", content);
    }

    private Tab sealedSecretTab() {
        VBox content = new VBox(8, new Label("Generated SealedSecret YAML"), sealedSecretYamlArea);
        VBox.setVgrow(sealedSecretYamlArea, Priority.ALWAYS);
        return new Tab("SealedSecret YAML", content);
    }

    private Tab diagnosticsTab() {
        TitledPane technicalPane = new TitledPane("Technical Details", technicalDetailsArea);
        technicalPane.setExpanded(false);
        VBox content = new VBox(12, new Label("Validation status"), validationStatusLabel, technicalPane);
        VBox.setVgrow(technicalPane, Priority.ALWAYS);
        return new Tab("Diagnostics", content);
    }

    private VBox section(String title, javafx.scene.Node... children) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: #f7f7f8; -fx-background-radius: 12; -fx-border-color: #d8d8dd; -fx-border-radius: 12;");
        box.getChildren().add(titleLabel);
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        box.getChildren().addAll(children);
        return box;
    }

    private HBox fieldRow(String labelText, javafx.scene.Node input) {
        Label label = new Label(labelText);
        label.setMinWidth(120);
        HBox row = new HBox(12, label, input);
        HBox.setHgrow(input, Priority.ALWAYS);
        if (input instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return row;
    }

    private HBox metadataRow(String labelText, javafx.scene.Node value) {
        Label label = new Label(labelText + ":");
        label.setMinWidth(120);
        if (value instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        HBox row = new HBox(12, label, value);
        HBox.setHgrow(value, Priority.ALWAYS);
        return row;
    }

    private VBox metadataRows(javafx.scene.Node... rows) {
        VBox box = new VBox(8, rows);
        return box;
    }

    private TextArea readOnlyArea() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        return textArea;
    }
}

