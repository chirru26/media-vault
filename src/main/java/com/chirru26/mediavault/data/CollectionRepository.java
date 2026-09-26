package com.chirru26.mediavault.data;

import java.sql.SQLException;
import java.util.*;

public final class CollectionRepository {
    public long createAlbum(String name)throws SQLException{String n=name==null?"":name.trim();if(n.isBlank())throw new IllegalArgumentException("Album name cannot be empty");try(var c=Database.getConnection();var p=c.prepareStatement("INSERT INTO albums(name,created_at) VALUES(?,?)",java.sql.Statement.RETURN_GENERATED_KEYS)){p.setString(1,n);p.setLong(2,System.currentTimeMillis());p.executeUpdate();try(var k=p.getGeneratedKeys()){return k.next()?k.getLong(1):0;}}}
    public void deleteAlbum(long id)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("DELETE FROM albums WHERE id=?")){p.setLong(1,id);p.executeUpdate();}}
    public List<String> albums()throws SQLException{return names("SELECT name FROM albums ORDER BY name");}
    public long albumId(String name)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("SELECT id FROM albums WHERE name=?")){p.setString(1,name);try(var r=p.executeQuery()){return r.next()?r.getLong(1):-1;}}}
    public void addToAlbum(long albumId,long mediaId)throws SQLException{link("album_media","album_id","media_id",albumId,mediaId);}
    public void removeFromAlbum(long albumId,long mediaId)throws SQLException{unlink("album_media","album_id","media_id",albumId,mediaId);}
    public List<String> albumsForMedia(long mediaId)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("SELECT a.name FROM albums a JOIN album_media am ON am.album_id=a.id WHERE am.media_id=? ORDER BY a.name")){p.setLong(1,mediaId);try(var r=p.executeQuery()){var out=new ArrayList<String>();while(r.next())out.add(r.getString(1));return out;}}}
    public void addTag(String tag)throws SQLException{String t=tag==null?"":tag.trim();if(t.isBlank())return;try(var c=Database.getConnection();var p=c.prepareStatement("INSERT OR IGNORE INTO tags(name) VALUES(?)")){p.setString(1,t);p.executeUpdate();}}
    public List<String> tags()throws SQLException{return names("SELECT name FROM tags ORDER BY name");}
    public void tagMedia(long mediaId,String tag)throws SQLException{addTag(tag);try(var c=Database.getConnection();var p=c.prepareStatement("INSERT OR IGNORE INTO media_tags(media_id,tag_id) SELECT ?,id FROM tags WHERE name=?")){p.setLong(1,mediaId);p.setString(2,tag.trim());p.executeUpdate();}}
    public void untagMedia(long mediaId,String tag)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("DELETE FROM media_tags WHERE media_id=? AND tag_id=(SELECT id FROM tags WHERE name=?)")){p.setLong(1,mediaId);p.setString(2,tag.trim());p.executeUpdate();}}
    public List<String> tagsForMedia(long mediaId)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("SELECT t.name FROM tags t JOIN media_tags mt ON mt.tag_id=t.id WHERE mt.media_id=? ORDER BY t.name")){p.setLong(1,mediaId);try(var r=p.executeQuery()){var out=new ArrayList<String>();while(r.next())out.add(r.getString(1));return out;}}}
    private List<String> names(String sql)throws SQLException{var out=new ArrayList<String>();try(var c=Database.getConnection();var p=c.prepareStatement(sql);var r=p.executeQuery()){while(r.next())out.add(r.getString(1));}return out;}
    private void link(String table,String a,String b,long x,long y)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("INSERT OR IGNORE INTO "+table+"("+a+","+b+") VALUES(?,?)")){p.setLong(1,x);p.setLong(2,y);p.executeUpdate();}}
    private void unlink(String table,String a,String b,long x,long y)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("DELETE FROM "+table+" WHERE "+a+"=? AND "+b+"=?")){p.setLong(1,x);p.setLong(2,y);p.executeUpdate();}}
}
