package com.sealforge.app;

import com.sealforge.application.service.KubesealGateway;
import com.sealforge.application.usecase.CopyYamlToClipboardUseCase;
import com.sealforge.application.usecase.CreateSecretDraftUseCase;
import com.sealforge.application.usecase.ExportYamlUseCase;
import com.sealforge.application.usecase.GenerateYamlUseCase;
import com.sealforge.application.usecase.LoadCertificateUseCase;
import com.sealforge.application.usecase.ResetDraftUseCase;
import com.sealforge.application.usecase.SealSecretUseCase;
import com.sealforge.application.usecase.ValidateSealedSecretUseCase;
import com.sealforge.config.AppConfig;

public record AppContext(
        AppConfig appConfig,
        KubesealGateway kubesealGateway,
        LoadCertificateUseCase loadCertificateUseCase,
        CreateSecretDraftUseCase createSecretDraftUseCase,
        GenerateYamlUseCase generateYamlUseCase,
        SealSecretUseCase sealSecretUseCase,
        ValidateSealedSecretUseCase validateSealedSecretUseCase,
        ExportYamlUseCase exportYamlUseCase,
        CopyYamlToClipboardUseCase copyYamlToClipboardUseCase,
        ResetDraftUseCase resetDraftUseCase) {
}
