package com.retrohub.launcher.ui.emulators;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;

import java.util.List;

public class EmulatorViewModel extends AndroidViewModel {
    public final EmulatorRepository repo;
    public final PlatformRepository platforms;

    public EmulatorViewModel(@NonNull Application app) {
        super(app);
        this.repo = new EmulatorRepository(app);
        this.platforms = new PlatformRepository(app);
    }

    public LiveData<List<Emulator>> emulators() { return repo.observeAll(); }
    public LiveData<List<Platform>> platforms() { return platforms.observeAll(); }
}
