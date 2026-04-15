package com.sealforge.application.usecase;

import com.sealforge.application.service.FileExportService;
import com.sealforge.domain.exception.ValidationException;

import java.nio.file.Path;

public final class ExportYamlUseCase {

    private final FileExportService fileExportService;

    public ExportYamlUseCase(FileExportService fileExportService) {
        this.fileExportService = fileExportService;
    }

    public void execute(String yamlContent, Path targetPath) {
        if (yamlContent == null || yamlContent.isBlank()) {
            throw new ValidationException("There is no YAML to export.");
        }
        fileExportService.writeText(targetPath, yamlContent);
    }
}

