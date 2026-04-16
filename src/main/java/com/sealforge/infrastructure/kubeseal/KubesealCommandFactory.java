package com.sealforge.infrastructure.kubeseal;

import com.sealforge.domain.enumtype.SealingScope;

import java.nio.file.Path;
import java.util.List;

public final class KubesealCommandFactory {

    public List<String> buildSealCommand(Path executable, Path certificatePath, SealingScope scope) {
        return List.of(
                executable.toString(),
                "--cert",
                certificatePath.toString(),
                "--scope",
                scope.kubesealValue());
    }

    public List<String> buildValidateCommand(Path executable) {
        return List.of(executable.toString(), "--validate");
    }

    public List<String> buildVersionCommand(Path executable) {
        return List.of(executable.toString(), "--version");
    }

    public List<String> buildHelpCommand(Path executable) {
        return List.of(executable.toString(), "--help");
    }
}
