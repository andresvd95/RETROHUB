package com.retrohub.launcher.ui.platforms;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.retrohub.launcher.adapter.PlatformCardAdapter;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformViewModel extends AndroidViewModel {

    public final PlatformRepository platforms;
    public final GameRepository games;

    private final MediatorLiveData<List<PlatformCardAdapter.Row>> rows = new MediatorLiveData<>();
    private List<Platform> latestPlatforms = Collections.emptyList();
    private List<GameEntry> latestGames = Collections.emptyList();

    public PlatformViewModel(@NonNull Application app) {
        super(app);
        this.platforms = new PlatformRepository(app);
        this.games = new GameRepository(app);
        rows.addSource(platforms.observeAll(), list -> {
            latestPlatforms = list == null ? Collections.emptyList() : list;
            recompute();
        });
        rows.addSource(games.observeAll(), list -> {
            latestGames = list == null ? Collections.emptyList() : list;
            recompute();
        });
    }

    private void recompute() {
        Map<String, Integer> counts = new HashMap<>();
        for (GameEntry g : latestGames) {
            Integer c = counts.get(g.platformId);
            counts.put(g.platformId, c == null ? 1 : c + 1);
        }
        List<PlatformCardAdapter.Row> withGames = new ArrayList<>();
        List<PlatformCardAdapter.Row> empty = new ArrayList<>();
        for (Platform p : latestPlatforms) {
            int c = counts.getOrDefault(p.id, 0);
            (c > 0 ? withGames : empty).add(new PlatformCardAdapter.Row(p, c));
        }
        Collections.sort(withGames, (a, b) -> Integer.compare(b.gameCount, a.gameCount));
        withGames.addAll(empty);
        rows.setValue(withGames);
    }

    public LiveData<List<PlatformCardAdapter.Row>> rows() { return rows; }
}
