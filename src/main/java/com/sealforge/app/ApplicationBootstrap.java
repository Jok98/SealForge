package com.sealforge.app;

import com.sealforge.application.service.ApplicationSettingsStore;
import com.sealforge.application.service.CertificateParser;
import com.sealforge.application.service.FileExportService;
import com.sealforge.application.service.KubesealGateway;
import com.sealforge.application.service.SecretYamlRenderer;
import com.sealforge.application.service.SystemClipboardService;
import com.sealforge.application.usecase.CopyYamlToClipboardUseCase;
import com.sealforge.application.usecase.CreateSecretDraftUseCase;
import com.sealforge.application.usecase.ExportYamlUseCase;
import com.sealforge.application.usecase.GenerateYamlUseCase;
import com.sealforge.application.usecase.LoadCertificateUseCase;
import com.sealforge.application.usecase.ResetDraftUseCase;
import com.sealforge.application.usecase.SaveSettingsUseCase;
import com.sealforge.application.usecase.SealSecretUseCase;
import com.sealforge.application.usecase.ValidateSealedSecretUseCase;
import com.sealforge.config.AppConfig;
import com.sealforge.config.ApplicationSettings;
import com.sealforge.config.RuntimeSettings;
import com.sealforge.domain.validation.SecretDraftValidator;
import com.sealforge.infrastructure.certificate.PemCertificateParser;
import com.sealforge.infrastructure.clipboard.JavaFxClipboardAdapter;
import com.sealforge.infrastructure.file.LocalFileExportAdapter;
import com.sealforge.infrastructure.kubeseal.KubesealCommandFactory;
import com.sealforge.infrastructure.kubeseal.KubesealProcessAdapter;
import com.sealforge.infrastructure.settings.PreferencesApplicationSettingsStore;
import com.sealforge.infrastructure.yaml.JacksonSecretYamlSerializer;

public final class ApplicationBootstrap {

    private ApplicationBootstrap() {
    }

    public static AppContext bootstrap() {
        AppConfig appConfig = AppConfig.load();
        ApplicationSettingsStore settingsStore = new PreferencesApplicationSettingsStore();
        ApplicationSettings loadedSettings = settingsStore.load();
        RuntimeSettings runtimeSettings = new RuntimeSettings(loadedSettings);

        SecretDraftValidator secretDraftValidator = new SecretDraftValidator();
        SecretYamlRenderer secretYamlRenderer = new JacksonSecretYamlSerializer();
        CertificateParser certificateParser = new PemCertificateParser();
        KubesealGateway kubesealGateway = new KubesealProcessAdapter(
                runtimeSettings::kubesealExecutable,
                new KubesealCommandFactory());
        FileExportService fileExportService = new LocalFileExportAdapter();
        SystemClipboardService clipboardService = new JavaFxClipboardAdapter();

        return new AppContext(
                appConfig,
                runtimeSettings,
                kubesealGateway,
                new LoadCertificateUseCase(certificateParser),
                new CreateSecretDraftUseCase(),
                new GenerateYamlUseCase(secretDraftValidator, secretYamlRenderer),
                new SealSecretUseCase(kubesealGateway),
                new ValidateSealedSecretUseCase(kubesealGateway),
                new ExportYamlUseCase(fileExportService),
                new CopyYamlToClipboardUseCase(clipboardService),
                new ResetDraftUseCase(),
                new SaveSettingsUseCase(runtimeSettings, settingsStore));
    }
}
