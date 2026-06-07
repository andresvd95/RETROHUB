package com.retrohub.launcher.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.GameRepository;

public class MainViewModel extends AndroidViewModel {
    public final GameRepository games;
    public final EmulatorRepository emulators;

    public MainViewModel(@NonNull Application app) {
        super(app);
        this.games = new GameRepository(app);
        this.emulators = new EmulatorRepository(app);
    }

    public LiveData<Integer> gameCount() { return games.observeCount(); }
    public LiveData<Integer> emulatorCount() { return emulators.observeCount(); }
}
