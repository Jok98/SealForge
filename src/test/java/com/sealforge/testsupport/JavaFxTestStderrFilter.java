package com.sealforge.testsupport;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public final class JavaFxTestStderrFilter {

    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);

    private JavaFxTestStderrFilter() {
    }

    public static void install() {
        if (INSTALLED.compareAndSet(false, true)) {
            PrintStream original = System.err;
            System.setErr(new PrintStream(new FilteringOutputStream(original), true, StandardCharsets.UTF_8));
        }
    }

    private static final class FilteringOutputStream extends OutputStream {

        private final PrintStream delegate;
        private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream(256);
        private boolean suppressRendererStackTrace;
        private String pendingPlatformStartupLine;

        private FilteringOutputStream(PrintStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public synchronized void write(int value) {
            if (value == '\r') {
                return;
            }
            if (value == '\n') {
                flushBufferedLine();
                return;
            }
            lineBuffer.write(value);
        }

        @Override
        public synchronized void flush() {
            if (lineBuffer.size() > 0) {
                flushBufferedLine();
            }
            if (pendingPlatformStartupLine != null) {
                emit(pendingPlatformStartupLine);
                pendingPlatformStartupLine = null;
            }
            delegate.flush();
        }

        private void flushBufferedLine() {
            String line = lineBuffer.toString(StandardCharsets.UTF_8);
            lineBuffer.reset();
            handleLine(line);
        }

        private void handleLine(String line) {
            if (pendingPlatformStartupLine != null) {
                if (line.contains("Unsupported JavaFX configuration: classes were loaded from 'unnamed module")) {
                    pendingPlatformStartupLine = null;
                    return;
                }
                emit(pendingPlatformStartupLine);
                pendingPlatformStartupLine = null;
            }

            if (suppressRendererStackTrace) {
                if (isRendererStackTraceLine(line)) {
                    return;
                }
                suppressRendererStackTrace = false;
            }

            if (line.contains("com.sun.javafx.application.PlatformImpl startup")) {
                pendingPlatformStartupLine = line;
                return;
            }

            if (line.contains("java.nio.BufferOverflowException")) {
                suppressRendererStackTrace = true;
                return;
            }

            emit(line);
        }

        private boolean isRendererStackTraceLine(String line) {
            return line.isBlank()
                    || line.startsWith("\tat ")
                    || line.startsWith("at ")
                    || line.startsWith("\t... ")
                    || line.startsWith("Caused by:")
                    || line.startsWith("Suppressed:")
                    || line.contains("java.nio.BufferOverflowException");
        }

        private void emit(String line) {
            delegate.println(line);
        }
    }
}
