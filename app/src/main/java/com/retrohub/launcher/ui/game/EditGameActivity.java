package com.retrohub.launcher.ui.game;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.model.GameEntry;

import java.io.File;

public class EditGameActivity extends AddGameActivity {

    public static final String EXTRA_GAME_ID = "game_id";

    private int gameId;
    private GameEntry editing;
    private boolean platformPrefilled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameId = getIntent().getIntExtra(EXTRA_GAME_ID, -1);
        if (gameId <= 0) { finish(); return; }
        AppDatabase.io.execute(() -> {
            editing = gameRepo.getById(gameId);
            if (editing == null) return;
            runOnUiThread(() -> {
                inputTitle.setText(editing.title);
                if (editing.romPath != null) inputRom.setText(editing.romPath);
                if (editing.customArgs != null) inputArgs.setText(editing.customArgs);
                selectedCoverPath = editing.coverUri;
                if (editing.coverUri != null && !editing.coverUri.isEmpty()) {
                    Glide.with(coverPreview).load(new File(editing.coverUri)).into(coverPreview);
                }
                selectGenre(editing.genre);
                tryPrefillPlatform();
            });
        });
    }

    @Override
    protected void afterPlatformsLoaded() {
        tryPrefillPlatform();
    }

    private void tryPrefillPlatform() {
        if (platformPrefilled || editing == null || platforms.isEmpty()) return;
        platformPrefilled = true;
        selectPlatformById(editing.platformId);
        loadEmulatorsForPlatform(editing.platformId, editing.emulatorId);
    }

    @Override
    protected GameEntry currentGame() {
        if (editing == null) editing = new GameEntry();
        return editing;
    }

    @Override
    protected void persistAndFinish(GameEntry g) {
        gameRepo.update(g);
        finish();
    }
}
