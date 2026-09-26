package com.chirru26.mediavault.media;

import java.nio.file.Path;
import java.time.Instant;

public record MediaItem(
        long id,
        String fileName,
        Path filePath,
        String mediaType,
        long fileSize,
        Instant modifiedAt,
        String thumbnailPath
) {}
