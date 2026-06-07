package com.retrohub.launcher.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retrohub.launcher.R;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class PlatformCompactAdapter extends RecyclerView.Adapter<PlatformCompactAdapter.VH> {

    public interface OnPlatformClick { void onClick(Platform p); }

    private final List<Platform> data = new ArrayList<>();
    private final OnPlatformClick listener;

    public PlatformCompactAdapter(OnPlatformClick listener) {
        this.listener = listener;
    }

    public void submit(List<Platform> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_platform_compact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Platform p = data.get(position);
        int color = ColorUtils.parseHex(p.colorHex, 0xFF22223A);
        h.iconBg.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.withAlphaFraction(color, 0.15f)));
        h.emoji.setText(p.emojiIcon);
        h.name.setText(p.shortName);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(p); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View iconBg;
        TextView emoji, name;
        VH(View v) {
            super(v);
            iconBg = v.findViewById(R.id.icon_bg);
            emoji = v.findViewById(R.id.emoji);
            name = v.findViewById(R.id.name);
        }
    }
}
