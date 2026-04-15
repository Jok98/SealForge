package com.sealforge.application.usecase;

import com.sealforge.application.service.KubesealGateway;
import com.sealforge.domain.exception.ValidationException;
import com.sealforge.domain.model.ValidationResult;

public final class ValidateSealedSecretUseCase {

    private final KubesealGateway kubesealGateway;

    public ValidateSealedSecretUseCase(KubesealGateway kubesealGateway) {
        this.kubesealGateway = kubesealGateway;
    }

    public ValidationResult execute(String sealedSecretYaml) {
        if (sealedSecretYaml == null || sealedSecretYaml.isBlank()) {
            throw new ValidationException("Generate or paste a SealedSecret before validating.");
        }
        return kubesealGateway.validate(sealedSecretYaml);
    }
}

