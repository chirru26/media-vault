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
        try {
            Files.createDirectories(DATA_DIR);
        } catch (Exception e) {
            throw new SQLException("Unable to create MediaVault data directory", e);
        }
        try (Connection connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("PRAGMA foreign_keys = ON");
                statement.executeUpdate("PRAGMA journal_mode = WAL");
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS media (
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
                        indexed_at INTEGER NOT NULL
                    )
                    """);
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_media_type ON media(media_type)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_media_name ON media(file_name)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_media_hash ON media(content_hash)");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        var connection = DriverManager.getConnection(JDBC_URL);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        return connection;
    }
}
