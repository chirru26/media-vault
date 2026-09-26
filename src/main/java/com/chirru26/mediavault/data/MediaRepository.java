package com.chirru26.mediavault.data;

import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.model.MediaType;

import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class MediaRepository {
    public void save(MediaItem item) throws SQLException {
        String sql = """
            INSERT INTO media(file_name,file_path,mime_type,media_type,file_size,modified_at,content_hash,width,height,duration_ms,indexed_at)
            VALUES(?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(file_path) DO UPDATE SET
              file_name=excluded.file_name, mime_type=excluded.mime_type, media_type=excluded.media_type,
              file_size=excluded.file_size, modified_at=excluded.modified_at, content_hash=excluded.content_hash,
              width=excluded.width, height=excluded.height, duration_ms=excluded.duration_ms, indexed_at=excluded.indexed_at
            """;
        try (var connection = Database.getConnection(); var ps = connection.prepareStatement(sql)) {
            ps.setString(1, item.fileName());
            ps.setString(2, item.filePath().toAbsolutePath().normalize().toString());
            ps.setString(3, item.mimeType());
            ps.setString(4, item.mediaType().name());
            ps.setLong(5, item.fileSize());
            ps.setLong(6, item.modifiedAt().toEpochMilli());
            setNullableString(ps, 7, item.contentHash());
            setNullableInt(ps, 8, item.width());
            setNullableInt(ps, 9, item.height());
            setNullableLong(ps, 10, item.durationMs());
            ps.setLong(11, item.indexedAt().toEpochMilli());
            ps.executeUpdate();
        }
    }

    public List<MediaItem> findAll() throws SQLException {
        var result = new ArrayList<MediaItem>();
        try (var connection = Database.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM media ORDER BY indexed_at DESC");
             var rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public long count() throws SQLException {
        try (var connection = Database.getConnection();
             var ps = connection.prepareStatement("SELECT COUNT(*) FROM media");
             var rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private MediaItem map(java.sql.ResultSet rs) throws SQLException {
        return new MediaItem(
                rs.getLong("id"), rs.getString("file_name"), Path.of(rs.getString("file_path")),
                rs.getString("mime_type"), MediaType.valueOf(rs.getString("media_type")), rs.getLong("file_size"),
                Instant.ofEpochMilli(rs.getLong("modified_at")), rs.getString("content_hash"),
                nullableInt(rs, "width"), nullableInt(rs, "height"), nullableLong(rs, "duration_ms"),
                Instant.ofEpochMilli(rs.getLong("indexed_at")));
    }

    private static Integer nullableInt(java.sql.ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column); return rs.wasNull() ? null : value;
    }
    private static Long nullableLong(java.sql.ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column); return rs.wasNull() ? null : value;
    }
    private static void setNullableString(java.sql.PreparedStatement ps, int i, String value) throws SQLException {
        if (value == null) ps.setNull(i, Types.VARCHAR); else ps.setString(i, value);
    }
    private static void setNullableInt(java.sql.PreparedStatement ps, int i, Integer value) throws SQLException {
        if (value == null) ps.setNull(i, Types.INTEGER); else ps.setInt(i, value);
    }
    private static void setNullableLong(java.sql.PreparedStatement ps, int i, Long value) throws SQLException {
        if (value == null) ps.setNull(i, Types.BIGINT); else ps.setLong(i, value);
    }
}
