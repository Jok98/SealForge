package com.sealforge.ui.view;

import com.sealforge.ui.model.AppScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public final class ShellView {

    private static final String ACTIVE_NAV_STYLE = "-fx-background-color: #1c4c91; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String INACTIVE_NAV_STYLE = "-fx-background-color: transparent; -fx-text-fill: #243447;";

    private final BorderPane root = new BorderPane();
    private final Label screenTitleLabel = new Label();
    private final Label screenSubtitleLabel = new Label();
    private final StackPane contentHost = new StackPane();
    private final Label footerLabel = new Label("Local-first authoring. Secret values are not persisted by default.");
    private final Map<AppScreen, Button> navigationButtons = new EnumMap<>(AppScreen.class);

    public ShellView(String applicationName) {
        buildLayout(applicationName);
    }

    public Parent root() {
        return root;
    }

    public Button navigationButton(AppScreen screen) {
        return navigationButtons.get(screen);
    }

    public void showScreen(AppScreen screen, Parent content) {
        screenTitleLabel.setText(screen.title());
        screenSubtitleLabel.setText(screen.subtitle());
        contentHost.getChildren().setAll(content);
        navigationButtons.forEach((candidate, button) -> button.setStyle(candidate == screen ? ACTIVE_NAV_STYLE : INACTIVE_NAV_STYLE));
    }

    public void setFooterMessage(String message) {
        footerLabel.setText(message == null || message.isBlank()
                ? "Local-first authoring. Secret values are not persisted by default."
                : message);
    }

    private void buildLayout(String applicationName) {
        root.setPadding(new Insets(18));

        VBox navigation = new VBox(10);
        navigation.setPadding(new Insets(16));
        navigation.setPrefWidth(240);
        navigation.setStyle("-fx-background-color: #edf3fb; -fx-background-radius: 16px;");

        Label appNameLabel = new Label(applicationName);
        appNameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label appTaglineLabel = new Label("Desktop-native Sealed Secrets authoring.");
        appTaglineLabel.setWrapText(true);
        appTaglineLabel.setStyle("-fx-text-fill: #4a5968;");

        Label navCaptionLabel = new Label("Workspace");
        navCaptionLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #5d7084;");

        navigation.getChildren().addAll(appNameLabel, appTaglineLabel, navCaptionLabel);
        for (AppScreen screen : AppScreen.values()) {
            Button button = new Button(screen.title());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setPrefHeight(40);
            button.setStyle(INACTIVE_NAV_STYLE);
            navigationButtons.put(screen, button);
            navigation.getChildren().add(button);
        }

        VBox navigationFooter = new VBox(8);
        navigationFooter.setPadding(new Insets(18, 0, 0, 0));
        Label localFirstLabel = new Label("Local-first");
        localFirstLabel.setStyle("-fx-font-weight: bold;");
        Label securityLabel = new Label("No backend server. No secret autosave.");
        securityLabel.setWrapText(true);
        securityLabel.setStyle("-fx-text-fill: #4a5968;");
        navigationFooter.getChildren().addAll(localFirstLabel, securityLabel);
        VBox.setVgrow(navigationFooter, Priority.ALWAYS);
        navigation.getChildren().add(navigationFooter);

        VBox screenHeader = new VBox(6, screenTitleLabel, screenSubtitleLabel);
        screenHeader.setPadding(new Insets(0, 0, 14, 0));
        screenTitleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        screenSubtitleLabel.setWrapText(true);
        screenSubtitleLabel.setStyle("-fx-text-fill: #556270;");

        BorderPane contentPane = new BorderPane();
        contentPane.setPadding(new Insets(6, 0, 0, 18));
        contentPane.setTop(screenHeader);
        contentPane.setCenter(contentHost);
        contentPane.setBottom(footerLabel);
        BorderPane.setMargin(footerLabel, new Insets(14, 0, 0, 0));
        footerLabel.setWrapText(true);
        footerLabel.setStyle("-fx-text-fill: #4a5968;");

        root.setLeft(navigation);
        root.setCenter(contentPane);
    }
}
