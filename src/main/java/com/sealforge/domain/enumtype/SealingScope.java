package com.sealforge.domain.enumtype;

public enum SealingScope {
    STRICT("strict", "Safest default. Name and namespace are bound into the encryption."),
    NAMESPACE_WIDE("namespace-wide", "Allows renaming inside the same namespace."),
    CLUSTER_WIDE("cluster-wide", "Most permissive. Can be unsealed with any name or namespace.");

    private final String kubesealValue;
    private final String description;

    SealingScope(String kubesealValue, String description) {
        this.kubesealValue = kubesealValue;
        this.description = description;
    }

    public String kubesealValue() {
        return kubesealValue;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return switch (this) {
            case STRICT -> "Strict";
            case NAMESPACE_WIDE -> "Namespace-wide";
            case CLUSTER_WIDE -> "Cluster-wide";
        };
    }
}

