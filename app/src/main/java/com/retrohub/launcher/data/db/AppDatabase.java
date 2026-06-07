package com.retrohub.launcher.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Platform.class, Emulator.class, GameEntry.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PlatformDao platformDao();
    public abstract EmulatorDao emulatorDao();
    public abstract GameEntryDao gameEntryDao();

    private static volatile AppDatabase INSTANCE;
    private static final int IO_THREADS = 4;
    public static final ExecutorService io = Executors.newFixedThreadPool(IO_THREADS);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "retrohub.db")
                        .addCallback(SEED_CALLBACK)
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Callback SEED_CALLBACK = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            io.execute(() -> {
                if (INSTANCE == null) return;
                INSTANCE.platformDao().insertAll(seedPlatforms());
            });
        }
    };

    private static List<Platform> seedPlatforms() {
        return Arrays.asList(
            new Platform("nes", "Nintendo Entertainment System", "NES", "#E60012", "🎮", 10),
            new Platform("snes", "Super Nintendo", "SNES", "#8B0000", "🕹️", 20),
            new Platform("n64", "Nintendo 64", "N64", "#E8002D", "🏆", 30),
            new Platform("gcn", "GameCube", "GCN", "#6A0DAD", "🟣", 40),
            new Platform("wii", "Wii", "Wii", "#C0C0C0", "🎯", 50),
            new Platform("gb", "Game Boy", "GB", "#7B8C4F", "👾", 60),
            new Platform("gbc", "Game Boy Color", "GBC", "#009AC7", "🌈", 70),
            new Platform("gba", "Game Boy Advance", "GBA", "#7B2D8B", "💜", 80),
            new Platform("nds", "Nintendo DS", "NDS", "#CC0000", "📱", 90),
            new Platform("ps1", "PlayStation", "PS1", "#003791", "🎯", 100),
            new Platform("ps2", "PlayStation 2", "PS2", "#00439B", "⚔️", 110),
            new Platform("ps3", "PlayStation 3", "PS3", "#00439B", "🖤", 120),
            new Platform("psp", "PlayStation Portable", "PSP", "#003791", "🎮", 130),
            new Platform("xbox", "Xbox", "Xbox", "#107C10", "🟢", 140),
            new Platform("xbox360", "Xbox 360", "X360", "#52B043", "💚", 150),
            new Platform("sega_genesis", "Sega Genesis / Mega Drive", "Genesis", "#1A52A5", "⚡", 160),
            new Platform("saturn", "Sega Saturn", "Saturn", "#2966B8", "🪐", 170),
            new Platform("dreamcast", "Sega Dreamcast", "DC", "#E86100", "🌊", 180),
            new Platform("arcade_mame", "Arcade / MAME", "MAME", "#FFD700", "🕹️", 190),
            new Platform("pico8", "PICO-8", "PICO", "#FF004D", "🩷", 200),
            new Platform("gamehub", "GameHub (Android)", "Hub", "#3DDC84", "🤖", 210),
            new Platform("winlator", "Winlator (Windows)", "Win", "#0078D4", "🪟", 220)
        );
    }
}
