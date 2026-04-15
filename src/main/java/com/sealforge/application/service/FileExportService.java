package com.sealforge.application.service;

import java.nio.file.Path;

public interface FileExportService {

    void writeText(Path targetPath, String content);
}

