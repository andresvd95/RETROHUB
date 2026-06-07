package com.retrohub.launcher.ui.emulators;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.retrohub.launcher.R;
import com.retrohub.launcher.adapter.EmulatorListAdapter;
import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.Platform;

import java.util.HashMap;
import java.util.Map;

public class EmulatorFragment extends Fragment {

    private EmulatorViewModel vm;
    private EmulatorListAdapter adapter;
    private View empty;
    private final Map<String, Platform> platformIndex = new HashMap<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emulators, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(EmulatorViewModel.class);
        RecyclerView rv = v.findViewById(R.id.emulators_rv);
        empty = v.findViewById(R.id.emulators_empty);
        FloatingActionButton fab = v.findViewById(R.id.add_emulator_fab);

        adapter = new EmulatorListAdapter(platformIndex, new EmulatorListAdapter.OnEmulatorAction() {
            @Override public void onClick(Emulator e) {
                Intent i = new Intent(requireContext(), AddEmulatorActivity.class);
                i.putExtra(AddEmulatorActivity.EXTRA_EMULATOR_ID, e.id);
                startActivity(i);
            }
            @Override public void onToggleEnabled(Emulator e, boolean enabled) {
                e.enabled = enabled;
                vm.repo.update(e);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDelete());
        helper.attachToRecyclerView(rv);

        fab.setOnClickListener(view -> startActivity(new Intent(requireContext(), AddEmulatorActivity.class)));

        vm.platforms().observe(getViewLifecycleOwner(), list -> {
            platformIndex.clear();
            if (list != null) for (Platform p : list) platformIndex.put(p.id, p);
            adapter.notifyDataSetChanged();
        });

        vm.emulators().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            boolean isEmpty = list == null || list.isEmpty();
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }

    private class SwipeToDelete extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable background = new ColorDrawable(Color.parseColor("#E8002D"));
        private final Drawable icon;

        SwipeToDelete() {
            super(0, ItemTouchHelper.LEFT);
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
            int pos = vh.getBindingAdapterPosition();
            Emulator e = adapter.getAt(pos);
            new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.delete, (d, w) -> vm.repo.delete(e))
                .setNegativeButton(R.string.cancel, (d, w) -> adapter.notifyItemChanged(pos))
                .setOnCancelListener(d -> adapter.notifyItemChanged(pos))
                .show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            View item = vh.itemView;
            background.setBounds(item.getRight() + (int) dX, item.getTop(), item.getRight(), item.getBottom());
            background.draw(c);
            if (icon != null) {
                int iconMargin = (item.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconLeft = item.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconTop = item.getTop() + iconMargin;
                int iconRight = item.getRight() - iconMargin;
                int iconBottom = iconTop + icon.getIntrinsicHeight();
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                icon.draw(c);
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
