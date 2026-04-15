package com.sealforge.application.usecase;

import com.sealforge.application.dto.SecretDraftInput;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.SecretEntry;

import java.util.List;

public final class CreateSecretDraftUseCase {

    public SecretDraft execute(SecretDraftInput input) {
        List<SecretEntry> entries = input.entries().stream()
                .map(entry -> new SecretEntry(entry.key(), entry.value()))
                .toList();

        return new SecretDraft(
                input.name(),
                input.namespace(),
                input.type(),
                input.scope(),
                entries);
    }
}

