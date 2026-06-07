package com.retrohub.launcher.ui.library;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.retrohub.launcher.R;
import com.retrohub.launcher.adapter.GameGridAdapter;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.ui.detail.GameDetailBottomSheet;
import com.retrohub.launcher.ui.game.AddGameActivity;
import com.retrohub.launcher.ui.game.EditGameActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryFragment extends Fragment {

    public static final String ARG_PLATFORM = "platform_id";

    private LibraryViewModel vm;
    private RecyclerView rv;
    private TextView empty;
    private LinearLayout chips;
    private GameGridAdapter adapter;
    private final Map<String, Platform> platformIndex = new HashMap<>();

    public static LibraryFragment newInstance(String platformId) {
        LibraryFragment f = new LibraryFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PLATFORM, platformId);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(LibraryViewModel.class);
        rv = v.findViewById(R.id.library_rv);
        empty = v.findViewById(R.id.library_empty);
        chips = v.findViewById(R.id.platform_chips);
        SearchView search = v.findViewById(R.id.library_search);
        FloatingActionButton fab = v.findViewById(R.id.add_game_fab);
        View sortBtn = v.findViewById(R.id.sort_btn);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new GameGridAdapter(platformIndex, new GameGridAdapter.OnGameAction() {
            @Override public void onClick(GameEntry game) { openDetail(game); }
            @Override public void onEdit(GameEntry game) {
                Intent i = new Intent(requireContext(), EditGameActivity.class);
                i.putExtra(EditGameActivity.EXTRA_GAME_ID, game.id);
                startActivity(i);
            }
            @Override public void onDelete(GameEntry game) { confirmDelete(game); }
            @Override public void onToggleFavorite(GameEntry game) {
                vm.games.setFavorite(game.id, !game.isFavorite);
            }
        });
        rv.setAdapter(adapter);

        search.setQueryHint(getString(R.string.search_hint));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { vm.setQuery(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { vm.setQuery(newText); return true; }
        });

        fab.setOnClickListener(view -> startActivity(new Intent(requireContext(), AddGameActivity.class)));
        sortBtn.setOnClickListener(view -> showSortMenu(view));

        if (getArguments() != null) {
            String pid = getArguments().getString(ARG_PLATFORM);
            if (pid != null) vm.setPlatformFilter(pid);
        }

        vm.platforms().observe(getViewLifecycleOwner(), list -> {
            platformIndex.clear();
            if (list != null) for (Platform p : list) platformIndex.put(p.id, p);
            adapter.notifyDataSetChanged();
            buildChips(list == null ? Collections.emptyList() : list);
        });

        vm.displayed().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            boolean isEmpty = list == null || list.isEmpty();
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void buildChips(List<Platform> platforms) {
        chips.removeAllViews();
        addChip(null, getString(R.string.filter_all));
        for (Platform p : platforms) addChip(p.id, p.shortName);
    }

    private void addChip(String pid, String label) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setTextSize(12f);
        chip.setTextColor(0xFFFFFFFF);
        int padH = (int) (12 * getResources().getDisplayMetrics().density);
        int padV = (int) (6 * getResources().getDisplayMetrics().density);
        chip.setPadding(padH, padV, padH, padV);
        chip.setBackgroundResource(R.drawable.bg_filter_chip);
        boolean selected = (pid == null && vm.getPlatformFilter() == null)
            || (pid != null && pid.equals(vm.getPlatformFilter()));
        chip.setSelected(selected);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            vm.setPlatformFilter(pid);
            for (int i = 0; i < chips.getChildCount(); i++) chips.getChildAt(i).setSelected(false);
            chip.setSelected(true);
        });
        chips.addView(chip);
    }

    private void showSortMenu(View anchor) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 1, 0, R.string.sort_name);
        menu.getMenu().add(0, 2, 1, R.string.sort_most_played);
        menu.getMenu().add(0, 3, 2, R.string.sort_recent);
        menu.getMenu().add(0, 4, 3, R.string.sort_by_platform);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: vm.setSort(LibraryViewModel.Sort.NAME); return true;
                case 2: vm.setSort(LibraryViewModel.Sort.MOST_PLAYED); return true;
                case 3: vm.setSort(LibraryViewModel.Sort.RECENT); return true;
                case 4: vm.setSort(LibraryViewModel.Sort.BY_PLATFORM); return true;
            }
            return false;
        });
        menu.show();
    }

    private void confirmDelete(GameEntry g) {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_msg)
            .setPositiveButton(R.string.delete, (d, w) -> vm.games.delete(g))
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void openDetail(GameEntry g) {
        GameDetailBottomSheet.newInstance(g.id).show(getParentFragmentManager(), "detail");
    }
}
