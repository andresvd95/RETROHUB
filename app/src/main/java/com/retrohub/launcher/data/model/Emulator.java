package com.retrohub.launcher.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emulators")
public class Emulator {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String packageName;
    public String activityName;
    public String platformId;
    public String iconUri;
    public boolean isInstalled;
    public boolean enabled = true;
    public long addedAt;
}
