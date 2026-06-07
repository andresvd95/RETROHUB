package com.retrohub.launcher.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.retrohub.launcher.data.model.GameEntry;

import java.util.List;

@Dao
public interface GameEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GameEntry game);

    @Update
    void update(GameEntry game);

    @Delete
    void delete(GameEntry game);

    @Query("SELECT * FROM game_entries ORDER BY title ASC")
    LiveData<List<GameEntry>> observeAll();

    @Query("SELECT * FROM game_entries ORDER BY title ASC")
    List<GameEntry> getAllSync();

    @Query("SELECT * FROM game_entries WHERE id = :id LIMIT 1")
    GameEntry getById(int id);

    @Query("SELECT * FROM game_entries WHERE id = :id LIMIT 1")
    LiveData<GameEntry> observeById(int id);

    @Query("SELECT * FROM game_entries WHERE platformId = :platformId ORDER BY title ASC")
    LiveData<List<GameEntry>> observeByPlatform(String platformId);

    @Query("SELECT * FROM game_entries WHERE isFavorite = 1 ORDER BY title ASC")
    LiveData<List<GameEntry>> observeFavorites();

    @Query("SELECT * FROM game_entries WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT :limit")
    LiveData<List<GameEntry>> observeRecent(int limit);

    @Query("SELECT * FROM game_entries WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT 1")
    LiveData<GameEntry> observeMostRecent();

    @Query("SELECT * FROM game_entries WHERE (lastPlayedAt = 0 OR playTimeSeconds < 600) ORDER BY addedAt DESC LIMIT :limit")
    LiveData<List<GameEntry>> observeRecommended(int limit);

    @Query("SELECT * FROM game_entries WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    LiveData<List<GameEntry>> search(String query);

    @Query("SELECT * FROM game_entries WHERE emulatorId = :emulatorId")
    List<GameEntry> getByEmulatorSync(int emulatorId);

    @Query("UPDATE game_entries SET playTimeSeconds = playTimeSeconds + :seconds, lastPlayedAt = :now WHERE id = :gameId")
    void addPlayTime(int gameId, long seconds, long now);

    @Query("UPDATE game_entries SET lastPlayedAt = :now WHERE id = :gameId")
    void markPlayed(int gameId, long now);

    @Query("UPDATE game_entries SET isFavorite = :favorite WHERE id = :gameId")
    void setFavorite(int gameId, boolean favorite);

    @Query("SELECT COUNT(*) FROM game_entries")
    LiveData<Integer> observeCount();

    @Query("SELECT COUNT(*) FROM game_entries WHERE platformId = :platformId")
    int countByPlatformSync(String platformId);
}
