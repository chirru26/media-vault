# MediaVault

MediaVault is a local-first desktop media management application built with **Java 25, JavaFX, Maven and SQLite**. It is designed as a real desktop application: the UI, local database, indexing services and file operations work together without requiring a server.

## Desktop features

- Recursive folder indexing and individual-file import
- Image, video, audio and document classification
- SHA-256 content hashing and duplicate detection
- SQLite local library with WAL mode
- Search by filename and path
- Media-type filters
- Image thumbnails and a large media viewer
- Metadata/details panel
- Favorites
- Soft Trash with restore-ready database state and permanent deletion
- Rename and move files from the desktop UI
- Show a file in the system file manager
- Albums with many-to-many media membership
- Tags with many-to-many media membership
- Settings persisted in SQLite
- Background indexing so the UI remains responsive
- GitHub Actions build/test verification

## Architecture

```text
JavaFX Desktop UI
   |
Application / Services
   |-- Scanner
   |-- Filter
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

The application stores its database under the current user's `.mediavault` directory. Media files are not copied into the database; MediaVault indexes metadata and keeps references to the original local files.

## Build and test

```bash
mvn clean test
```

## Current development branch

`desktop-complete` contains the complete desktop UI integration on top of the `feature/foundation-v2` foundation.
