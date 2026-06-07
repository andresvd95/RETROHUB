package com.retrohub.launcher.data.repository;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.lifecycle.LiveData;

import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.db.EmulatorDao;
import com.retrohub.launcher.data.model.Emulator;

import java.util.List;

public class EmulatorRepository {
    private final EmulatorDao dao;
    private final PackageManager pm;

    public EmulatorRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).emulatorDao();
        this.pm = context.getApplicationContext().getPackageManager();
    }

    public LiveData<List<Emulator>> observeAll() { return dao.observeAll(); }
    public LiveData<List<Emulator>> observeByPlatform(String platformId) { return dao.observeByPlatform(platformId); }
    public LiveData<Emulator> observeById(int id) { return dao.observeById(id); }
    public LiveData<Integer> observeCount() { return dao.observeCount(); }

    public void insert(Emulator e, Runnable onDone) {
        AppDatabase.io.execute(() -> {
            e.isInstalled = isInstalled(e.packageName);
            if (e.addedAt == 0) e.addedAt = System.currentTimeMillis();
            dao.insert(e);
            if (onDone != null) onDone.run();
        });
    }

    public void update(Emulator e) {
        AppDatabase.io.execute(() -> {
            e.isInstalled = isInstalled(e.packageName);
            dao.update(e);
        });
    }

    public void delete(Emulator e) {
        AppDatabase.io.execute(() -> dao.delete(e));
    }

    public List<Emulator> getByPlatformSync(String platformId) {
        return dao.getByPlatformSync(platformId);
    }

    public Emulator getById(int id) { return dao.getById(id); }
    public List<Emulator> getAllSync() { return dao.getAllSync(); }

    public boolean isInstalled(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void refreshInstalledFlags() {
        AppDatabase.io.execute(() -> {
            for (Emulator e : dao.getAllSync()) {
                boolean installed = isInstalled(e.packageName);
                if (installed != e.isInstalled) {
                    e.isInstalled = installed;
                    dao.update(e);
                }
            }
        });
    }
}
