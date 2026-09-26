package com.chirru26.mediavault.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public final class HashService {
    public String sha256(Path file) throws Exception {
        var digest=MessageDigest.getInstance("SHA-256");
        try(InputStream in=Files.newInputStream(file)){
            byte[] buffer=new byte[1024*1024]; int read;
            while((read=in.read(buffer))!=-1) digest.update(buffer,0,read);
        }
        var out=new StringBuilder();
        for(byte b:digest.digest()) out.append(String.format("%02x",b));
        return out.toString();
    }
}
