package com.retrohub.launcher.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.retrohub.launcher.data.model.Platform;

import java.util.List;

@Dao
public interface PlatformDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Platform> platforms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Platform platform);

    @Update
    void update(Platform platform);

    @Query("SELECT * FROM platforms ORDER BY sortOrder ASC, name ASC")
    LiveData<List<Platform>> observeAll();

    @Query("SELECT * FROM platforms ORDER BY sortOrder ASC, name ASC")
    List<Platform> getAllSync();

    @Query("SELECT * FROM platforms WHERE id = :id LIMIT 1")
    Platform getById(String id);

    @Query("SELECT * FROM platforms WHERE id = :id LIMIT 1")
    LiveData<Platform> observeById(String id);
}
