package com.sealforge.infrastructure.kubeseal;

import com.sealforge.application.service.KubesealGateway;
import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.exception.KubesealExecutionException;
import com.sealforge.domain.exception.KubesealUnavailableException;
import com.sealforge.domain.exception.ValidationException;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.KubesealRuntimeStatus;
import com.sealforge.domain.model.ValidationResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class KubesealProcessAdapter implements KubesealGateway {

    private static final Duration DEFAULT_STATUS_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration DEFAULT_SEAL_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration DEFAULT_VALIDATE_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration DEFAULT_PROCESS_DESTROY_TIMEOUT = Duration.ofMillis(250);

    private final Supplier<Path> executablePathSupplier;
    private final KubesealCommandFactory commandFactory;
    private final Duration statusTimeout;
    private final Duration sealTimeout;
    private final Duration validateTimeout;
    private final Duration processDestroyTimeout;

    public KubesealProcessAdapter(Supplier<Path> executablePathSupplier, KubesealCommandFactory commandFactory) {
        this(
                executablePathSupplier,
                commandFactory,
                DEFAULT_STATUS_TIMEOUT,
                DEFAULT_SEAL_TIMEOUT,
                DEFAULT_VALIDATE_TIMEOUT,
                DEFAULT_PROCESS_DESTROY_TIMEOUT);
    }

    public KubesealProcessAdapter(
            Supplier<Path> executablePathSupplier,
            KubesealCommandFactory commandFactory,
            Duration statusTimeout,
            Duration sealTimeout,
            Duration validateTimeout,
            Duration processDestroyTimeout) {
        this.executablePathSupplier = executablePathSupplier;
        this.commandFactory = commandFactory;
        this.statusTimeout = statusTimeout;
        this.sealTimeout = sealTimeout;
        this.validateTimeout = validateTimeout;
        this.processDestroyTimeout = processDestroyTimeout;
    }

    @Override
    public String seal(String plainSecretYaml, CertificateReference certificateReference, SealingScope scope) {
        if (plainSecretYaml == null || plainSecretYaml.isBlank()) {
            throw new ValidationException("Plain Secret YAML must be generated before sealing.");
        }
        if (certificateReference == null) {
            throw new ValidationException("A public certificate must be loaded before sealing.");
        }

        Path tempCertificate = null;
        try {
            tempCertificate = writeTemporaryCertificate(certificateReference.pemContent());
            ProcessResult processResult = execute(
                    commandFactory.buildSealCommand(executablePath(), tempCertificate, scope),
                    plainSecretYaml,
                    sealTimeout);
            if (processResult.exitCode() != 0) {
                throw new KubesealExecutionException(
                        toUserFacingMessage(processResult, "sealing"),
                        technicalDetails(processResult));
            }
            return processResult.standardOutput().strip();
        } catch (IOException exception) {
            throw unavailable(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KubesealExecutionException(
                    "kubeseal was interrupted before it finished sealing.",
                    exception.getMessage(),
                    exception);
        } finally {
            deleteIfExists(tempCertificate);
        }
    }

    @Override
    public ValidationResult validate(String sealedSecretYaml) {
        try {
            ProcessResult processResult = execute(
                    commandFactory.buildValidateCommand(executablePath()),
                    sealedSecretYaml,
                    validateTimeout);

            if (processResult.exitCode() == 0) {
                return new ValidationResult(true, "SealedSecret validation succeeded.", processResult.standardOutput().strip());
            }

            String normalizedOutput = normalizeOutput(processResult);
            if (isUnsupportedValidate(normalizedOutput)) {
                return new ValidationResult(
                        false,
                        "This kubeseal executable does not support --validate. Upgrade kubeseal to use local validation.",
                        technicalDetails(processResult));
            }
            return new ValidationResult(
                    false,
                    toUserFacingMessage(processResult, "validation"),
                    technicalDetails(processResult));
        } catch (IOException exception) {
            return new ValidationResult(
                    false,
                    unavailableMessage(),
                    exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KubesealExecutionException(
                    "kubeseal validation was interrupted before it finished.",
                    exception.getMessage(),
                    exception);
        }
    }

    @Override
    public KubesealRuntimeStatus inspectStatus() {
        Path executable = executablePath();
        try {
            ProcessResult versionResult = execute(commandFactory.buildVersionCommand(executable), "", statusTimeout);
            if (versionResult.exitCode() != 0) {
                return unavailableStatus(executable, versionResult, "version check");
            }

            String version = parseVersion(versionResult);
            ProcessResult helpResult = execute(commandFactory.buildHelpCommand(executable), "", statusTimeout);
            boolean validationSupported = helpResult.exitCode() == 0
                    && normalizeOutput(helpResult).contains("--validate");

            String message = validationSupported
                    ? "kubeseal " + version + " ready at " + executable
                    : "kubeseal " + version + " ready at " + executable + ". This build does not advertise --validate.";
            String technicalDetails = validationSupported
                    ? technicalDetails(versionResult)
                    : joinNonBlank(technicalDetails(versionResult), technicalDetails(helpResult));

            return new KubesealRuntimeStatus(
                    true,
                    executable,
                    version,
                    validationSupported,
                    message,
                    technicalDetails);
        } catch (IOException exception) {
            return unavailableStatus(executable, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new KubesealRuntimeStatus(
                    false,
                    executable,
                    "",
                    false,
                    "kubeseal availability check was interrupted.",
                    exception.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return inspectStatus().available();
    }

    @Override
    public Path executablePath() {
        return executablePathSupplier.get();
    }

    private ProcessResult execute(List<String> command, String standardInput, Duration timeout) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        try (ExecutorService executorService = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> stdoutCapture = executorService.submit(() -> readFully(process.getInputStream()));
            Future<String> stderrCapture = executorService.submit(() -> readFully(process.getErrorStream()));

            try (OutputStream outputStream = process.getOutputStream()) {
                outputStream.write(standardInput.getBytes(StandardCharsets.UTF_8));
            }

            boolean finished;
            try {
                finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                destroyProcess(process);
                throw exception;
            }

            if (!finished) {
                destroyProcess(process);
                throw timeout(command, timeout, stdoutCapture, stderrCapture);
            }

            int exitCode = process.exitValue();
            return new ProcessResult(
                    exitCode,
                    awaitOutput(stdoutCapture),
                    awaitOutput(stderrCapture),
                    command);
        }
    }

    private Path writeTemporaryCertificate(String pemContent) throws IOException {
        Path tempFile = Files.createTempFile("sealforge-cert-", ".pem");
        try {
            Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
            // Windows and other non-POSIX file systems do not expose these permissions.
        }
        Files.writeString(tempFile, pemContent, StandardCharsets.UTF_8);
        return tempFile;
    }

    private void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup for public certificate material.
        }
    }

    private KubesealUnavailableException unavailable(IOException exception) {
        return new KubesealUnavailableException(
                unavailableMessage(),
                exception.getMessage(),
                exception);
    }

    private String unavailableMessage() {
        return "kubeseal is not available. Install it or configure the executable path in Settings.";
    }

    private KubesealRuntimeStatus unavailableStatus(Path executable, IOException exception) {
        return new KubesealRuntimeStatus(
                false,
                executable,
                "",
                false,
                "kubeseal could not be executed from " + executable + ". Configure it in Settings.",
                exception.getMessage());
    }

    private KubesealRuntimeStatus unavailableStatus(Path executable, ProcessResult processResult, String operation) {
        return new KubesealRuntimeStatus(
                false,
                executable,
                "",
                false,
                "kubeseal failed its " + operation + ". Confirm the configured executable is a working kubeseal binary.",
                technicalDetails(processResult));
    }

    private String toUserFacingMessage(ProcessResult processResult, String operation) {
        String normalized = normalizeOutput(processResult);
        if (normalized.isBlank()) {
            return "kubeseal failed during " + operation + ".";
        }
        if (isUnsupportedValidate(normalized)) {
            return "This kubeseal executable does not support --validate. Upgrade kubeseal to use local validation.";
        }
        if (normalized.contains("unable to decrypt sealed secret")) {
            return "kubeseal reported that the SealedSecret could not be decrypted for validation.";
        }
        if (requiresClusterConnectivity(normalized)) {
            return "kubeseal validation requires Kubernetes API access and a working kubeconfig for the target cluster.";
        }
        if (normalized.startsWith("error:")) {
            return normalized.substring("error:".length()).trim();
        }
        return normalized.lines().findFirst().orElse("kubeseal failed during " + operation + ".");
    }

    private String normalizeOutput(ProcessResult processResult) {
        return joinNonBlank(processResult.standardError(), processResult.standardOutput()).strip();
    }

    private String technicalDetails(ProcessResult processResult) {
        return "Command: " + String.join(" ", processResult.command()) + System.lineSeparator()
                + "Exit code: " + processResult.exitCode() + System.lineSeparator()
                + joinNonBlank(
                "stderr:" + System.lineSeparator() + processResult.standardError().strip(),
                "stdout:" + System.lineSeparator() + processResult.standardOutput().strip()).strip();
    }

    private String joinNonBlank(String first, String second) {
        String normalizedFirst = first == null ? "" : first.strip();
        String normalizedSecond = second == null ? "" : second.strip();
        if (normalizedFirst.isBlank()) {
            return normalizedSecond;
        }
        if (normalizedSecond.isBlank()) {
            return normalizedFirst;
        }
        return normalizedFirst + System.lineSeparator() + System.lineSeparator() + normalizedSecond;
    }

    private String parseVersion(ProcessResult processResult) {
        return normalizeOutput(processResult).lines()
                .map(String::strip)
                .filter(line -> !line.isBlank())
                .findFirst()
                .orElse("(version unavailable)");
    }

    private boolean isUnsupportedValidate(String normalizedOutput) {
        return normalizedOutput.contains("unknown flag: --validate")
                || normalizedOutput.contains("unknown long flag '--validate'")
                || normalizedOutput.contains("flag provided but not defined: -validate");
    }

    private boolean requiresClusterConnectivity(String normalizedOutput) {
        return normalizedOutput.contains("no configuration has been provided")
                || normalizedOutput.contains("connection refused")
                || normalizedOutput.contains("context deadline exceeded")
                || normalizedOutput.contains("no such host")
                || normalizedOutput.contains("couldn't get current server API group list")
                || normalizedOutput.contains("unable to connect to the server");
    }

    private KubesealExecutionException timeout(
            List<String> command,
            Duration timeout,
            Future<String> stdoutCapture,
            Future<String> stderrCapture) {
        String stdout = capturePartialOutput(stdoutCapture);
        String stderr = capturePartialOutput(stderrCapture);
        String userMessage = "kubeseal timed out after " + timeout.toSeconds() + " seconds. Check the executable, kubeconfig, or cluster connectivity.";
        String technicalDetails = "Command: " + String.join(" ", command) + System.lineSeparator()
                + "Timeout: " + timeout + System.lineSeparator()
                + joinNonBlank(
                "stderr:" + System.lineSeparator() + stderr.strip(),
                "stdout:" + System.lineSeparator() + stdout.strip()).strip();
        return new KubesealExecutionException(userMessage, technicalDetails);
    }

    private void destroyProcess(Process process) {
        process.destroy();
        try {
            if (!process.waitFor(processDestroyTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                process.waitFor(processDestroyTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException exception) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }

    private String awaitOutput(Future<String> outputFuture) throws IOException, InterruptedException {
        try {
            return outputFuture.get();
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("Failed to capture kubeseal output.", cause);
        }
    }

    private String capturePartialOutput(Future<String> outputFuture) {
        try {
            return outputFuture.get(processDestroyTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            return "";
        }
    }

    private String readFully(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private record ProcessResult(int exitCode, String standardOutput, String standardError, List<String> command) {
    }
}
