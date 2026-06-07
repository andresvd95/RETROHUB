package com.retrohub.launcher.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.db.GameEntryDao;
import com.retrohub.launcher.data.model.GameEntry;

import java.util.List;

public class GameRepository {
    private final GameEntryDao dao;

    public GameRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).gameEntryDao();
    }

    public LiveData<List<GameEntry>> observeAll() { return dao.observeAll(); }
    public LiveData<List<GameEntry>> observeByPlatform(String platformId) { return dao.observeByPlatform(platformId); }
    public LiveData<List<GameEntry>> observeFavorites() { return dao.observeFavorites(); }
    public LiveData<List<GameEntry>> observeRecent(int limit) { return dao.observeRecent(limit); }
    public LiveData<GameEntry> observeMostRecent() { return dao.observeMostRecent(); }
    public LiveData<List<GameEntry>> observeRecommended(int limit) { return dao.observeRecommended(limit); }
    public LiveData<List<GameEntry>> search(String query) { return dao.search(query); }
    public LiveData<GameEntry> observeById(int id) { return dao.observeById(id); }
    public LiveData<Integer> observeCount() { return dao.observeCount(); }

    public void insert(GameEntry g, java.util.function.LongConsumer onDone) {
        AppDatabase.io.execute(() -> {
            if (g.addedAt == 0) g.addedAt = System.currentTimeMillis();
            long id = dao.insert(g);
            if (onDone != null) onDone.accept(id);
        });
    }

    public void update(GameEntry g) {
        AppDatabase.io.execute(() -> dao.update(g));
    }

    public void delete(GameEntry g) {
        AppDatabase.io.execute(() -> dao.delete(g));
    }

    public void setFavorite(int gameId, boolean favorite) {
        AppDatabase.io.execute(() -> dao.setFavorite(gameId, favorite));
    }

    public void markPlayedNow(int gameId) {
        AppDatabase.io.execute(() -> dao.markPlayed(gameId, System.currentTimeMillis()));
    }

    public void addPlayTime(int gameId, long seconds) {
        AppDatabase.io.execute(() -> dao.addPlayTime(gameId, seconds, System.currentTimeMillis()));
    }

    public GameEntry getById(int id) { return dao.getById(id); }
    public int countByPlatformSync(String platformId) { return dao.countByPlatformSync(platformId); }
    public List<GameEntry> getAllSync() { return dao.getAllSync(); }
    public List<GameEntry> getByEmulatorSync(int emulatorId) { return dao.getByEmulatorSync(emulatorId); }
}
