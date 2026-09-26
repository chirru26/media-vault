# MediaVault

MediaVault is a local-first desktop media management application built with Java 25, JavaFX, Maven and SQLite.

## Features

- Recursive folder scanning
- Image, video, audio and document indexing
- SHA-256 content hashing and duplicate detection
- SQLite local library with WAL mode
- Image thumbnails and large preview viewer
- Search by filename/path
- Media-type filtering
- Sorting by name, date and file size
- Favorites and Trash lifecycle
- Permanent deletion support
- Albums with many-to-many media membership
- Tags with many-to-many media membership
- Persistent application settings
- File metadata: size, modified time, MIME type, dimensions/duration fields
- Graceful handling of inaccessible files

## Architecture

```text
JavaFX UI
   |
Application / Services
   |-- Scanner
   |-- Filter
   |-- Sorter
   |-- Hashing
   |-- Thumbnail / Viewer
   |
Repositories
   |-- Media
   |-- Collections (Albums / Tags)
   |-- Settings
   |
SQLite (~/.mediavault/mediavault.db)
```

## Requirements

- JDK 25+
- Maven 3.9+

## Run

```bash
mvn clean javafx:run
```

The application stores its local database under the current user's `.mediavault` directory. Media files themselves are never copied by the indexer; MediaVault stores metadata and references the original local paths.
