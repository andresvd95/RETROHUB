package com.retrohub.launcher.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

public class GameGridAdapter extends RecyclerView.Adapter<GameGridAdapter.VH> {

    public interface OnGameAction {
        void onClick(GameEntry game);
        void onEdit(GameEntry game);
        void onDelete(GameEntry game);
        void onToggleFavorite(GameEntry game);
    }

    private final List<GameEntry> data = new ArrayList<>();
    private final Map<String, Platform> platformIndex;
    private final OnGameAction listener;

    public GameGridAdapter(Map<String, Platform> platformIndex, OnGameAction listener) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_grid, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        GameEntry g = data.get(position);
        Platform p = platformIndex == null ? null : platformIndex.get(g.platformId);
        int color = p == null ? 0xFF22223A : ColorUtils.parseHex(p.colorHex, 0xFF22223A);
        h.background.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.withAlphaFraction(color, 0.5f)));
        h.emoji.setText(p == null ? "🎮" : p.emojiIcon);
        h.title.setText(g.title);
        h.platform.setText(p == null ? g.platformId : p.shortName);
        h.playtime.setText(TimeUtils.formatPlayTimeShort(g.playTimeSeconds));
        h.fav.setImageResource(g.isFavorite ? R.drawable.ic_star : R.drawable.ic_star_outline);
        h.fav.setOnClickListener(v -> { if (listener != null) listener.onToggleFavorite(g); });

        if (g.coverUri != null && !g.coverUri.isEmpty()) {
            h.cover.setVisibility(View.VISIBLE);
            h.emoji.setVisibility(View.GONE);
            Glide.with(h.cover).load(new File(g.coverUri))
                .centerCrop()
                .into(h.cover);
        } else {
            h.cover.setVisibility(View.GONE);
            h.emoji.setVisibility(View.VISIBLE);
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(g); });
        h.itemView.setOnLongClickListener(v -> { showMenu(v.getContext(), v, g); return true; });
    }

    private void showMenu(Context ctx, View anchor, GameEntry g) {
        PopupMenu menu = new PopupMenu(ctx, anchor);
        menu.getMenu().add(0, 1, 0, "Edit");
        menu.getMenu().add(0, 2, 1, g.isFavorite ? "Remove favorite" : "Mark favorite");
        menu.getMenu().add(0, 3, 2, "Delete");
        menu.setOnMenuItemClickListener((MenuItem item) -> {
            if (listener == null) return false;
            switch (item.getItemId()) {
                case 1: listener.onEdit(g); return true;
                case 2: listener.onToggleFavorite(g); return true;
                case 3: listener.onDelete(g); return true;
            }
            return false;
        });
        menu.show();
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View background;
        ImageView cover, fav;
        TextView emoji, title, platform, playtime;
        VH(View v) {
            super(v);
            background = v.findViewById(R.id.background);
            cover = v.findViewById(R.id.cover);
            emoji = v.findViewById(R.id.emoji);
            title = v.findViewById(R.id.title);
            platform = v.findViewById(R.id.platform);
            playtime = v.findViewById(R.id.playtime);
            fav = v.findViewById(R.id.fav);
        }
    }
}
