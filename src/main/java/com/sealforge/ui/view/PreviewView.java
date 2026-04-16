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

    private final VBox root = new VBox(14);
    private final Label previewStatusLabel = new Label("Generate a Secret draft from the editor to populate this screen.");
    private final Button backToEditorButton = new Button("Back To Editor");
    private final Button validateButton = new Button("Validate SealedSecret");
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
        previewStatusLabel.setText("Review the generated YAML carefully before copying or exporting plaintext.");
        setActionsEnabled(true);
    }

    public void clearGeneratedYaml() {
        secretYamlArea.clear();
        sealedSecretYamlArea.clear();
        previewStatusLabel.setText("Generate a Secret draft from the editor to populate this screen.");
        setActionsEnabled(false);
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
    }

    private void buildLayout() {
        root.setPadding(new Insets(6, 0, 0, 0));

        previewStatusLabel.setWrapText(true);
        validationStatusLabel.setWrapText(true);

        HBox actions = new HBox(
                8,
                backToEditorButton,
                validateButton,
                copySecretButton,
                copySealedButton,
                exportSecretButton,
                exportSealedButton);

        TabPane tabPane = new TabPane(
                tab("Secret YAML", secretYamlArea),
                tab("SealedSecret YAML", sealedSecretYamlArea),
                tab("Diagnostics", diagnosticsPane()));
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(previewStatusLabel, actions, tabPane);
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
}
