package com.chirru26.mediavault.media;

import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ThumbnailService {
    private static final int SIZE = 240;
    private static final int VIEWER_SIZE = 1400;

    public Image load(Path path) {
        return load(path, SIZE);
    }

    public Image loadLarge(Path path) {
        return load(path, VIEWER_SIZE);
    }

    private Image load(Path path, int size) {
        if (path == null || !Files.isRegularFile(path)) return null;
        try {
            return new Image(path.toUri().toString(), size, size, true, true, true);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public byte[] readPreviewBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}
