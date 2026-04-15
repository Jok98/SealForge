package com.sealforge.domain.validation;

import com.sealforge.domain.exception.ValidationException;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.SecretEntry;

import java.util.HashSet;
import java.util.Set;

public final class SecretDraftValidator {

    public void validate(SecretDraft draft) {
        if (draft == null) {
            throw new ValidationException("A secret draft is required.");
        }
        if (!KubernetesNameValidator.isValid(draft.name())) {
            throw new ValidationException("Secret name must be a valid Kubernetes DNS subdomain.");
        }
        if (!KubernetesNameValidator.isValid(draft.namespace())) {
            throw new ValidationException("Namespace must be a valid Kubernetes DNS subdomain.");
        }
        if (draft.type() == null || draft.type().isBlank()) {
            throw new ValidationException("Secret type is required.");
        }
        if (draft.scope() == null) {
            throw new ValidationException("A sealing scope must be selected.");
        }
        if (draft.entries() == null || draft.entries().isEmpty()) {
            throw new ValidationException("At least one secret entry is required.");
        }

        Set<String> keys = new HashSet<>();
        for (SecretEntry entry : draft.entries()) {
            if (entry.key() == null || entry.key().isBlank()) {
                throw new ValidationException("Secret entry keys cannot be empty.");
            }
            if (!keys.add(entry.key())) {
                throw new ValidationException("Duplicate secret entry keys are not allowed: " + entry.key());
            }
        }
    }
}

