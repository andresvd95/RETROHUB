package com.retrohub.launcher.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    public static final String FILTER_ALL = "__all__";
    public static final String FILTER_FAVORITES = "__favorites__";
    public static final String FILTER_RECENT = "__recent__";

    public final GameRepository games;
    public final PlatformRepository platforms;

    private final MutableLiveData<String> currentFilter = new MutableLiveData<>(FILTER_ALL);
    private final MediatorLiveData<List<GameEntry>> filteredGames = new MediatorLiveData<>();

    private LiveData<List<GameEntry>> activeSource;

    public HomeViewModel(@NonNull Application app) {
        super(app);
        this.games = new GameRepository(app);
        this.platforms = new PlatformRepository(app);
        Transformations.distinctUntilChanged(currentFilter).observeForever(this::rebind);
    }

    private void rebind(String filter) {
        if (activeSource != null) filteredGames.removeSource(activeSource);
        if (FILTER_ALL.equals(filter)) {
            activeSource = games.observeAll();
        } else if (FILTER_FAVORITES.equals(filter)) {
            activeSource = games.observeFavorites();
        } else if (FILTER_RECENT.equals(filter)) {
            activeSource = games.observeRecent(30);
        } else {
            activeSource = games.observeByPlatform(filter);
        }
        filteredGames.addSource(activeSource, filteredGames::setValue);
    }

    public void setFilter(String filter) { currentFilter.setValue(filter); }
    public LiveData<String> filter() { return currentFilter; }
    public LiveData<List<GameEntry>> filteredGames() { return filteredGames; }

    public LiveData<List<GameEntry>> recents() { return games.observeRecent(10); }
    public LiveData<List<GameEntry>> recommended() { return games.observeRecommended(10); }
    public LiveData<GameEntry> mostRecent() { return games.observeMostRecent(); }
    public LiveData<List<Platform>> allPlatforms() { return platforms.observeAll(); }
    public LiveData<List<GameEntry>> allGames() { return games.observeAll(); }
}
