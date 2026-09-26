package com.chirru26.mediavault.service;

import com.chirru26.mediavault.model.MediaItem;
import java.util.Comparator;
import java.util.List;

public final class MediaSorter {
    public enum Sort { NAME_ASC, NAME_DESC, NEWEST, OLDEST, SIZE_LARGE, SIZE_SMALL }
    public List<MediaItem> sort(List<MediaItem> items, Sort sort) {
        Comparator<MediaItem> c=switch(sort){
            case NAME_ASC -> Comparator.comparing(MediaItem::fileName,String.CASE_INSENSITIVE_ORDER);
            case NAME_DESC -> Comparator.comparing(MediaItem::fileName,String.CASE_INSENSITIVE_ORDER).reversed();
            case NEWEST -> Comparator.comparing(MediaItem::modifiedAt).reversed();
            case OLDEST -> Comparator.comparing(MediaItem::modifiedAt);
            case SIZE_LARGE -> Comparator.comparingLong(MediaItem::fileSize).reversed();
            case SIZE_SMALL -> Comparator.comparingLong(MediaItem::fileSize);
        };
        return items.stream().sorted(c).toList();
    }
}
