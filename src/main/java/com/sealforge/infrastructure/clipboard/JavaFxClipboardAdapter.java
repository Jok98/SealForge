package com.sealforge.infrastructure.clipboard;

import com.sealforge.application.service.SystemClipboardService;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public final class JavaFxClipboardAdapter implements SystemClipboardService {

    @Override
    public void copyText(String content) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);
    }
}

