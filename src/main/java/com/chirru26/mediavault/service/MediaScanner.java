package com.chirru26.mediavault.service;

import com.chirru26.mediavault.data.MediaRepository;
import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.model.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

public final class MediaScanner {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mkv", "mov", "avi", "webm", "m4v");
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "flac", "aac", "m4a", "ogg");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx");

    private final MediaRepository repository;

    public MediaScanner(MediaRepository repository) {
        this.repository = repository;
    }

    public ScanResult scan(Path root) throws IOException {
        if (!Files.isDirectory(root)) throw new IllegalArgumentException("Not a directory: " + root);
        int discovered = 0;
        int indexed = 0;
        try (var stream = Files.walk(root)) {
            var iterator = stream.filter(Files::isRegularFile).iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                discovered++;
                var type = detectType(path);
                if (type == MediaType.OTHER) continue;
                try {
                    var attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    var item = new MediaItem(0, path.getFileName().toString(), path.toAbsolutePath().normalize(),
                            Files.probeContentType(path) == null ? "application/octet-stream" : Files.probeContentType(path),
                            type, attrs.size(), attrs.lastModifiedTime().toMillis() == 0 ? Instant.EPOCH : attrs.lastModifiedTime().toInstant(),
                            null, null, null, null, Instant.now());
                    repository.save(item);
                    indexed++;
                } catch (IOException | java.sql.SQLException ignored) {
                    // A single inaccessible file must not abort a complete library scan.
                }
            }
        }
        return new ScanResult(discovered, indexed);
    }

    public MediaType detectType(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0) return MediaType.OTHER;
        String ext = name.substring(dot + 1);
        if (IMAGE_EXTENSIONS.contains(ext)) return MediaType.IMAGE;
        if (VIDEO_EXTENSIONS.contains(ext)) return MediaType.VIDEO;
        if (AUDIO_EXTENSIONS.contains(ext)) return MediaType.AUDIO;
        if (DOCUMENT_EXTENSIONS.contains(ext)) return MediaType.DOCUMENT;
        return MediaType.OTHER;
    }

    public record ScanResult(int discovered, int indexed) {}
}
