package com.sealforge.domain.model;

import java.nio.file.Path;

public record KubesealRuntimeStatus(
        boolean available,
        Path executablePath,
        String version,
        boolean validationSupported,
        String message,
        String technicalDetails) {
}
