package com.retrohub.launcher.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryViewModel extends AndroidViewModel {

    public enum Sort { NAME, MOST_PLAYED, RECENT, BY_PLATFORM }

    public final GameRepository games;
    public final PlatformRepository platforms;

    private final MutableLiveData<String> platformFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> textQuery = new MutableLiveData<>("");
    private final MutableLiveData<Sort> sort = new MutableLiveData<>(Sort.NAME);

    private final MediatorLiveData<List<GameEntry>> displayed = new MediatorLiveData<>();
    private List<GameEntry> latestRaw = Collections.emptyList();

    public LibraryViewModel(@NonNull Application app) {
        super(app);
        this.games = new GameRepository(app);
        this.platforms = new PlatformRepository(app);
        displayed.addSource(games.observeAll(), list -> {
            latestRaw = list == null ? Collections.emptyList() : list;
            recompute();
        });
        displayed.addSource(platformFilter, f -> recompute());
        displayed.addSource(textQuery, q -> recompute());
        displayed.addSource(sort, s -> recompute());
    }

    private void recompute() {
        String pf = platformFilter.getValue();
        String q = textQuery.getValue() == null ? "" : textQuery.getValue().trim().toLowerCase();
        Sort s = sort.getValue() == null ? Sort.NAME : sort.getValue();
        List<GameEntry> filtered = new ArrayList<>();
        for (GameEntry g : latestRaw) {
            if (pf != null && !pf.equals(g.platformId)) continue;
            if (!q.isEmpty() && (g.title == null || !g.title.toLowerCase().contains(q))) continue;
            filtered.add(g);
        }
        Comparator<GameEntry> cmp;
        switch (s) {
            case MOST_PLAYED:
                cmp = (a, b) -> Long.compare(b.playTimeSeconds, a.playTimeSeconds); break;
            case RECENT:
                cmp = (a, b) -> Long.compare(b.lastPlayedAt, a.lastPlayedAt); break;
            case BY_PLATFORM:
                cmp = (a, b) -> {
                    int c = String.valueOf(a.platformId).compareTo(String.valueOf(b.platformId));
                    if (c != 0) return c;
                    return String.valueOf(a.title).compareToIgnoreCase(String.valueOf(b.title));
                };
                break;
            case NAME:
            default:
                cmp = (a, b) -> String.valueOf(a.title).compareToIgnoreCase(String.valueOf(b.title));
        }
        Collections.sort(filtered, cmp);
        displayed.setValue(filtered);
    }

    public LiveData<List<GameEntry>> displayed() { return displayed; }
    public LiveData<List<Platform>> platforms() { return platforms.observeAll(); }
    public void setPlatformFilter(String id) { platformFilter.setValue(id); }
    public String getPlatformFilter() { return platformFilter.getValue(); }
    public void setQuery(String q) { textQuery.setValue(q); }
    public void setSort(Sort s) { sort.setValue(s); }

    public Map<String, Platform> indexPlatforms(List<Platform> list) {
        Map<String, Platform> map = new HashMap<>();
        if (list != null) for (Platform p : list) map.put(p.id, p);
        return map;
    }
}
