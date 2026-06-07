package com.retrohub.launcher.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retrohub.launcher.R;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.util.ColorUtils;
import com.retrohub.launcher.util.TimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameCardAdapter extends RecyclerView.Adapter<GameCardAdapter.VH> {

    public interface OnGameClick { void onClick(GameEntry game); }

    private final List<GameEntry> data = new ArrayList<>();
    private final Map<String, Platform> platformIndex;
    private final OnGameClick listener;

    public GameCardAdapter(Map<String, Platform> platformIndex, OnGameClick listener) {
        this.platformIndex = platformIndex;
        this.listener = listener;
    }

    public void submit(List<GameEntry> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        GameEntry g = data.get(position);
        Platform p = platformIndex == null ? null : platformIndex.get(g.platformId);
        int color = p == null ? 0xFF22223A : ColorUtils.parseHex(p.colorHex, 0xFF22223A);
        h.card.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.withAlphaFraction(color, 0.20f)));
        h.emoji.setText(p == null ? "🎮" : p.emojiIcon);
        h.title.setText(g.title);
        h.playtime.setText(TimeUtils.formatPlayTimeShort(g.playTimeSeconds));
        h.platformChip.setText(p == null ? g.platformId : p.shortName);
        if (g.coverUri != null && !g.coverUri.isEmpty()) {
            h.cover.setVisibility(View.VISIBLE);
            h.emoji.setVisibility(View.GONE);
            Glide.with(h.cover).load(new File(g.coverUri))
                .placeholder(R.drawable.bg_card_small)
                .into(h.cover);
        } else {
            h.cover.setVisibility(View.GONE);
            h.emoji.setVisibility(View.VISIBLE);
        }
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(g); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View card;
        ImageView cover;
        TextView emoji, title, playtime, platformChip;
        VH(View v) {
            super(v);
            card = v.findViewById(R.id.card);
            cover = v.findViewById(R.id.cover);
            emoji = v.findViewById(R.id.emoji);
            title = v.findViewById(R.id.title);
            playtime = v.findViewById(R.id.playtime);
            platformChip = v.findViewById(R.id.platform_chip);
        }
    }
}
