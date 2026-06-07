package com.retrohub.launcher.ui.platforms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retrohub.launcher.R;
import com.retrohub.launcher.adapter.PlatformCardAdapter;
import com.retrohub.launcher.ui.main.MainActivity;

public class PlatformFragment extends Fragment {

    private PlatformViewModel vm;
    private PlatformCardAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_platforms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(PlatformViewModel.class);
        TextView subtitle = v.findViewById(R.id.platforms_subtitle);
        RecyclerView rv = v.findViewById(R.id.platforms_rv);

        adapter = new PlatformCardAdapter(p ->
            ((MainActivity) requireActivity()).openLibraryForPlatform(p.id));
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rv.setAdapter(adapter);

        vm.rows().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            int withGames = 0;
            if (list != null) for (PlatformCardAdapter.Row r : list) if (r.gameCount > 0) withGames++;
            subtitle.setText(withGames + " platforms with games");
        });
    }
}
