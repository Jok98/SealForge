package com.sealforge.application.dto;

import com.sealforge.domain.enumtype.SealingScope;

import java.util.List;

public record SecretDraftInput(
        String name,
        String namespace,
        String type,
        SealingScope scope,
        List<SecretEntryInput> entries) {

    public static SecretDraftInput empty(String defaultSecretType) {
        return new SecretDraftInput("", "", defaultSecretType, SealingScope.STRICT, List.of());
    }
}

