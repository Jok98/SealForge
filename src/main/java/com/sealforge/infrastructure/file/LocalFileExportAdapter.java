package com.sealforge.infrastructure.file;

import com.sealforge.application.service.FileExportService;
import com.sealforge.domain.exception.TechnicalFailureException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class LocalFileExportAdapter implements FileExportService {

    @Override
    public void writeText(Path targetPath, String content) {
        try {
            Files.writeString(
                    targetPath,
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new TechnicalFailureException(
                    "The file could not be written.",
                    exception.getMessage(),
                    exception);
        }
    }
}

