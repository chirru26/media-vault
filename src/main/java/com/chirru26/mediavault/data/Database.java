package com.chirru26.mediavault.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".mediavault");
    private static final String JDBC_URL = "jdbc:sqlite:" + DATA_DIR.resolve("mediavault.db");

    private Database() {}

    public static void initialize() throws SQLException {
        try { Files.createDirectories(DATA_DIR); }
        catch (Exception e) { throw new SQLException("Unable to create MediaVault data directory", e); }
        try (Connection c = getConnection(); var s = c.createStatement()) {
            s.executeUpdate("PRAGMA journal_mode=WAL");
            s.executeUpdate("PRAGMA foreign_keys=ON");
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS media(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_name TEXT NOT NULL,
                    file_path TEXT NOT NULL UNIQUE,
                    mime_type TEXT NOT NULL,
                    media_type TEXT NOT NULL,
                    file_size INTEGER NOT NULL,
                    modified_at INTEGER NOT NULL,
                    content_hash TEXT,
                    width INTEGER,
                    height INTEGER,
                    duration_ms INTEGER,
                    indexed_at INTEGER NOT NULL,
                    favorite INTEGER NOT NULL DEFAULT 0,
                    trashed INTEGER NOT NULL DEFAULT 0
                )
                """);
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_media_type ON media(media_type)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_media_name ON media(file_name)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_media_hash ON media(content_hash)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS albums(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,created_at INTEGER NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS album_media(album_id INTEGER NOT NULL,media_id INTEGER NOT NULL,PRIMARY KEY(album_id,media_id),FOREIGN KEY(album_id) REFERENCES albums(id) ON DELETE CASCADE,FOREIGN KEY(media_id) REFERENCES media(id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS tags(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS media_tags(media_id INTEGER NOT NULL,tag_id INTEGER NOT NULL,PRIMARY KEY(media_id,tag_id),FOREIGN KEY(media_id) REFERENCES media(id) ON DELETE CASCADE,FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS settings(key TEXT PRIMARY KEY,value TEXT NOT NULL)");
        }
    }

    public static Connection getConnection() throws SQLException {
        var c = DriverManager.getConnection(JDBC_URL);
        c.createStatement().execute("PRAGMA foreign_keys=ON");
        return c;
    }
}
