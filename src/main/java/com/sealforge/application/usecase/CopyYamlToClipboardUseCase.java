package com.sealforge.application.usecase;

import com.sealforge.application.service.SystemClipboardService;
import com.sealforge.domain.exception.ValidationException;

public final class CopyYamlToClipboardUseCase {

    private final SystemClipboardService systemClipboardService;

    public CopyYamlToClipboardUseCase(SystemClipboardService systemClipboardService) {
        this.systemClipboardService = systemClipboardService;
    }

    public void execute(String yamlContent) {
        if (yamlContent == null || yamlContent.isBlank()) {
            throw new ValidationException("There is no YAML to copy.");
        }
        systemClipboardService.copyText(yamlContent);
    }
}

