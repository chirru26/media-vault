package com.chirru26.mediavault.model;

import java.nio.file.Path;
import java.time.Instant;

public record MediaItem(
        long id,
        String fileName,
        Path filePath,
        String mimeType,
        MediaType mediaType,
        long fileSize,
        Instant modifiedAt,
        String contentHash,
        Integer width,
        Integer height,
        Long durationMs,
        Instant indexedAt
) {}
