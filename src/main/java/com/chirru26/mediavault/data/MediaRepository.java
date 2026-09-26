package com.chirru26.mediavault.data;

import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.model.MediaType;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public final class MediaRepository {
    public void save(MediaItem item) throws SQLException {
        String sql = """INSERT INTO media(file_name,file_path,mime_type,media_type,file_size,modified_at,content_hash,width,height,duration_ms,indexed_at)
        VALUES(?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(file_path) DO UPDATE SET file_name=excluded.file_name,mime_type=excluded.mime_type,media_type=excluded.media_type,file_size=excluded.file_size,modified_at=excluded.modified_at,content_hash=excluded.content_hash,width=excluded.width,height=excluded.height,duration_ms=excluded.duration_ms,indexed_at=excluded.indexed_at""";
        try (var c = Database.getConnection(); var p = c.prepareStatement(sql)) {
            p.setString(1,item.fileName()); p.setString(2,item.filePath().toAbsolutePath().normalize().toString());
            p.setString(3,item.mimeType()); p.setString(4,item.mediaType().name()); p.setLong(5,item.fileSize());
            p.setLong(6,item.modifiedAt().toEpochMilli()); set(p,7,item.contentHash()); set(p,8,item.width()); set(p,9,item.height());
            set(p,10,item.durationMs()); p.setLong(11,item.indexedAt().toEpochMilli()); p.executeUpdate();
        }
    }
    public List<MediaItem> findAll() throws SQLException { return find("WHERE trashed=0",""); }
    public List<MediaItem> findFavorites() throws SQLException { return find("WHERE favorite=1 AND trashed=0",""); }
    public List<MediaItem> findTrash() throws SQLException { return find("WHERE trashed=1",""); }
    public List<MediaItem> findDuplicates() throws SQLException { return find("WHERE content_hash IN (SELECT content_hash FROM media WHERE content_hash IS NOT NULL GROUP BY content_hash HAVING COUNT(*)>1) AND trashed=0","ORDER BY content_hash,file_name"); }
    public List<MediaItem> findByAlbum(long albumId) throws SQLException { return find("WHERE trashed=0 AND id IN (SELECT media_id FROM album_media WHERE album_id=" + albumId + ")",""); }
    public List<MediaItem> findByTag(String tag) throws SQLException { return find("WHERE trashed=0 AND id IN (SELECT mt.media_id FROM media_tags mt JOIN tags t ON t.id=mt.tag_id WHERE t.name='" + tag.replace("'","''") + "')",""); }
    public void setFavorite(long id, boolean value) throws SQLException { updateFlag(id,"favorite",value); }
    public void setTrashed(long id, boolean value) throws SQLException { updateFlag(id,"trashed",value); }
    public void deletePermanently(long id) throws SQLException { try(var c=Database.getConnection();var p=c.prepareStatement("DELETE FROM media WHERE id=?")){p.setLong(1,id);p.executeUpdate();} }
    public void updatePath(long id, Path path) throws SQLException { try(var c=Database.getConnection();var p=c.prepareStatement("UPDATE media SET file_name=?,file_path=?,modified_at=?,indexed_at=? WHERE id=?")){p.setString(1,path.getFileName().toString());p.setString(2,path.toAbsolutePath().normalize().toString());p.setLong(3,FilesModified(path));p.setLong(4,System.currentTimeMillis());p.setLong(5,id);p.executeUpdate();} }
    public long count() throws SQLException { try(var c=Database.getConnection();var p=c.prepareStatement("SELECT COUNT(*) FROM media WHERE trashed=0");var r=p.executeQuery()){return r.next()?r.getLong(1):0;} }
    public long countAll() throws SQLException { try(var c=Database.getConnection();var p=c.prepareStatement("SELECT COUNT(*) FROM media");var r=p.executeQuery()){return r.next()?r.getLong(1):0;} }
    private long FilesModified(Path path){try{return java.nio.file.Files.getLastModifiedTime(path).toMillis();}catch(Exception e){return System.currentTimeMillis();}}
    private List<MediaItem> find(String where,String order) throws SQLException { var out=new ArrayList<MediaItem>(); String sql="SELECT * FROM media "+where+" "+(order.isBlank()?"ORDER BY indexed_at DESC":order); try(var c=Database.getConnection();var p=c.prepareStatement(sql);var r=p.executeQuery()){while(r.next())out.add(map(r));} return out; }
    private void updateFlag(long id,String col,boolean value)throws SQLException{if(!Set.of("favorite","trashed").contains(col))throw new IllegalArgumentException("Invalid flag");try(var c=Database.getConnection();var p=c.prepareStatement("UPDATE media SET "+col+"=? WHERE id=?")){p.setInt(1,value?1:0);p.setLong(2,id);p.executeUpdate();}}
    private MediaItem map(ResultSet r)throws SQLException{return new MediaItem(r.getLong("id"),r.getString("file_name"),Path.of(r.getString("file_path")),r.getString("mime_type"),MediaType.valueOf(r.getString("media_type")),r.getLong("file_size"),Instant.ofEpochMilli(r.getLong("modified_at")),r.getString("content_hash"),nullableInt(r,"width"),nullableInt(r,"height"),nullableLong(r,"duration_ms"),Instant.ofEpochMilli(r.getLong("indexed_at")));}
    private static Integer nullableInt(ResultSet r,String c)throws SQLException{int v=r.getInt(c);return r.wasNull()?null:v;}
    private static Long nullableLong(ResultSet r,String c)throws SQLException{long v=r.getLong(c);return r.wasNull()?null:v;}
    private static void set(PreparedStatement p,int i,Object v)throws SQLException{if(v==null)p.setNull(i,Types.NULL);else if(v instanceof String s)p.setString(i,s);else if(v instanceof Integer n)p.setInt(i,n);else if(v instanceof Long n)p.setLong(i,n);}
}
