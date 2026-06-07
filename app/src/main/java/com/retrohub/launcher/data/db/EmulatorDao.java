package com.retrohub.launcher.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.retrohub.launcher.data.model.Emulator;

import java.util.List;

@Dao
public interface EmulatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Emulator emulator);

    @Update
    void update(Emulator emulator);

    @Delete
    void delete(Emulator emulator);

    @Query("SELECT * FROM emulators ORDER BY name ASC")
    LiveData<List<Emulator>> observeAll();

    @Query("SELECT * FROM emulators ORDER BY name ASC")
    List<Emulator> getAllSync();

    @Query("SELECT * FROM emulators WHERE platformId = :platformId ORDER BY name ASC")
    LiveData<List<Emulator>> observeByPlatform(String platformId);

    @Query("SELECT * FROM emulators WHERE platformId = :platformId ORDER BY name ASC")
    List<Emulator> getByPlatformSync(String platformId);

    @Query("SELECT * FROM emulators WHERE id = :id LIMIT 1")
    Emulator getById(int id);

    @Query("SELECT * FROM emulators WHERE id = :id LIMIT 1")
    LiveData<Emulator> observeById(int id);

    @Query("SELECT COUNT(*) FROM emulators")
    LiveData<Integer> observeCount();
}
