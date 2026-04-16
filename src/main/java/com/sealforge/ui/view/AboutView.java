package com.sealforge.ui.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public final class AboutView {

    private final ScrollPane root = new ScrollPane();

    public AboutView() {
        buildLayout();
    }

    public Parent root() {
        return root;
    }

    private void buildLayout() {
        VBox content = new VBox(
                16,
                section(
                        "What SealForge is",
                        wrappedLabel("A local-first JavaFX desktop application for authoring and validating Kubernetes Sealed Secrets with less kubeseal CLI friction.")),
                section(
                        "What SealForge is not",
                        wrappedLabel("It is not a secret manager, not a secure vault, and not a replacement for enterprise lifecycle tooling such as Vault, External Secrets Operator, or SOPS.")),
                section(
                        "Security posture",
                        wrappedLabel("Secret values are handled locally in memory for the active session."),
                        wrappedLabel("Secret drafts are not autosaved."),
                        wrappedLabel("Clipboard and file export actions require explicit user intent."),
                        wrappedLabel("Certificates are public material, but they are still tracked with fingerprint and source information.")),
                section(
                        "Release intent",
                        wrappedLabel("SealForge is being structured as a public, cross-platform open source desktop tool for teams using Bitnami Sealed Secrets in GitOps workflows.")));

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

    private Label wrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        return label;
    }
}
