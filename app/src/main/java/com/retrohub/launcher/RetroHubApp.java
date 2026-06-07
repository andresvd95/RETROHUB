package com.retrohub.launcher;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.retrohub.launcher.data.db.AppDatabase;

public class RetroHubApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // Eagerly initialize the database so the seed callback runs on first launch.
        AppDatabase.getInstance(this);
    }
}
