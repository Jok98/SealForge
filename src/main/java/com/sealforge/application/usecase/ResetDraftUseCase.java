package com.sealforge.application.usecase;

import com.sealforge.application.dto.SecretDraftInput;

public final class ResetDraftUseCase {

    public SecretDraftInput execute(String defaultSecretType) {
        return SecretDraftInput.empty(defaultSecretType);
    }
}

