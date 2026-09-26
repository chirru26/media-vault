package com.chirru26.mediavault.service;

import com.chirru26.mediavault.model.MediaItem;

import java.util.List;
import java.util.Locale;

public final class MediaFilter {
    public List<MediaItem> apply(List<MediaItem> items, String query, String type) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(item -> normalized.isEmpty()
                        || item.fileName().toLowerCase(Locale.ROOT).contains(normalized)
                        || item.filePath().toString().toLowerCase(Locale.ROOT).contains(normalized))
                .filter(item -> type == null || type.equals("All") || item.mediaType().name().equals(type))
                .toList();
    }
}
