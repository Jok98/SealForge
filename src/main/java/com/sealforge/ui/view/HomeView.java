package com.sealforge.ui.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class HomeView {

    private final VBox root = new VBox(18);
    private final Button startAuthoringButton = new Button("New Secret");
    private final Button openPreviewButton = new Button("Open Preview");
    private final Button openSettingsButton = new Button("Settings");
    private final Label kubesealStatusLabel = new Label("-");
    private final Label certificateStatusLabel = new Label("-");
    private final Label previewStatusLabel = new Label("-");
    private final Label defaultSecretTypeLabel = new Label("-");

    public HomeView() {
        buildLayout();
    }

    public Parent root() {
        return root;
    }

    public Button startAuthoringButton() {
        return startAuthoringButton;
    }

    public Button openPreviewButton() {
        return openPreviewButton;
    }

    public Button openSettingsButton() {
        return openSettingsButton;
    }

    public void setKubesealStatus(boolean available, String executablePath) {
        kubesealStatusLabel.setText(available
                ? "kubeseal ready at " + executablePath
                : "kubeseal not detected. Configure it in Settings before sealing.");
    }

    public void setCertificateStatus(String summary) {
        certificateStatusLabel.setText(summary);
    }

    public void setPreviewAvailable(boolean available) {
        openPreviewButton.setDisable(!available);
        previewStatusLabel.setText(available
                ? "Generated YAML is available for review and export."
                : "No generated YAML yet. Use Secret Editor to generate a preview.");
    }

    public void setDefaultSecretType(String defaultSecretType) {
        defaultSecretTypeLabel.setText(defaultSecretType);
    }

    private void buildLayout() {
        root.setPadding(new Insets(6, 0, 0, 0));

        Label introTitleLabel = new Label("Start a local Sealed Secrets authoring session.");
        introTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label introBodyLabel = new Label(
                "SealForge helps you load a public certificate, draft a Kubernetes Secret, run kubeseal locally, and export YAML deliberately.");
        introBodyLabel.setWrapText(true);

        HBox actions = new HBox(10, startAuthoringButton, openPreviewButton, openSettingsButton);
        startAuthoringButton.setPrefWidth(150);
        openPreviewButton.setPrefWidth(150);
        openSettingsButton.setPrefWidth(150);

        VBox statusCard = card(
                "Current workspace",
                metadataRow("kubeseal", kubesealStatusLabel),
                metadataRow("Certificate", certificateStatusLabel),
                metadataRow("Preview", previewStatusLabel),
                metadataRow("Default Secret Type", defaultSecretTypeLabel));

        VBox guidanceCard = card(
                "Security reminders",
                wrappedLabel("Certificates are public cluster material. Secret entry values are sensitive."),
                wrappedLabel("Plain Secret YAML is only copied or exported through explicit user actions."),
                wrappedLabel("No secret drafts are persisted automatically."));

        root.getChildren().addAll(introTitleLabel, introBodyLabel, actions, statusCard, guidanceCard);
        VBox.setVgrow(statusCard, Priority.NEVER);
        VBox.setVgrow(guidanceCard, Priority.ALWAYS);
    }

    private VBox card(String title, javafx.scene.Node... children) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox container = new VBox(10, titleLabel);
        container.getChildren().addAll(children);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 16px; -fx-border-color: #d7e2f0; -fx-border-radius: 16px;");
        return container;
    }

    private HBox metadataRow(String label, Label valueLabel) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(140);
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
