package com.retrohub.launcher.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.db.PlatformDao;
import com.retrohub.launcher.data.model.Platform;

import java.util.List;

public class PlatformRepository {
    private final PlatformDao dao;

    public PlatformRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).platformDao();
    }

    public LiveData<List<Platform>> observeAll() { return dao.observeAll(); }
    public LiveData<Platform> observeById(String id) { return dao.observeById(id); }

    public List<Platform> getAllSync() { return dao.getAllSync(); }
    public Platform getById(String id) { return dao.getById(id); }
}
