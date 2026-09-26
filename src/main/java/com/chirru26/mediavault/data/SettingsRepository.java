package com.chirru26.mediavault.data;

import java.sql.SQLException;

public final class SettingsRepository {
    public void set(String key,String value)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("INSERT INTO settings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")){p.setString(1,key);p.setString(2,value);p.executeUpdate();}}
    public String get(String key,String fallback)throws SQLException{try(var c=Database.getConnection();var p=c.prepareStatement("SELECT value FROM settings WHERE key=?")){p.setString(1,key);try(var r=p.executeQuery()){return r.next()?r.getString(1):fallback;}}}
}
