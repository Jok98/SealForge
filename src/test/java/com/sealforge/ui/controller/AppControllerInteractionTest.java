package com.sealforge.ui.controller;

import com.sealforge.app.AppContext;
import com.sealforge.application.service.ApplicationSettingsStore;
import com.sealforge.application.service.KubesealGateway;
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
import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.KubesealRuntimeStatus;
import com.sealforge.domain.model.ValidationResult;
import com.sealforge.domain.validation.SecretDraftValidator;
import com.sealforge.infrastructure.certificate.PemCertificateParser;
import com.sealforge.infrastructure.yaml.JacksonSecretYamlSerializer;
import com.sealforge.testsupport.JavaFxTestStderrFilter;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class AppControllerInteractionTest {

    static {
        JavaFxTestStderrFilter.install();
    }

    private static final String VALID_CERTIFICATE_PEM = loadResource("/test-certificate.pem");

    @Start
    private void start(Stage stage) {
        RuntimeSettings runtimeSettings = new RuntimeSettings(new ApplicationSettings("/usr/bin/kubeseal", "Opaque"));
        InMemoryApplicationSettingsStore settingsStore = new InMemoryApplicationSettingsStore(runtimeSettings.snapshot());
        KubesealGateway kubesealGateway = new FakeKubesealGateway(runtimeSettings);

        AppContext appContext = new AppContext(
                new AppConfig("SealForge"),
                runtimeSettings,
                kubesealGateway,
                new LoadCertificateUseCase(new PemCertificateParser()),
                new CreateSecretDraftUseCase(),
                new GenerateYamlUseCase(new SecretDraftValidator(), new JacksonSecretYamlSerializer()),
                new SealSecretUseCase(kubesealGateway),
                new ValidateSealedSecretUseCase(kubesealGateway),
                new ExportYamlUseCase((targetPath, content) -> { }),
                new CopyYamlToClipboardUseCase(content -> { }),
                new ResetDraftUseCase(),
                new SaveSettingsUseCase(runtimeSettings, settingsStore));

        AppController controller = new AppController(appContext);
        stage.setScene(new Scene(controller.createView(), 1280, 860));
        stage.show();
    }

    @Test
    void shortcutNavigatesToSettings(FxRobot robot) {
        assertThat(robot.lookup("#screen-title").queryAs(Label.class).getText()).isEqualTo("Home");

        robot.press(KeyCode.CONTROL, KeyCode.DIGIT4).release(KeyCode.DIGIT4, KeyCode.CONTROL);

        assertThat(robot.lookup("#screen-title").queryAs(Label.class).getText()).isEqualTo("Settings");
    }

    @Test
    void invalidCertificateShowsInlineEditorError(FxRobot robot) throws Exception {
        robot.clickOn("#nav-secret-editor");
        robot.clickOn("#certificate-text-area").write("not-a-pem");
        robot.press(KeyCode.CONTROL, KeyCode.ENTER).release(KeyCode.ENTER, KeyCode.CONTROL);

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> robot.lookup("#certificate-error-label").queryAs(Label.class).getText().contains("parsed"));

        assertThat(robot.lookup("#screen-title").queryAs(Label.class).getText()).isEqualTo("Secret Editor");
        assertThat(robot.lookup("#certificate-error-label").queryAs(Label.class).getText())
                .containsIgnoringCase("certificate")
                .containsIgnoringCase("parsed");
    }

    @Test
    void savingSettingsUpdatesHomeSummary(FxRobot robot) {
        robot.clickOn("#nav-settings");
        robot.clickOn("#default-secret-type-field");
        robot.press(KeyCode.CONTROL, KeyCode.A).release(KeyCode.A, KeyCode.CONTROL);
        robot.write("kubernetes.io/dockerconfigjson");
        robot.clickOn("#save-settings-button");
        robot.clickOn("#nav-home");

        assertThat(robot.lookup("#home-default-secret-type-label").queryAs(Label.class).getText())
                .isEqualTo("kubernetes.io/dockerconfigjson");
    }

    @Test
    void generateShortcutOpensPreviewWithGeneratedYaml(FxRobot robot) throws Exception {
        robot.clickOn("#nav-secret-editor");
        replaceText(robot, "#certificate-text-area", VALID_CERTIFICATE_PEM);
        replaceText(robot, "#secret-name-field", "demo-secret");
        replaceText(robot, "#namespace-field", "team-a");
        replaceText(robot, "#secret-entry-key-field", "token");
        replaceText(robot, "#secret-entry-value-field-masked", "super-secret");

        robot.press(KeyCode.CONTROL, KeyCode.ENTER).release(KeyCode.ENTER, KeyCode.CONTROL);

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> "Preview".equals(robot.lookup("#screen-title").queryAs(Label.class).getText()));

        assertThat(robot.lookup("#screen-title").queryAs(Label.class).getText()).isEqualTo("Preview");
        assertThat(robot.lookup("#sealed-secret-yaml-area").queryAs(TextArea.class).getText())
                .contains("SealedSecret")
                .contains("encryptedData");
    }

    private static String loadResource(String resourcePath) {
        try (InputStream inputStream = AppControllerInteractionTest.class.getResourceAsStream(resourcePath)) {
            return new String(Objects.requireNonNull(inputStream, "Missing resource " + resourcePath).readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load test resource " + resourcePath, exception);
        }
    }

    private static void replaceText(FxRobot robot, String query, String text) {
        robot.interact(() -> robot.lookup(query).queryAs(TextInputControl.class).setText(text));
    }

    private static final class InMemoryApplicationSettingsStore implements ApplicationSettingsStore {

        private ApplicationSettings applicationSettings;

        private InMemoryApplicationSettingsStore(ApplicationSettings applicationSettings) {
            this.applicationSettings = applicationSettings;
        }

        @Override
        public ApplicationSettings load() {
            return applicationSettings;
        }

        @Override
        public void save(ApplicationSettings applicationSettings) {
            this.applicationSettings = applicationSettings.normalized();
        }
    }

    private static final class FakeKubesealGateway implements KubesealGateway {

        private final RuntimeSettings runtimeSettings;

        private FakeKubesealGateway(RuntimeSettings runtimeSettings) {
            this.runtimeSettings = runtimeSettings;
        }

        @Override
        public String seal(String plainSecretYaml, CertificateReference certificateReference, SealingScope scope) {
            return """
                    apiVersion: bitnami.com/v1alpha1
                    kind: SealedSecret
                    metadata:
                      name: preview
                    spec:
                      encryptedData:
                        token: sealed
                    """.strip();
        }

        @Override
        public ValidationResult validate(String sealedSecretYaml) {
            return new ValidationResult(true, "SealedSecret validation succeeded.", "validated");
        }

        @Override
        public KubesealRuntimeStatus inspectStatus() {
            return new KubesealRuntimeStatus(
                    true,
                    runtimeSettings.kubesealExecutable(),
                    "kubeseal v0.fake",
                    true,
                    "kubeseal v0.fake ready at " + runtimeSettings.kubesealExecutable(),
                    "kubeseal v0.fake");
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public Path executablePath() {
            return runtimeSettings.kubesealExecutable();
        }
    }
}
