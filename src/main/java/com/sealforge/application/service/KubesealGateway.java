package com.sealforge.application.service;

import com.sealforge.domain.enumtype.SealingScope;
import com.sealforge.domain.model.CertificateReference;
import com.sealforge.domain.model.KubesealRuntimeStatus;
import com.sealforge.domain.model.ValidationResult;

import java.nio.file.Path;

public interface KubesealGateway {

    String seal(String plainSecretYaml, CertificateReference certificateReference, SealingScope scope);

    ValidationResult validate(String sealedSecretYaml);

    KubesealRuntimeStatus inspectStatus();

    boolean isAvailable();

    Path executablePath();
}
