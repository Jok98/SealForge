package com.sealforge.application.usecase;

import com.sealforge.application.service.KubesealGateway;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.SecretDraft;

public final class SealSecretUseCase {

    private final KubesealGateway kubesealGateway;

    public SealSecretUseCase(KubesealGateway kubesealGateway) {
        this.kubesealGateway = kubesealGateway;
    }

    public String execute(String plainSecretYaml, SecretDraft draft, CertificateReference certificateReference) {
        return kubesealGateway.seal(plainSecretYaml, certificateReference, draft.scope());
    }
}

