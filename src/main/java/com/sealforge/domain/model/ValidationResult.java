package com.sealforge.domain.model;

public record ValidationResult(
        boolean success,
        String message,
        String details) {
}

