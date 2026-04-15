package com.sealforge.ui.controller;

import com.sealforge.app.AppContext;
import com.sealforge.application.dto.CertificateLoadRequest;
import com.sealforge.application.dto.SecretDraftInput;
import com.sealforge.application.dto.SecretEntryInput;
import com.sealforge.domain.enumtype.CertificateSourceType;
import com.sealforge.domain.exception.SealForgeException;
import com.sealforge.domain.exception.ValidationException;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.GeneratedYaml;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.ValidationResult;
import com.sealforge.ui.component.SecretEntryRowView;
import com.sealforge.ui.model.SecretDraftFormModel;
import com.sealforge.ui.model.SecretEntryRowModel;
import com.sealforge.ui.view.MainView;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MainController {

    private final AppContext appContext;
    private final SecretDraftFormModel formModel = new SecretDraftFormModel();
    private final MainView view = new MainView();
    private final List<SecretEntryRowView> entryRowViews = new ArrayList<>();

    private CertificateReference loadedCertificate;

    public MainController(AppContext appContext) {
        this.appContext = appContext;
    }

    public Parent createView() {
        bindForm();
        registerActions();
        applyResetState();
        view.setKubesealStatus(
                appContext.kubesealGateway().isAvailable(),
                appContext.kubesealGateway().executablePath().toString());
        return view.root();
    }

    private void bindForm() {
        view.certificateTextArea().textProperty().bindBidirectional(formModel.certificatePemProperty());
        view.secretNameField().textProperty().bindBidirectional(formModel.secretNameProperty());
        view.namespaceField().textProperty().bindBidirectional(formModel.namespaceProperty());
        view.secretTypeField().textProperty().bindBidirectional(formModel.secretTypeProperty());
        view.scopeComboBox().valueProperty().bindBidirectional(formModel.scopeProperty());
        formModel.scopeProperty().addListener((observable, oldValue, newValue) -> view.setScopeDescription(newValue));
        formModel.certificatePemProperty().addListener((observable, oldValue, newValue) -> {
            if (loadedCertificate != null && !newValue.strip().equals(loadedCertificate.pemContent().strip())) {
                loadedCertificate = null;
                view.clearCertificateDetails();
            }
        });
    }

    private void registerActions() {
        view.inspectCertificateButton().setOnAction(event -> inspectCertificate());
        view.loadCertificateFileButton().setOnAction(event -> loadCertificateFromFile());
        view.addEntryButton().setOnAction(event -> addEntryRow(new SecretEntryRowModel()));
        view.generateButton().setOnAction(event -> generateYaml());
        view.validateButton().setOnAction(event -> validateSealedSecret());
        view.copySecretButton().setOnAction(event -> copyYaml(view.secretYamlArea().getText(), "Plain Secret YAML copied to the clipboard."));
        view.copySealedButton().setOnAction(event -> copyYaml(view.sealedSecretYamlArea().getText(), "SealedSecret YAML copied to the clipboard."));
        view.exportSecretButton().setOnAction(event -> exportYaml(view.secretYamlArea().getText(), "secret.yaml"));
        view.exportSealedButton().setOnAction(event -> exportYaml(view.sealedSecretYamlArea().getText(), "sealed-secret.yaml"));
        view.resetButton().setOnAction(event -> {
            if (confirmReset()) {
                applyResetState();
            }
        });
    }

    private void inspectCertificate() {
        try {
            loadedCertificate = appContext.loadCertificateUseCase().execute(new CertificateLoadRequest(
                    CertificateSourceType.PASTE,
                    "Pasted certificate",
                    formModel.certificatePemProperty().get()));
            view.setCertificateDetails(loadedCertificate);
            view.setTechnicalDetails("");
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
            String pemContent = Files.readString(selectedPath);
            formModel.certificatePemProperty().set(pemContent);
            loadedCertificate = appContext.loadCertificateUseCase().execute(new CertificateLoadRequest(
                    CertificateSourceType.FILE,
                    selectedPath.toString(),
                    pemContent));
            view.setCertificateDetails(loadedCertificate);
            view.setTechnicalDetails("");
        } catch (IOException exception) {
            handleFailure(exception);
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void generateYaml() {
        try {
            ensureCertificateLoaded();
            SecretDraft draft = buildDraft();
            String plainSecretYaml = appContext.generateYamlUseCase().execute(draft);
            String sealedSecretYaml = appContext.sealSecretUseCase().execute(plainSecretYaml, draft, loadedCertificate);
            view.setGeneratedYaml(new GeneratedYaml(plainSecretYaml, sealedSecretYaml));
            view.setValidationResult(new ValidationResult(false, "Validation not run yet.", ""));
            view.setTechnicalDetails("");
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void validateSealedSecret() {
        try {
            ValidationResult validationResult = appContext.validateSealedSecretUseCase()
                    .execute(view.sealedSecretYamlArea().getText());
            view.setValidationResult(validationResult);
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private void copyYaml(String yaml, String successMessage) {
        try {
            appContext.copyYamlToClipboardUseCase().execute(yaml);
            showInfo(successMessage);
        } catch (Exception exception) {
            handleFailure(exception);
        }
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
            showInfo("YAML exported to " + file.getAbsolutePath());
        } catch (Exception exception) {
            handleFailure(exception);
        }
    }

    private SecretDraft buildDraft() {
        SecretDraftInput draftInput = new SecretDraftInput(
                formModel.secretNameProperty().get(),
                formModel.namespaceProperty().get(),
                formModel.secretTypeProperty().get(),
                formModel.scopeProperty().get(),
                formModel.entries().stream()
                        .map(model -> new SecretEntryInput(model.keyProperty().get(), model.valueProperty().get()))
                        .toList());
        return appContext.createSecretDraftUseCase().execute(draftInput);
    }

    private void ensureCertificateLoaded() {
        if (loadedCertificate == null) {
            inspectCertificate();
            if (loadedCertificate == null) {
                throw new ValidationException("A public certificate must be loaded before sealing.");
            }
        }
    }

    private void addEntryRow(SecretEntryRowModel rowModel) {
        formModel.entries().add(rowModel);
        SecretEntryRowView rowView = new SecretEntryRowView(rowModel, () -> removeEntryRow(rowModel));
        entryRowViews.add(rowView);
        view.entryRowsContainer().getChildren().add(rowView);
    }

    private void removeEntryRow(SecretEntryRowModel rowModel) {
        int index = formModel.entries().indexOf(rowModel);
        if (index >= 0) {
            formModel.entries().remove(index);
            SecretEntryRowView rowView = entryRowViews.remove(index);
            rowView.clearSensitiveValue();
            view.entryRowsContainer().getChildren().remove(rowView);
        }
    }

    private void applyResetState() {
        SecretDraftInput resetState = appContext.resetDraftUseCase().execute(appContext.appConfig().defaultSecretType());
        formModel.certificatePemProperty().set("");
        formModel.secretNameProperty().set(resetState.name());
        formModel.namespaceProperty().set(resetState.namespace());
        formModel.secretTypeProperty().set(resetState.type());
        formModel.scopeProperty().set(resetState.scope());
        loadedCertificate = null;
        view.clearCertificateDetails();
        view.clearGeneratedYaml();
        view.setValidationResult(new ValidationResult(false, "Validation not run yet.", ""));
        view.setTechnicalDetails("");

        for (SecretEntryRowView rowView : entryRowViews) {
            rowView.clearSensitiveValue();
        }
        entryRowViews.clear();
        formModel.entries().clear();
        view.entryRowsContainer().getChildren().clear();
        addEntryRow(new SecretEntryRowModel());
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("SealForge");
        if (window() != null) {
            alert.initOwner(window());
        }
        alert.showAndWait();
    }

    private void handleFailure(Exception exception) {
        if (exception instanceof SealForgeException sealForgeException) {
            view.setTechnicalDetails(sealForgeException.technicalDetails());
            showError(sealForgeException.userMessage());
            return;
        }
        String technicalDetails = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        view.setTechnicalDetails(technicalDetails);
        showError("The requested action could not be completed.");
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
        return view.root().getScene() == null ? null : view.root().getScene().getWindow();
    }
}
