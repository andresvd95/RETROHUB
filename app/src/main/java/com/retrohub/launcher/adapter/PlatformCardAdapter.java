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

public class PlatformCardAdapter extends RecyclerView.Adapter<PlatformCardAdapter.VH> {

    public interface OnPlatformClick { void onClick(Platform p); }

    public static class Row {
        public final Platform platform;
        public final int gameCount;
        public Row(Platform p, int gameCount) { this.platform = p; this.gameCount = gameCount; }
    }

    private final List<Row> data = new ArrayList<>();
    private final OnPlatformClick listener;

    public PlatformCardAdapter(OnPlatformClick listener) {
        this.listener = listener;
    }

    public void submit(List<Row> rows) {
        data.clear();
        if (rows != null) data.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_platform_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Row row = data.get(position);
        Platform p = row.platform;
        int color = ColorUtils.parseHex(p.colorHex, 0xFF22223A);
        h.card.setBackgroundTintList(ColorStateList.valueOf(blend(color)));
        h.accent.setBackgroundColor(color);
        h.emoji.setText(p.emojiIcon);
        h.name.setText(p.name);
        h.count.setText(row.gameCount == 0 ? "No games yet" : row.gameCount + " games");
        h.chip.setText(p.shortName);
        h.chip.setBackgroundTintList(ColorStateList.valueOf(color));
        h.itemView.setAlpha(row.gameCount == 0 ? 0.55f : 1f);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(p); });
    }

    private int blend(int color) {
        // 12% tint over the surface color.
        return ColorUtils.withAlphaFraction(color, 0.12f);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View card, accent;
        TextView emoji, name, count, chip;
        VH(View v) {
            super(v);
            card = v.findViewById(R.id.card);
            accent = v.findViewById(R.id.accent);
            emoji = v.findViewById(R.id.emoji);
            name = v.findViewById(R.id.name);
            count = v.findViewById(R.id.count);
            chip = v.findViewById(R.id.chip);
        }
    }
}
