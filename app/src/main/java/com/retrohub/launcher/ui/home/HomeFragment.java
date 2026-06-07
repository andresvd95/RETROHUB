package com.retrohub.launcher.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retrohub.launcher.R;
import com.retrohub.launcher.adapter.GameCardAdapter;
import com.retrohub.launcher.adapter.PlatformCompactAdapter;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.ui.detail.GameDetailBottomSheet;
import com.retrohub.launcher.ui.main.MainActivity;
import com.retrohub.launcher.ui.search.SearchActivity;
import com.retrohub.launcher.util.ColorUtils;
import com.retrohub.launcher.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel vm;

    private TextView statsLabel;
    private LinearLayout tabsContainer;
    private HorizontalScrollView tabsScroll;

    private View featuredCard;
    private TextView featuredEmoji;
    private TextView featuredTitle;
    private TextView featuredSubtitle;
    private TextView featuredPlatformChip;
    private View featuredEmpty;

    private RecyclerView recentsRv;
    private RecyclerView platformsRv;
    private RecyclerView recommendedRv;
    private RecyclerView filteredRv;
    private TextView filteredEmpty;

    private GameCardAdapter recentsAdapter;
    private GameCardAdapter recommendedAdapter;
    private PlatformCompactAdapter platformsAdapter;
    private GameCardAdapter filteredAdapter;

    private Map<String, Platform> platformIndex = new HashMap<>();
    private List<Platform> allPlatforms = Collections.emptyList();
    private List<GameEntry> allGames = Collections.emptyList();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(HomeViewModel.class);

        statsLabel = v.findViewById(R.id.home_stats);
        tabsContainer = v.findViewById(R.id.home_tabs_container);
        tabsScroll = v.findViewById(R.id.home_tabs_scroll);

        featuredCard = v.findViewById(R.id.featured_card);
        featuredEmoji = v.findViewById(R.id.featured_emoji);
        featuredTitle = v.findViewById(R.id.featured_title);
        featuredSubtitle = v.findViewById(R.id.featured_subtitle);
        featuredPlatformChip = v.findViewById(R.id.featured_chip);
        featuredEmpty = v.findViewById(R.id.featured_empty);

        recentsRv = v.findViewById(R.id.recents_rv);
        platformsRv = v.findViewById(R.id.home_platforms_rv);
        recommendedRv = v.findViewById(R.id.recommended_rv);
        filteredRv = v.findViewById(R.id.home_filtered_rv);
        filteredEmpty = v.findViewById(R.id.home_filtered_empty);

        v.findViewById(R.id.home_search).setOnClickListener(view ->
            startActivity(new Intent(requireContext(), SearchActivity.class)));

        recentsAdapter = new GameCardAdapter(platformIndex, this::openDetail);
        recommendedAdapter = new GameCardAdapter(platformIndex, this::openDetail);
        filteredAdapter = new GameCardAdapter(platformIndex, this::openDetail);
        platformsAdapter = new PlatformCompactAdapter(p ->
            ((MainActivity) requireActivity()).openLibraryForPlatform(p.id));

        recentsRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recentsRv.setAdapter(recentsAdapter);

        recommendedRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedRv.setAdapter(recommendedAdapter);

        platformsRv.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        platformsRv.setAdapter(platformsAdapter);

        filteredRv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        filteredRv.setAdapter(filteredAdapter);

        observe();
    }

    private void observe() {
        vm.allPlatforms().observe(getViewLifecycleOwner(), platforms -> {
            allPlatforms = platforms == null ? Collections.emptyList() : platforms;
            platformIndex.clear();
            for (Platform p : allPlatforms) platformIndex.put(p.id, p);
            recentsAdapter.notifyDataSetChanged();
            recommendedAdapter.notifyDataSetChanged();
            filteredAdapter.notifyDataSetChanged();
            rebuildPlatformGrid();
            rebuildTabs();
        });

        vm.allGames().observe(getViewLifecycleOwner(), games -> {
            allGames = games == null ? Collections.emptyList() : games;
            int count = allGames.size();
            int platformCount = countPlatformsWithGames();
            statsLabel.setText(count + " games · " + platformCount + " platforms");
            rebuildTabs();
        });

        vm.recents().observe(getViewLifecycleOwner(), list -> recentsAdapter.submit(list));
        vm.recommended().observe(getViewLifecycleOwner(), list -> recommendedAdapter.submit(list));
        vm.mostRecent().observe(getViewLifecycleOwner(), this::bindFeatured);
        vm.filteredGames().observe(getViewLifecycleOwner(), list -> {
            filteredAdapter.submit(list);
            boolean empty = list == null || list.isEmpty();
            filteredEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            filteredRv.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private int countPlatformsWithGames() {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (GameEntry g : allGames) seen.add(g.platformId);
        return seen.size();
    }

    private void bindFeatured(GameEntry game) {
        if (game == null) {
            featuredCard.setVisibility(View.GONE);
            featuredEmpty.setVisibility(View.VISIBLE);
            return;
        }
        featuredEmpty.setVisibility(View.GONE);
        featuredCard.setVisibility(View.VISIBLE);
        Platform p = platformIndex.get(game.platformId);
        int color = p == null ? 0xFF22223A : ColorUtils.parseHex(p.colorHex, 0xFF22223A);
        featuredCard.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        featuredEmoji.setText(p == null ? "🎮" : p.emojiIcon);
        featuredTitle.setText(game.title);
        featuredSubtitle.setText(getString(R.string.featured_subtitle,
            TimeUtils.formatPlayTimeShort(game.playTimeSeconds)));
        featuredPlatformChip.setText(p == null ? game.platformId : p.shortName);
        featuredCard.setOnClickListener(v -> openDetail(game));
    }

    private void rebuildPlatformGrid() {
        // Only platforms that have at least one game (or all if none yet — still show first 8 anyway).
        java.util.Map<String, Integer> counts = new HashMap<>();
        for (GameEntry g : allGames) {
            Integer c = counts.get(g.platformId);
            counts.put(g.platformId, c == null ? 1 : c + 1);
        }
        List<Platform> shown = new ArrayList<>();
        for (Platform p : allPlatforms) {
            if (counts.containsKey(p.id)) shown.add(p);
        }
        if (shown.isEmpty()) {
            // Cold start: still show the first 8 platforms so the UI isn't empty.
            for (int i = 0; i < Math.min(8, allPlatforms.size()); i++) shown.add(allPlatforms.get(i));
        }
        platformsAdapter.submit(shown);
    }

    private void rebuildTabs() {
        if (tabsContainer == null) return;
        tabsContainer.removeAllViews();
        Map<String, Integer> counts = new HashMap<>();
        for (GameEntry g : allGames) {
            Integer c = counts.get(g.platformId);
            counts.put(g.platformId, c == null ? 1 : c + 1);
        }
        int favoritesCount = 0;
        for (GameEntry g : allGames) if (g.isFavorite) favoritesCount++;
        int recentsCount = 0;
        for (GameEntry g : allGames) if (g.lastPlayedAt > 0) recentsCount++;

        String currentFilter = vm.filter().getValue();
        addTab(HomeViewModel.FILTER_ALL, getString(R.string.filter_all), allGames.size(), currentFilter);
        addTab(HomeViewModel.FILTER_FAVORITES, getString(R.string.filter_favorites), favoritesCount, currentFilter);
        addTab(HomeViewModel.FILTER_RECENT, getString(R.string.filter_recent), recentsCount, currentFilter);
        for (Platform p : allPlatforms) {
            Integer c = counts.get(p.id);
            if (c == null || c == 0) continue;
            addTab(p.id, p.shortName, c, currentFilter);
        }
    }

    private void addTab(String id, String label, int count, String currentFilter) {
        TextView tab = new TextView(requireContext());
        boolean active = id.equals(currentFilter);
        String text = count > 0 && !id.equals(HomeViewModel.FILTER_ALL) ? label + " (" + count + ")" : label;
        tab.setText(text);
        tab.setTextSize(13f);
        tab.setTextColor(active ? 0xFFFFFFFF : 0x66FFFFFF);
        tab.setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        int padH = (int) (16 * getResources().getDisplayMetrics().density);
        int padV = (int) (10 * getResources().getDisplayMetrics().density);
        tab.setPadding(padH, padV, padH, padV);
        if (active) {
            android.graphics.drawable.GradientDrawable underline = new android.graphics.drawable.GradientDrawable();
            underline.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            underline.setColor(0xFFE8002D);
            tab.setBackground(makeUnderline());
        }
        tab.setOnClickListener(v -> {
            vm.setFilter(id);
            rebuildTabs();
        });
        tabsContainer.addView(tab);
    }

    private android.graphics.drawable.Drawable makeUnderline() {
        int lineH = (int) (2 * getResources().getDisplayMetrics().density);
        android.graphics.drawable.GradientDrawable line = new android.graphics.drawable.GradientDrawable();
        line.setColor(0xFFE8002D);
        line.setSize(0, lineH);
        android.graphics.drawable.LayerDrawable layer = new android.graphics.drawable.LayerDrawable(
            new android.graphics.drawable.Drawable[]{ line });
        layer.setLayerGravity(0, android.view.Gravity.BOTTOM);
        layer.setLayerHeight(0, lineH);
        return layer;
    }

    private void openDetail(GameEntry game) {
        GameDetailBottomSheet sheet = GameDetailBottomSheet.newInstance(game.id);
        sheet.show(getParentFragmentManager(), "game_detail");
    }
}
