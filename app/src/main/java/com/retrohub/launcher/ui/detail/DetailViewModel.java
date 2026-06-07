package com.retrohub.launcher.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;

public class DetailViewModel extends AndroidViewModel {
    public final GameRepository games;
    public final PlatformRepository platforms;
    public final EmulatorRepository emulators;

    private final MutableLiveData<Integer> currentId = new MutableLiveData<>();

    public DetailViewModel(@NonNull Application app) {
        super(app);
        games = new GameRepository(app);
        platforms = new PlatformRepository(app);
        emulators = new EmulatorRepository(app);
    }

    public void setGameId(int id) { currentId.setValue(id); }

    public LiveData<GameEntry> game() {
        return Transformations.switchMap(currentId, id -> id == null ? null : games.observeById(id));
    }

    public Platform platformFor(GameEntry g) {
        if (g == null) return null;
        return platforms.getById(g.platformId);
    }

    public Emulator emulatorFor(GameEntry g) {
        if (g == null) return null;
        return emulators.getById(g.emulatorId);
    }

    public void runOnIo(Runnable r) { AppDatabase.io.execute(r); }
}
