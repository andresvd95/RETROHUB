package com.retrohub.launcher.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_entries")
public class GameEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String platformId;
    public int emulatorId;
    public String romPath;
    public String coverUri;
    public String genre;
    public long playTimeSeconds;
    public long lastPlayedAt;
    public long addedAt;
    public boolean isFavorite;
    public String customArgs;
}
