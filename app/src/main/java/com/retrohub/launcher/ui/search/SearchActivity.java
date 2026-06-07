package com.retrohub.launcher.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retrohub.launcher.R;
import com.retrohub.launcher.adapter.GameGridAdapter;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;
import com.retrohub.launcher.ui.detail.GameDetailBottomSheet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private GameRepository gameRepo;
    private PlatformRepository platformRepo;

    private final Handler debounce = new Handler(Looper.getMainLooper());
    private Runnable pending;

    private RecyclerView rv;
    private TextView empty;
    private GameGridAdapter adapter;
    private final Map<String, Platform> platformIndex = new HashMap<>();

    private LiveData<List<GameEntry>> currentSource;
    private final Observer<List<GameEntry>> observer = new Observer<List<GameEntry>>() {
        @Override
        public void onChanged(List<GameEntry> list) {
            adapter.submit(list == null ? Collections.emptyList() : list);
            boolean isEmpty = list == null || list.isEmpty();
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        gameRepo = new GameRepository(this);
        platformRepo = new PlatformRepository(this);

        SearchView search = findViewById(R.id.search_view);
        rv = findViewById(R.id.search_rv);
        empty = findViewById(R.id.search_empty);
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        adapter = new GameGridAdapter(platformIndex, new GameGridAdapter.OnGameAction() {
            @Override public void onClick(GameEntry game) {
                GameDetailBottomSheet.newInstance(game.id).show(getSupportFragmentManager(), "detail");
            }
            @Override public void onEdit(GameEntry game) {}
            @Override public void onDelete(GameEntry game) {}
            @Override public void onToggleFavorite(GameEntry game) {
                gameRepo.setFavorite(game.id, !game.isFavorite);
            }
        });
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(adapter);

        platformRepo.observeAll().observe(this, list -> {
            platformIndex.clear();
            if (list != null) for (Platform p : list) platformIndex.put(p.id, p);
            adapter.notifyDataSetChanged();
        });

        search.setIconifiedByDefault(false);
        search.requestFocus();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { runQuery(query); return true; }
            @Override public boolean onQueryTextChange(String newText) {
                if (pending != null) debounce.removeCallbacks(pending);
                pending = () -> runQuery(newText);
                debounce.postDelayed(pending, 300);
                return true;
            }
        });

        runQuery("");
    }

    private void runQuery(String q) {
        if (currentSource != null) currentSource.removeObserver(observer);
        currentSource = (q == null || q.isEmpty()) ? gameRepo.observeAll() : gameRepo.search(q);
        currentSource.observe((LifecycleOwner) this, observer);
    }
}
