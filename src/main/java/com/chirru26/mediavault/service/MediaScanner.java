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
    private static final Set<String> IMAGE_EXTENSIONS=Set.of("jpg","jpeg","png","gif","webp","bmp","heic","tiff");
    private static final Set<String> VIDEO_EXTENSIONS=Set.of("mp4","mkv","mov","avi","webm","m4v","3gp");
    private static final Set<String> AUDIO_EXTENSIONS=Set.of("mp3","wav","flac","aac","m4a","ogg","opus");
    private static final Set<String> DOCUMENT_EXTENSIONS=Set.of("pdf","doc","docx","txt","xls","xlsx","ppt","pptx","csv","rtf");
    private final MediaRepository repository;
    private final HashService hashService=new HashService();
    public MediaScanner(MediaRepository repository){this.repository=repository;}

    public ScanResult scan(Path root)throws IOException{
        if(Files.isRegularFile(root)) return indexOne(root)?new ScanResult(1,1):new ScanResult(1,0);
        if(!Files.isDirectory(root))throw new IllegalArgumentException("Not a file or directory: "+root);
        int discovered=0,indexed=0;
        try(var stream=Files.walk(root)){var it=stream.filter(Files::isRegularFile).iterator();while(it.hasNext()){discovered++;if(indexOne(it.next()))indexed++;}}
        return new ScanResult(discovered,indexed);
    }
    private boolean indexOne(Path path){var type=detectType(path);if(type==MediaType.OTHER)return false;try{
        var attrs=Files.readAttributes(path,BasicFileAttributes.class);String mime=Files.probeContentType(path);String hash=hashService.sha256(path);
        repository.save(new MediaItem(0,path.getFileName().toString(),path.toAbsolutePath().normalize(),mime==null?"application/octet-stream":mime,type,attrs.size(),attrs.lastModifiedTime().toInstant(),hash,null,null,null,Instant.now()));return true;
    }catch(Exception ignored){return false;}}
    public MediaType detectType(Path path){String name=path.getFileName().toString().toLowerCase(Locale.ROOT);int dot=name.lastIndexOf('.');if(dot<0)return MediaType.OTHER;String ext=name.substring(dot+1);if(IMAGE_EXTENSIONS.contains(ext))return MediaType.IMAGE;if(VIDEO_EXTENSIONS.contains(ext))return MediaType.VIDEO;if(AUDIO_EXTENSIONS.contains(ext))return MediaType.AUDIO;if(DOCUMENT_EXTENSIONS.contains(ext))return MediaType.DOCUMENT;return MediaType.OTHER;}
    public record ScanResult(int discovered,int indexed){}
}
