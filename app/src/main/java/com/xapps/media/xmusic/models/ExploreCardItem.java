package com.xapps.media.xmusic.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ExploreCardItem {
    public final String id;
    public final String title;
    public final String subtitle;
    public final String imageUrl;
    public final String searchQuery;
    @Nullable public final Song directSong;

    public ExploreCardItem(@NonNull String id, @NonNull String title, @NonNull String subtitle,
                           @NonNull String imageUrl, @NonNull String searchQuery) {
        this(id, title, subtitle, imageUrl, searchQuery, null);
    }

    public ExploreCardItem(@NonNull String id, @NonNull String title, @NonNull String subtitle,
                           @NonNull String imageUrl, @NonNull String searchQuery, @Nullable Song directSong) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.searchQuery = searchQuery;
        this.directSong = directSong;
    }
}
