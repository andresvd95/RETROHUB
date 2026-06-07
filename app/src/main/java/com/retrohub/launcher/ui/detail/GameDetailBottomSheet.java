package com.retrohub.launcher.ui.detail;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.retrohub.launcher.R;
import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.ui.game.EditGameActivity;
import com.retrohub.launcher.util.AppLauncher;
import com.retrohub.launcher.util.ColorUtils;
import com.retrohub.launcher.util.SessionTimer;
import com.retrohub.launcher.util.TimeUtils;

import java.io.File;

public class GameDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GAME_ID = "game_id";

    public static GameDetailBottomSheet newInstance(int gameId) {
        GameDetailBottomSheet f = new GameDetailBottomSheet();
        Bundle b = new Bundle();
        b.putInt(ARG_GAME_ID, gameId);
        f.setArguments(b);
        return f;
    }

    private DetailViewModel vm;
    private GameEntry currentGame;
    private Platform currentPlatform;
    private Emulator currentEmulator;

    private ImageView cover;
    private TextView emoji, title, subtitle, statPlaytime, statPlatform;
    private ImageView favIcon;
    private View coverBg;
    private Button playBtn, editBtn, closeBtn;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_game_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        cover = v.findViewById(R.id.cover);
        emoji = v.findViewById(R.id.emoji);
        coverBg = v.findViewById(R.id.cover_bg);
        title = v.findViewById(R.id.title);
        subtitle = v.findViewById(R.id.subtitle);
        statPlaytime = v.findViewById(R.id.stat_playtime);
        statPlatform = v.findViewById(R.id.stat_platform);
        favIcon = v.findViewById(R.id.stat_favorite);
        playBtn = v.findViewById(R.id.btn_play);
        editBtn = v.findViewById(R.id.btn_edit);
        closeBtn = v.findViewById(R.id.btn_close);

        vm = new ViewModelProvider(this).get(DetailViewModel.class);
        int id = requireArguments().getInt(ARG_GAME_ID, -1);
        if (id <= 0) { dismiss(); return; }
        vm.setGameId(id);

        vm.game().observe(getViewLifecycleOwner(), this::bind);

        closeBtn.setOnClickListener(view -> dismiss());
        editBtn.setOnClickListener(view -> {
            if (currentGame == null) return;
            Intent i = new Intent(requireContext(), EditGameActivity.class);
            i.putExtra(EditGameActivity.EXTRA_GAME_ID, currentGame.id);
            startActivity(i);
            dismiss();
        });
        playBtn.setOnClickListener(view -> playCurrent());
        favIcon.setOnClickListener(view -> {
            if (currentGame == null) return;
            vm.games.setFavorite(currentGame.id, !currentGame.isFavorite);
        });
    }

    private void bind(GameEntry g) {
        if (g == null) return;
        currentGame = g;
        title.setText(g.title);
        statPlaytime.setText(TimeUtils.formatPlayTime(g.playTimeSeconds));
        favIcon.setImageResource(g.isFavorite ? R.drawable.ic_star : R.drawable.ic_star_outline);

        vm.runOnIo(() -> {
            currentPlatform = vm.platformFor(g);
            currentEmulator = vm.emulatorFor(g);
            if (getView() == null) return;
            getView().post(() -> applyPlatformAndEmulator(g));
        });

        if (g.coverUri != null && !g.coverUri.isEmpty()) {
            cover.setVisibility(View.VISIBLE);
            emoji.setVisibility(View.GONE);
            Glide.with(cover).load(new File(g.coverUri)).centerCrop().into(cover);
        } else {
            cover.setVisibility(View.GONE);
            emoji.setVisibility(View.VISIBLE);
        }
    }

    private void applyPlatformAndEmulator(GameEntry g) {
        int color = currentPlatform == null ? 0xFF22223A
            : ColorUtils.parseHex(currentPlatform.colorHex, 0xFF22223A);
        coverBg.setBackgroundTintList(ColorStateList.valueOf(color));
        emoji.setText(currentPlatform == null ? "🎮" : currentPlatform.emojiIcon);
        String platLabel = currentPlatform == null ? g.platformId : currentPlatform.name;
        String genre = g.genre == null || g.genre.isEmpty() ? "—" : g.genre;
        subtitle.setText(platLabel + " · " + genre);
        statPlatform.setText(currentPlatform == null ? g.platformId : currentPlatform.shortName);

        boolean canPlay = currentEmulator != null;
        playBtn.setEnabled(canPlay);
        playBtn.setAlpha(canPlay ? 1f : 0.5f);
    }

    private void playCurrent() {
        if (currentGame == null) return;
        if (currentEmulator == null) {
            playBtn.setText("No emulator assigned");
            return;
        }
        SessionTimer.start(requireContext(), currentGame.id);
        vm.games.markPlayedNow(currentGame.id);
        AppLauncher.launch(requireContext(), currentGame, currentEmulator);
        dismiss();
    }
}
