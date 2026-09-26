package com.chirru26.mediavault.media;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ThumbnailService {
    private static final int SIZE = 240;

    public Image load(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return null;
        }
        try {
            return new Image(path.toUri().toString(), SIZE, SIZE, true, true, true);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public byte[] readPreviewBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}
