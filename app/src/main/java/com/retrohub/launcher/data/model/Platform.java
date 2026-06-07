package com.retrohub.launcher.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "platforms")
public class Platform {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String name;
    public String shortName;
    public String colorHex;
    public String emojiIcon;
    public int sortOrder;

    public Platform() {}

    public Platform(@NonNull String id, String name, String shortName,
                    String colorHex, String emojiIcon, int sortOrder) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.colorHex = colorHex;
        this.emojiIcon = emojiIcon;
        this.sortOrder = sortOrder;
    }
}
