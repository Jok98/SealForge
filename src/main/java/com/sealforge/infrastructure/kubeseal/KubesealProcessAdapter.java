package com.sealforge.infrastructure.kubeseal;

import com.sealforge.application.service.KubesealGateway;
import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.exception.KubesealExecutionException;
import com.sealforge.domain.exception.KubesealUnavailableException;
import com.sealforge.domain.exception.ValidationException;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.ValidationResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class KubesealProcessAdapter implements KubesealGateway {

    private final Supplier<Path> executablePathSupplier;
    private final KubesealCommandFactory commandFactory;

    public KubesealProcessAdapter(Supplier<Path> executablePathSupplier, KubesealCommandFactory commandFactory) {
        this.executablePathSupplier = executablePathSupplier;
        this.commandFactory = commandFactory;
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
                    plainSecretYaml);
            if (processResult.exitCode() != 0) {
                throw new KubesealExecutionException(
                        toUserFacingMessage(processResult.standardError(), "sealing"),
                        processResult.standardError());
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
                    sealedSecretYaml);

            if (processResult.exitCode() == 0) {
                return new ValidationResult(true, "SealedSecret validation succeeded.", processResult.standardOutput().strip());
            }

            return new ValidationResult(
                    false,
                    toUserFacingMessage(processResult.standardError(), "validation"),
                    processResult.standardError().strip());
        } catch (IOException exception) {
            throw unavailable(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KubesealExecutionException(
                    "kubeseal validation was interrupted before it finished.",
                    exception.getMessage(),
                    exception);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessResult processResult = execute(commandFactory.buildVersionCommand(executablePath()), "");
            return processResult.exitCode() == 0;
        } catch (IOException | InterruptedException exception) {
            return false;
        }
    }

    @Override
    public Path executablePath() {
        return executablePathSupplier.get();
    }

    private ProcessResult execute(List<String> command, String standardInput) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();

        try (OutputStream outputStream = process.getOutputStream()) {
            outputStream.write(standardInput.getBytes(StandardCharsets.UTF_8));
        }

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, stdout, stderr);
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
                "kubeseal is not available. Install it or configure -Dsealforge.kubeseal.path.",
                exception.getMessage(),
                exception);
    }

    private String toUserFacingMessage(String stderr, String operation) {
        String normalized = stderr == null ? "" : stderr.strip();
        if (normalized.isBlank()) {
            return "kubeseal failed during " + operation + ".";
        }
        if (normalized.contains("unable to decrypt sealed secret")) {
            return "kubeseal reported that the SealedSecret could not be decrypted for validation.";
        }
        if (normalized.startsWith("error:")) {
            return normalized.substring("error:".length()).trim();
        }
        return normalized.lines().findFirst().orElse("kubeseal failed during " + operation + ".");
    }

    private record ProcessResult(int exitCode, String standardOutput, String standardError) {
    }
}
