package com.sealforge.application.usecase;

import com.sealforge.application.service.SecretYamlRenderer;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.validation.SecretDraftValidator;

public final class GenerateYamlUseCase {

    private final SecretDraftValidator validator;
    private final SecretYamlRenderer secretYamlRenderer;

    public GenerateYamlUseCase(SecretDraftValidator validator, SecretYamlRenderer secretYamlRenderer) {
        this.validator = validator;
        this.secretYamlRenderer = secretYamlRenderer;
    }

    public String execute(SecretDraft draft) {
        validator.validate(draft);
        return secretYamlRenderer.render(draft);
    }
}

