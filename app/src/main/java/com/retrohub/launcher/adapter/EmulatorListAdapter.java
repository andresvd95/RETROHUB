package com.retrohub.launcher.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import androidx.appcompat.widget.SwitchCompat;

import com.retrohub.launcher.R;
import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmulatorListAdapter extends RecyclerView.Adapter<EmulatorListAdapter.VH> {

    public interface OnEmulatorAction {
        void onClick(Emulator e);
        void onToggleEnabled(Emulator e, boolean enabled);
    }

    private final List<Emulator> data = new ArrayList<>();
    private final Map<String, Platform> platformIndex;
    private final OnEmulatorAction listener;

    public EmulatorListAdapter(Map<String, Platform> platformIndex, OnEmulatorAction listener) {
        this.platformIndex = platformIndex;
        this.listener = listener;
    }

    public void submit(List<Emulator> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public Emulator getAt(int position) { return data.get(position); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emulator, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Emulator e = data.get(position);
        Context ctx = h.itemView.getContext();
        h.name.setText(e.name);
        h.pkg.setText(e.packageName);
        Platform p = platformIndex == null ? null : platformIndex.get(e.platformId);
        h.platformChip.setText(p == null ? (e.platformId == null ? "—" : e.platformId) : p.shortName);
        h.dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            e.isInstalled ? 0xFF3DDC84 : 0xFFE8002D));
        h.toggle.setOnCheckedChangeListener(null);
        h.toggle.setChecked(e.enabled);
        h.toggle.setOnCheckedChangeListener((CompoundButton b, boolean checked) -> {
            if (listener != null) listener.onToggleEnabled(e, checked);
        });

        if (e.iconUri != null && !e.iconUri.isEmpty()) {
            Glide.with(h.icon).load(new File(e.iconUri)).into(h.icon);
        } else {
            Drawable icon = null;
            try {
                icon = ctx.getPackageManager().getApplicationIcon(e.packageName);
            } catch (PackageManager.NameNotFoundException ignored) {}
            if (icon != null) h.icon.setImageDrawable(icon);
            else h.icon.setImageResource(R.drawable.ic_gamepad);
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, pkg, platformChip;
        View dot;
        SwitchCompat toggle;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            name = v.findViewById(R.id.name);
            pkg = v.findViewById(R.id.pkg);
            platformChip = v.findViewById(R.id.platform_chip);
            dot = v.findViewById(R.id.dot);
            toggle = v.findViewById(R.id.toggle);
        }
    }
}
