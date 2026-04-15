package com.sealforge.domain.model;

import com.sealforge.domain.enumtype.SealingScope;

import java.util.List;

public record SecretDraft(
        String name,
        String namespace,
        String type,
        SealingScope scope,
        List<SecretEntry> entries) {
}

