package com.sealforge.ui.view;

import com.sealforge.domain.model.GeneratedYaml;
import com.sealforge.domain.model.ValidationResult;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class PreviewView {

    private static final String WARNING_STYLE =
            "-fx-background-color: #fff7e6; -fx-background-radius: 10px; -fx-border-color: #f0c36d; "
                    + "-fx-border-radius: 10px; -fx-padding: 10px; -fx-text-fill: #8a4b08;";

    private final VBox root = new VBox(14);
    private final Label previewStatusLabel = new Label("Generate a Secret draft from the editor to populate this screen.");
    private final Label plaintextWarningLabel = new Label(
            "Plain Secret YAML contains unsealed values. Copy or export it only when you intentionally need plaintext outside SealForge.");
    private final Button backToEditorButton = new Button("Back To Editor");
    private final Button validateButton = new Button("Validate SealedSecret");
    private final Button cancelOperationButton = new Button("Cancel Running Action");
    private final Button copySecretButton = new Button("Copy Secret YAML");
    private final Button copySealedButton = new Button("Copy SealedSecret YAML");
    private final Button exportSecretButton = new Button("Export Secret YAML");
    private final Button exportSealedButton = new Button("Export SealedSecret YAML");
    private final TextArea secretYamlArea = readOnlyArea();
    private final TextArea sealedSecretYamlArea = readOnlyArea();
    private final Label validationStatusLabel = new Label("Validation not run yet.");
    private final TextArea technicalDetailsArea = readOnlyArea();

    public PreviewView() {
        buildLayout();
        setActionsEnabled(false);
    }

    public Parent root() {
        return root;
    }

    public Button backToEditorButton() {
        return backToEditorButton;
    }

    public Button validateButton() {
        return validateButton;
    }

    public Button cancelOperationButton() {
        return cancelOperationButton;
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

    public TextArea sealedSecretYamlArea() {
        return sealedSecretYamlArea;
    }

    public void setGeneratedYaml(GeneratedYaml generatedYaml) {
        secretYamlArea.setText(generatedYaml.plainSecretYaml());
        sealedSecretYamlArea.setText(generatedYaml.sealedSecretYaml());
        previewStatusLabel.setText("Review both YAML outputs carefully before sharing them.");
        setPlaintextWarningVisible(true);
        setActionsEnabled(true);
    }

    public void clearGeneratedYaml() {
        secretYamlArea.clear();
        sealedSecretYamlArea.clear();
        previewStatusLabel.setText("Generate a Secret draft from the editor to populate this screen.");
        setPlaintextWarningVisible(false);
        setActionsEnabled(false);
    }

    public void setPreviewStatus(String message) {
        previewStatusLabel.setText(message);
    }

    public void setValidationResult(ValidationResult validationResult) {
        validationStatusLabel.setText(validationResult.message());
        technicalDetailsArea.setText(validationResult.details() == null ? "" : validationResult.details());
    }

    public void setTechnicalDetails(String details) {
        technicalDetailsArea.setText(details == null ? "" : details);
    }

    public void setActionsEnabled(boolean enabled) {
        validateButton.setDisable(!enabled);
        copySecretButton.setDisable(!enabled);
        copySealedButton.setDisable(!enabled);
        exportSecretButton.setDisable(!enabled);
        exportSealedButton.setDisable(!enabled);
        cancelOperationButton.setDisable(true);
    }

    public void setBusy(boolean busy, String message) {
        backToEditorButton.setDisable(busy);
        validateButton.setDisable(busy);
        copySecretButton.setDisable(busy);
        copySealedButton.setDisable(busy);
        exportSecretButton.setDisable(busy);
        exportSealedButton.setDisable(busy);
        cancelOperationButton.setDisable(!busy);
        if (busy) {
            previewStatusLabel.setText(message);
        }
    }

    private void buildLayout() {
        root.setPadding(new Insets(6, 0, 0, 0));
        root.setId("preview-root");
        previewStatusLabel.setId("preview-status-label");
        plaintextWarningLabel.setId("plaintext-warning-label");
        backToEditorButton.setId("back-to-editor-button");
        validateButton.setId("validate-sealed-secret-button");
        cancelOperationButton.setId("cancel-preview-operation-button");
        copySecretButton.setId("copy-secret-yaml-button");
        copySealedButton.setId("copy-sealed-yaml-button");
        exportSecretButton.setId("export-secret-yaml-button");
        exportSealedButton.setId("export-sealed-yaml-button");
        secretYamlArea.setId("secret-yaml-area");
        sealedSecretYamlArea.setId("sealed-secret-yaml-area");
        validationStatusLabel.setId("validation-status-label");
        technicalDetailsArea.setId("technical-details-area");

        cancelOperationButton.setDisable(true);

        previewStatusLabel.setWrapText(true);
        plaintextWarningLabel.setWrapText(true);
        plaintextWarningLabel.setStyle(WARNING_STYLE);
        setPlaintextWarningVisible(false);
        validationStatusLabel.setWrapText(true);

        HBox actions = new HBox(
                8,
                backToEditorButton,
                validateButton,
                cancelOperationButton,
                copySecretButton,
                copySealedButton,
                exportSecretButton,
                exportSealedButton);

        TabPane tabPane = new TabPane(
                tab("Secret YAML", secretYamlArea),
                tab("SealedSecret YAML", sealedSecretYamlArea),
                tab("Diagnostics", diagnosticsPane()));
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(previewStatusLabel, plaintextWarningLabel, actions, tabPane);
    }

    private VBox diagnosticsPane() {
        VBox diagnostics = new VBox(10,
                new Label("Validation"),
                validationStatusLabel,
                new Label("Technical details"),
                technicalDetailsArea);
        diagnostics.setPadding(new Insets(14));
        return diagnostics;
    }

    private Tab tab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private TextArea readOnlyArea() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        return textArea;
    }

    private void setPlaintextWarningVisible(boolean visible) {
        plaintextWarningLabel.setVisible(visible);
        plaintextWarningLabel.setManaged(visible);
    }
}
