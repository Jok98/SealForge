package com.sealforge.application.service;

import com.sealforge.domain.model.SecretDraft;

public interface SecretYamlRenderer {

    String render(SecretDraft secretDraft);
}

