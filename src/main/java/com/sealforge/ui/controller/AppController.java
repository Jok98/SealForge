package com.sealforge.ui.controller;

import com.sealforge.app.AppContext;
import com.sealforge.application.dto.CertificateLoadRequest;
import com.sealforge.application.dto.SecretDraftInput;
import com.sealforge.application.dto.SecretEntryInput;
import com.sealforge.config.ApplicationSettings;
import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.exception.SealForgeException;
import com.sealforge.domain.exception.UserInputException;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.GeneratedYaml;
import com.sealforge.domain.model.KubesealRuntimeStatus;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.ValidationResult;
import com.sealforge.ui.component.SecretEntryRowView;
import com.sealforge.ui.model.AppScreen;
import com.sealforge.ui.model.SecretDraftFormModel;
import com.sealforge.ui.model.SecretEntryRowModel;
import com.sealforge.ui.model.SettingsFormModel;
import com.sealforge.ui.view.AboutView;
import com.sealforge.ui.view.HomeView;
import com.sealforge.ui.view.PreviewView;
import com.sealforge.ui.view.SecretEditorView;
import com.sealforge.ui.view.SettingsView;
import com.sealforge.ui.view.ShellView;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class AppController {

    private final AppContext appContext;
    private final SecretDraftFormModel formModel = new SecretDraftFormModel();
    private final SettingsFormModel settingsFormModel = new SettingsFormModel();
    private final ShellView shellView;
    private final HomeView homeView = new HomeView();
    private final SecretEditorView secretEditorView = new SecretEditorView();
    private final PreviewView previewView = new PreviewView();
    private final SettingsView settingsView = new SettingsView();
    private final AboutView aboutView = new AboutView();
    private final List<SecretEntryRowView> entryRowViews = new ArrayList<>();

    private CertificateReference loadedCertificate;
    private GeneratedYaml generatedYaml;
    private Scene shortcutsScene;
    private Task<?> activeTask;
    private boolean shuttingDown;

    public AppController(AppContext appContext) {
        this.appContext = appContext;
        this.shellView = new ShellView(appContext.appConfig().applicationName());
    }

    public Parent createView() {
        bindDraftForm();
        bindSettingsForm();
        registerNavigation();
        registerHomeActions();
        registerEditorActions();
        registerPreviewActions();
        registerSettingsActions();
        loadSettingsIntoForm();
        applyResetState();
        updateKubesealStatus();
        updateHomeSummary();
        updatePreviewState();
        registerKeyboardShortcuts();
        navigate(AppScreen.HOME);
        return shellView.root();
    }

    private void bindDraftForm() {
        secretEditorView.certificateTextArea().textProperty().bindBidirectional(formModel.certificatePemProperty());
        secretEditorView.secretNameField().textProperty().bindBidirectional(formModel.secretNameProperty());
        secretEditorView.namespaceField().textProperty().bindBidirectional(formModel.namespaceProperty());
        secretEditorView.secretTypeField().textProperty().bindBidirectional(formModel.secretTypeProperty());
        secretEditorView.scopeComboBox().valueProperty().bindBidirectional(formModel.scopeProperty());

        formModel.scopeProperty().addListener((observable, oldValue, newValue) -> secretEditorView.setScopeDescription(newValue));
        formModel.certificatePemProperty().addListener((observable, oldValue, newValue) -> {
            secretEditorView.clearCertificateError();
            if (loadedCertificate != null && !newValue.strip().equals(loadedCertificate.pemContent().strip())) {
                loadedCertificate = null;
                secretEditorView.clearCertificateDetails();
                updateHomeSummary();
            }
        });
        formModel.secretNameProperty().addListener((observable, oldValue, newValue) -> secretEditorView.clearSecretNameError());
        formModel.namespaceProperty().addListener((observable, oldValue, newValue) -> secretEditorView.clearNamespaceError());
        formModel.secretTypeProperty().addListener((observable, oldValue, newValue) -> secretEditorView.clearSecretTypeError());
    }

    private void bindSettingsForm() {
        settingsView.kubesealExecutableField().textProperty().bindBidirectional(settingsFormModel.kubesealExecutableProperty());
        settingsView.defaultSecretTypeField().textProperty().bindBidirectional(settingsFormModel.defaultSecretTypeProperty());
        settingsFormModel.kubesealExecutableProperty().addListener((observable, oldValue, newValue) -> settingsView.clearKubesealExecutableError());
        settingsFormModel.defaultSecretTypeProperty().addListener((observable, oldValue, newValue) -> settingsView.clearDefaultSecretTypeError());
    }

    private void registerNavigation() {
        for (AppScreen screen : AppScreen.values()) {
            shellView.navigationButton(screen).setOnAction(event -> navigate(screen));
        }
    }

    private void registerHomeActions() {
        homeView.startAuthoringButton().setOnAction(event -> navigate(AppScreen.SECRET_EDITOR));
        homeView.openPreviewButton().setOnAction(event -> navigate(AppScreen.PREVIEW));
        homeView.openSettingsButton().setOnAction(event -> navigate(AppScreen.SETTINGS));
    }

    private void registerEditorActions() {
        secretEditorView.inspectCertificateButton().setOnAction(event -> inspectCertificate());
        secretEditorView.loadCertificateFileButton().setOnAction(event -> loadCertificateFromFile());
        secretEditorView.addEntryButton().setOnAction(event -> addEntryRow(new SecretEntryRowModel()));
        secretEditorView.generateButton().setOnAction(event -> generateYaml());
        secretEditorView.cancelOperationButton().setOnAction(event -> cancelActiveOperation());
        secretEditorView.resetButton().setOnAction(event -> {
            if (confirmReset()) {
                applyResetState();
                shellView.setFooterMessage("Draft cleared. Secret values were removed from the active UI state.");
            }
        });
    }

    private void registerPreviewActions() {
        previewView.backToEditorButton().setOnAction(event -> navigate(AppScreen.SECRET_EDITOR));
        previewView.validateButton().setOnAction(event -> validateSealedSecret());
        previewView.cancelOperationButton().setOnAction(event -> cancelActiveOperation());
        previewView.copySecretButton().setOnAction(event -> copyPlainSecretYaml());
        previewView.copySealedButton().setOnAction(event -> copySealedSecretYaml());
        previewView.exportSecretButton().setOnAction(event -> exportPlainSecretYaml());
        previewView.exportSealedButton().setOnAction(event -> exportSealedSecretYaml());
    }

    private void registerSettingsActions() {
        settingsView.browseKubesealButton().setOnAction(event -> browseKubesealExecutable());
        settingsView.saveButton().setOnAction(event -> saveSettings());
        settingsView.restoreDefaultsButton().setOnAction(event -> restoreDefaultSettings());
    }

    private void navigate(AppScreen screen) {
        shellView.showScreen(screen, switch (screen) {
            case HOME -> homeView.root();
            case SECRET_EDITOR -> secretEditorView.root();
            case PREVIEW -> previewView.root();
            case SETTINGS -> settingsView.root();
            case ABOUT -> aboutView.root();
        });
    }

    private void loadSettingsIntoForm() {
        ApplicationSettings settings = appContext.runtimeSettings().snapshot();
        settingsFormModel.kubesealExecutableProperty().set(settings.kubesealExecutable());
        settingsFormModel.defaultSecretTypeProperty().set(settings.defaultSecretType());
        settingsView.setSaveStatus("Settings are non-sensitive and saved locally.");
    }

    private void inspectCertificate() {
        try {
            secretEditorView.clearInlineValidation();
            loadedCertificate = loadCertificate(
                    CertificateSourceType.PASTE,
                    "Pasted certificate",
                    formModel.certificatePemProperty().get());
            secretEditorView.setEditorStatus("Certificate inspected successfully. Continue filling the draft.");
            previewView.setTechnicalDetails("");
            updateHomeSummary();
            shellView.setFooterMessage("Public certificate loaded. Secret entry values remain local to this session.");
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void loadCertificateFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sealed Secrets Certificate");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PEM files", "*.pem", "*.crt", "*.cert"));
        Path selectedPath = null;
        if (window() != null) {
            var file = fileChooser.showOpenDialog(window());
            if (file != null) {
                selectedPath = file.toPath();
            }
        }
        if (selectedPath == null) {
            return;
        }

        try {
            secretEditorView.clearInlineValidation();
            String pemContent = Files.readString(selectedPath);
            formModel.certificatePemProperty().set(pemContent);
            loadedCertificate = loadCertificate(
                    CertificateSourceType.FILE,
                    selectedPath.toString(),
                    pemContent);
            secretEditorView.setEditorStatus("Certificate loaded from file.");
            previewView.setTechnicalDetails("");
            updateHomeSummary();
            shellView.setFooterMessage("Public certificate loaded from " + selectedPath);
        } catch (IOException exception) {
            handleFailure(exception);
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void generateYaml() {
        try {
            secretEditorView.clearInlineValidation();
            SecretDraftInput draftInput = snapshotDraftInput();
            String certificatePem = formModel.certificatePemProperty().get();
            CertificateReference currentCertificate = loadedCertificate;

            runBackgroundTask(
                    AppScreen.SECRET_EDITOR,
                    "Generating preview with kubeseal...",
                    () -> generatePreview(draftInput, certificatePem, currentCertificate),
                    result -> {
                        loadedCertificate = result.certificateReference();
                        secretEditorView.setCertificateDetails(result.certificateReference());
                        generatedYaml = result.generatedYaml();
                        previewView.setGeneratedYaml(generatedYaml);
                        previewView.setValidationResult(new ValidationResult(false, "Validation not run yet.", ""));
                        previewView.setTechnicalDetails("");
                        secretEditorView.setEditorStatus("Preview generated successfully. Review the YAML before exporting.");
                        shellView.setFooterMessage("Preview generated locally. Plain Secret YAML is only exposed on copy or export.");
                        updateHomeSummary();
                        updatePreviewState();
                        navigate(AppScreen.PREVIEW);
                    });
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void validateSealedSecret() {
        try {
            String sealedSecretYaml = previewView.sealedSecretYamlArea().getText();
            runBackgroundTask(
                    AppScreen.PREVIEW,
                    "Validating SealedSecret with kubeseal...",
                    () -> appContext.validateSealedSecretUseCase().execute(sealedSecretYaml),
                    validationResult -> {
                        previewView.setValidationResult(validationResult);
                        previewView.setPreviewStatus(validationResult.message());
                        shellView.setFooterMessage(validationResult.message());
                    });
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void browseKubesealExecutable() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select kubeseal Executable");
        if (window() == null) {
            return;
        }
        var file = fileChooser.showOpenDialog(window());
        if (file != null) {
            settingsFormModel.kubesealExecutableProperty().set(file.getAbsolutePath());
            settingsView.setSaveStatus("Path selected. Save settings to apply it.");
        }
    }

    private void saveSettings() {
        try {
            settingsView.clearInlineValidation();
            ApplicationSettings savedSettings = appContext.saveSettingsUseCase().execute(new ApplicationSettings(
                    settingsFormModel.kubesealExecutableProperty().get(),
                    settingsFormModel.defaultSecretTypeProperty().get()));
            settingsFormModel.kubesealExecutableProperty().set(savedSettings.kubesealExecutable());
            settingsFormModel.defaultSecretTypeProperty().set(savedSettings.defaultSecretType());
            settingsView.setSaveStatus("Settings saved. kubeseal changes apply immediately. The default Secret type applies on reset or new draft.");
            updateKubesealStatus();
            updateHomeSummary();
            shellView.setFooterMessage("Settings saved locally. Secret drafts remain non-persistent.");
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void restoreDefaultSettings() {
        ApplicationSettings defaults = ApplicationSettings.defaults();
        settingsFormModel.kubesealExecutableProperty().set(defaults.kubesealExecutable());
        settingsFormModel.defaultSecretTypeProperty().set(defaults.defaultSecretType());
        settingsView.setSaveStatus("Defaults loaded into the form. Save settings to apply them.");
    }

    private void copyYaml(String yaml, String successMessage) {
        try {
            appContext.copyYamlToClipboardUseCase().execute(yaml);
            shellView.setFooterMessage(successMessage);
            showInfo(successMessage);
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void copyPlainSecretYaml() {
        if (!hasPlaintextPreview()) {
            return;
        }
        if (!confirmPlaintextAction(
                "Copy plaintext Secret YAML",
                "This will place unsealed secret values on the system clipboard.")) {
            return;
        }
        copyYaml(
                generatedYaml.plainSecretYaml(),
                "Plain Secret YAML copied to the clipboard. Clear it when you no longer need plaintext.");
    }

    private void copySealedSecretYaml() {
        if (generatedYaml == null || generatedYaml.sealedSecretYaml().isBlank()) {
            return;
        }
        copyYaml(generatedYaml.sealedSecretYaml(), "SealedSecret YAML copied to the clipboard.");
    }

    private void exportPlainSecretYaml() {
        if (!hasPlaintextPreview()) {
            return;
        }
        if (!confirmPlaintextAction(
                "Export plaintext Secret YAML",
                "This will write unsealed secret values to a file on disk.")) {
            return;
        }
        exportYaml(generatedYaml.plainSecretYaml(), "secret.yaml");
    }

    private void exportSealedSecretYaml() {
        if (generatedYaml == null || generatedYaml.sealedSecretYaml().isBlank()) {
            return;
        }
        exportYaml(generatedYaml.sealedSecretYaml(), "sealed-secret.yaml");
    }

    private void exportYaml(String yaml, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export YAML");
        fileChooser.setInitialFileName(defaultFileName);
        var file = window() == null ? null : fileChooser.showSaveDialog(window());
        if (file == null) {
            return;
        }
        try {
            appContext.exportYamlUseCase().execute(yaml, file.toPath());
            shellView.setFooterMessage("YAML exported to " + file.getAbsolutePath());
            showInfo("YAML exported to " + file.getAbsolutePath());
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private SecretDraftInput snapshotDraftInput() {
        return new SecretDraftInput(
                formModel.secretNameProperty().get(),
                formModel.namespaceProperty().get(),
                formModel.secretTypeProperty().get(),
                formModel.scopeProperty().get(),
                formModel.entries().stream()
                        .map(model -> new SecretEntryInput(model.keyProperty().get(), model.valueProperty().get()))
                        .toList());
    }

    private GeneratePreviewResult generatePreview(
            SecretDraftInput draftInput,
            String certificatePem,
            CertificateReference currentCertificate) {
        CertificateReference certificateReference = currentCertificate;
        if (certificateReference == null || !certificatePem.strip().equals(certificateReference.pemContent().strip())) {
            certificateReference = parseCertificate(
                    CertificateSourceType.PASTE,
                    "Pasted certificate",
                    certificatePem);
        }

        SecretDraft draft = appContext.createSecretDraftUseCase().execute(draftInput);
        String plainSecretYaml = appContext.generateYamlUseCase().execute(draft);
        String sealedSecretYaml = appContext.sealSecretUseCase().execute(plainSecretYaml, draft, certificateReference);
        return new GeneratePreviewResult(certificateReference, new GeneratedYaml(plainSecretYaml, sealedSecretYaml));
    }

    private CertificateReference loadCertificate(
            CertificateSourceType sourceType,
            String sourceDescription,
            String pemContent) {
        CertificateReference certificateReference = parseCertificate(
                sourceType,
                sourceDescription,
                pemContent);
        secretEditorView.setCertificateDetails(certificateReference);
        return certificateReference;
    }

    private CertificateReference parseCertificate(
            CertificateSourceType sourceType,
            String sourceDescription,
            String pemContent) {
        return appContext.loadCertificateUseCase().execute(new CertificateLoadRequest(
                sourceType,
                sourceDescription,
                pemContent));
    }

    private void addEntryRow(SecretEntryRowModel rowModel) {
        formModel.entries().add(rowModel);
        SecretEntryRowView rowView = new SecretEntryRowView(rowModel, () -> removeEntryRow(rowModel));
        rowModel.keyProperty().addListener((observable, oldValue, newValue) -> secretEditorView.clearEntriesError());
        entryRowViews.add(rowView);
        secretEditorView.entryRowsContainer().getChildren().add(rowView);
    }

    private void removeEntryRow(SecretEntryRowModel rowModel) {
        int index = formModel.entries().indexOf(rowModel);
        if (index >= 0) {
            formModel.entries().remove(index);
            SecretEntryRowView rowView = entryRowViews.remove(index);
            rowView.clearSensitiveValue();
            secretEditorView.entryRowsContainer().getChildren().remove(rowView);
            secretEditorView.clearEntriesError();
        }
    }

    private void applyResetState() {
        SecretDraftInput resetState = appContext.resetDraftUseCase().execute(appContext.runtimeSettings().defaultSecretType());
        applyDraftState(resetState);
        updateHomeSummary();
        updatePreviewState();
        updateKubesealStatus();
    }

    public boolean confirmApplicationClose() {
        if (shuttingDown || (!hasSensitiveDraft() && activeTask == null)) {
            return true;
        }

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                closeConfirmationMessage(),
                ButtonType.OK,
                ButtonType.CANCEL);
        alert.setHeaderText("Close and clear the current session?");
        if (window() != null) {
            alert.initOwner(window());
        }
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public void prepareForShutdown() {
        if (shuttingDown) {
            return;
        }
        shuttingDown = true;
        cancelActiveOperation();
        clearSensitiveState();
    }

    private void updateKubesealStatus() {
        KubesealRuntimeStatus kubesealRuntimeStatus = appContext.kubesealGateway().inspectStatus();
        secretEditorView.setKubesealStatus(kubesealRuntimeStatus);
        settingsView.setKubesealStatus(kubesealRuntimeStatus);
        homeView.setKubesealStatus(kubesealRuntimeStatus);
    }

    private void updateHomeSummary() {
        homeView.setCertificateStatus(loadedCertificate == null
                ? "No public certificate loaded."
                : "Loaded " + loadedCertificate.sourceDescription() + " with fingerprint " + loadedCertificate.fingerprint());
        homeView.setDefaultSecretType(appContext.runtimeSettings().defaultSecretType());
        homeView.setPreviewAvailable(generatedYaml != null && !generatedYaml.sealedSecretYaml().isBlank());
    }

    private void updatePreviewState() {
        boolean previewAvailable = generatedYaml != null && !generatedYaml.sealedSecretYaml().isBlank();
        previewView.setActionsEnabled(previewAvailable);
    }

    private void registerKeyboardShortcuts() {
        shellView.root().sceneProperty().addListener((observable, oldScene, newScene) -> installKeyboardShortcuts(newScene));
        installKeyboardShortcuts(shellView.root().getScene());
    }

    private void installKeyboardShortcuts(Scene scene) {
        if (scene == null || scene == shortcutsScene) {
            return;
        }

        shortcutsScene = scene;
        scene.getAccelerators().put(shortcut(KeyCode.DIGIT1), () -> navigate(AppScreen.HOME));
        scene.getAccelerators().put(shortcut(KeyCode.DIGIT2), () -> navigate(AppScreen.SECRET_EDITOR));
        scene.getAccelerators().put(shortcut(KeyCode.DIGIT3), () -> navigate(AppScreen.PREVIEW));
        scene.getAccelerators().put(shortcut(KeyCode.DIGIT4), () -> navigate(AppScreen.SETTINGS));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> navigate(AppScreen.ABOUT));
        scene.getAccelerators().put(shortcut(KeyCode.ENTER), this::generateYaml);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), this::cancelActiveOperation);
        scene.getAccelerators().put(shortcutShift(KeyCode.R), () -> {
            if (confirmReset()) {
                applyResetState();
                shellView.setFooterMessage("Draft cleared. Secret values were removed from the active UI state.");
            }
        });
        scene.getAccelerators().put(shortcutShift(KeyCode.V), this::validateSealedSecret);
    }

    private KeyCombination shortcut(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCombination.SHORTCUT_DOWN);
    }

    private KeyCombination shortcutShift(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    }

    private boolean confirmReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Reset the form and clear all in-memory secret values?",
                ButtonType.OK,
                ButtonType.CANCEL);
        alert.setHeaderText("Clear current draft");
        if (window() != null) {
            alert.initOwner(window());
        }
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private boolean confirmPlaintextAction(String actionTitle, String actionRisk) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                actionRisk + " Continue only if you explicitly need plaintext outside SealForge.",
                ButtonType.OK,
                ButtonType.CANCEL);
        alert.setHeaderText(actionTitle);
        if (window() != null) {
            alert.initOwner(window());
        }
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(appContext.appConfig().applicationName());
        if (window() != null) {
            alert.initOwner(window());
        }
        alert.showAndWait();
    }

    private void handleFailure(Exception exception) {
        if (exception instanceof SealForgeException sealForgeException) {
            previewView.setTechnicalDetails(sealForgeException.technicalDetails());
            shellView.setFooterMessage(sealForgeException.userMessage());
            if (sealForgeException instanceof UserInputException) {
                showInlineUserInputError(sealForgeException.userMessage());
                return;
            }
            settingsView.setSaveError(sealForgeException.userMessage());
            previewView.setPreviewStatus(sealForgeException.userMessage());
            secretEditorView.setEditorError(sealForgeException.userMessage());
            showError(sealForgeException.userMessage());
            return;
        }

        String technicalDetails = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        previewView.setTechnicalDetails(technicalDetails);
        settingsView.setSaveError("The requested action could not be completed.");
        previewView.setPreviewStatus("The requested action could not be completed.");
        secretEditorView.setEditorError("The requested action could not be completed.");
        shellView.setFooterMessage("The requested action could not be completed.");
        showError("The requested action could not be completed.");
    }

    private <T> void runBackgroundTask(
            AppScreen ownerScreen,
            String busyMessage,
            Callable<T> action,
            Consumer<T> onSuccess) {
        if (activeTask != null) {
            return;
        }

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };

        activeTask = task;
        setBusyState(ownerScreen, true, busyMessage);
        shellView.setFooterMessage(busyMessage);

        task.setOnSucceeded(event -> {
            activeTask = null;
            if (shuttingDown) {
                return;
            }
            setBusyState(ownerScreen, false, "");
            onSuccess.accept(task.getValue());
        });
        task.setOnCancelled(event -> {
            activeTask = null;
            if (shuttingDown) {
                return;
            }
            setBusyState(ownerScreen, false, "");
            previewView.setPreviewStatus("Operation cancelled.");
            secretEditorView.setEditorStatus("Operation cancelled.");
            shellView.setFooterMessage("Operation cancelled.");
        });
        task.setOnFailed(event -> {
            activeTask = null;
            if (shuttingDown) {
                return;
            }
            setBusyState(ownerScreen, false, "");
            Throwable failure = task.getException();
            if (isCancellationFailure(task, failure)) {
                previewView.setPreviewStatus("Operation cancelled.");
                secretEditorView.setEditorStatus("Operation cancelled.");
                shellView.setFooterMessage("Operation cancelled.");
                return;
            }
            if (failure instanceof Exception exception) {
                handleFailure(exception);
                return;
            }
            handleFailure(new RuntimeException(failure));
        });

        Thread.ofVirtual().name("sealforge-background-task").start(task);
    }

    private boolean isCancellationFailure(Task<?> task, Throwable failure) {
        return task.isCancelled()
                || failure instanceof InterruptedException
                || (failure != null
                && failure.getMessage() != null
                && failure.getMessage().toLowerCase().contains("interrupted"));
    }

    private void cancelActiveOperation() {
        if (activeTask != null) {
            activeTask.cancel(true);
        }
    }

    private void setBusyState(AppScreen ownerScreen, boolean busy, String message) {
        shellView.setNavigationDisabled(busy);
        secretEditorView.setBusy(busy && ownerScreen == AppScreen.SECRET_EDITOR, message);
        previewView.setBusy(busy && ownerScreen == AppScreen.PREVIEW, message);
    }

    private void showInlineUserInputError(String message) {
        if (message.contains("certificate")) {
            navigate(AppScreen.SECRET_EDITOR);
            secretEditorView.showCertificateError(message);
            secretEditorView.setEditorError(message);
            return;
        }
        if (message.startsWith("Secret name")) {
            navigate(AppScreen.SECRET_EDITOR);
            secretEditorView.showSecretNameError(message);
            secretEditorView.setEditorError(message);
            return;
        }
        if (message.startsWith("Namespace")) {
            navigate(AppScreen.SECRET_EDITOR);
            secretEditorView.showNamespaceError(message);
            secretEditorView.setEditorError(message);
            return;
        }
        if (message.startsWith("Secret type")) {
            navigate(AppScreen.SECRET_EDITOR);
            secretEditorView.showSecretTypeError(message);
            secretEditorView.setEditorError(message);
            return;
        }
        if (message.contains("entry")) {
            navigate(AppScreen.SECRET_EDITOR);
            secretEditorView.showEntriesError(message);
            secretEditorView.setEditorError(message);
            return;
        }
        if (message.contains("Default secret type")) {
            navigate(AppScreen.SETTINGS);
            settingsView.showDefaultSecretTypeError(message);
            settingsView.setSaveError(message);
            return;
        }
        if (message.contains("SealedSecret")) {
            navigate(AppScreen.PREVIEW);
            previewView.setValidationResult(new ValidationResult(false, message, ""));
            previewView.setPreviewStatus(message);
            return;
        }

        secretEditorView.setEditorError(message);
        settingsView.setSaveError(message);
        previewView.setPreviewStatus(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Action Failed");
        if (window() != null) {
            alert.initOwner(window());
        }
        alert.showAndWait();
    }

    private Window window() {
        return shellView.root().getScene() == null ? null : shellView.root().getScene().getWindow();
    }

    private void applyDraftState(SecretDraftInput draftState) {
        formModel.certificatePemProperty().set("");
        formModel.secretNameProperty().set(draftState.name());
        formModel.namespaceProperty().set(draftState.namespace());
        formModel.secretTypeProperty().set(draftState.type());
        formModel.scopeProperty().set(draftState.scope());
        loadedCertificate = null;
        generatedYaml = null;

        secretEditorView.clearInlineValidation();
        secretEditorView.clearCertificateDetails();
        secretEditorView.setEditorStatus("Generate a preview to open the validation and export screen.");
        previewView.clearGeneratedYaml();
        previewView.setValidationResult(new ValidationResult(false, "Validation not run yet.", ""));
        previewView.setTechnicalDetails("");

        for (SecretEntryRowView rowView : entryRowViews) {
            rowView.clearSensitiveValue();
        }
        entryRowViews.clear();
        formModel.entries().clear();
        secretEditorView.entryRowsContainer().getChildren().clear();

        if (draftState.entries().isEmpty()) {
            addEntryRow(new SecretEntryRowModel());
            return;
        }

        for (SecretEntryInput entry : draftState.entries()) {
            SecretEntryRowModel rowModel = new SecretEntryRowModel();
            rowModel.keyProperty().set(entry.key());
            rowModel.valueProperty().set(entry.value());
            addEntryRow(rowModel);
        }
    }

    private void clearSensitiveState() {
        applyDraftState(appContext.resetDraftUseCase().execute(appContext.runtimeSettings().defaultSecretType()));
        secretEditorView.setEditorStatus("Session cleared for shutdown.");
        previewView.setPreviewStatus("Session cleared for shutdown.");
    }

    private boolean hasSensitiveDraft() {
        if (hasPlaintextPreview()) {
            return true;
        }
        return formModel.entries().stream()
                .map(model -> model.valueProperty().get())
                .anyMatch(value -> value != null && !value.isBlank());
    }

    private boolean hasPlaintextPreview() {
        return generatedYaml != null
                && generatedYaml.plainSecretYaml() != null
                && !generatedYaml.plainSecretYaml().isBlank();
    }

    private String closeConfirmationMessage() {
        if (activeTask != null && hasSensitiveDraft()) {
            return "Closing now will cancel the running kubeseal action and clear all in-memory plaintext values from this session.";
        }
        if (activeTask != null) {
            return "Closing now will cancel the running kubeseal action.";
        }
        return "Closing now will clear all in-memory plaintext values from this session.";
    }

    private record GeneratePreviewResult(CertificateReference certificateReference, GeneratedYaml generatedYaml) {
    }
}
