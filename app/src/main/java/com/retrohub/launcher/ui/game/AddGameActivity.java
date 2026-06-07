package com.retrohub.launcher.ui.game;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.retrohub.launcher.R;
import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;
import com.retrohub.launcher.util.ImageStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddGameActivity extends AppCompatActivity {

    protected EditText inputTitle, inputRom, inputArgs;
    protected Spinner spinnerPlatform, spinnerEmulator, spinnerGenre;
    protected ImageView coverPreview;
    protected String selectedCoverPath;

    protected GameRepository gameRepo;
    protected EmulatorRepository emuRepo;
    protected PlatformRepository platformRepo;
    protected List<Platform> platforms = new ArrayList<>();
    protected List<Emulator> currentEmulators = new ArrayList<>();

    private final ActivityResultLauncher<String> pickImage =
        registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    private final ActivityResultLauncher<String[]> pickRom =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onRomPicked);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);

        gameRepo = new GameRepository(this);
        emuRepo = new EmulatorRepository(this);
        platformRepo = new PlatformRepository(this);

        coverPreview = findViewById(R.id.cover_preview);
        inputTitle = findViewById(R.id.input_title);
        inputRom = findViewById(R.id.input_rom);
        inputArgs = findViewById(R.id.input_args);
        spinnerPlatform = findViewById(R.id.spinner_platform);
        spinnerEmulator = findViewById(R.id.spinner_emulator);
        spinnerGenre = findViewById(R.id.spinner_genre);

        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(
            this, R.array.genres, android.R.layout.simple_spinner_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_pick_cover).setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btn_pick_rom).setOnClickListener(v -> pickRom.launch(new String[]{"*/*"}));
        ((Button) findViewById(R.id.btn_save)).setOnClickListener(v -> save());

        spinnerPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                if (pos >= 0 && pos < platforms.size()) loadEmulatorsForPlatform(platforms.get(pos).id, null);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadPlatforms();
        onAfterCreate();
    }

    /** Hook for subclasses to prefill after spinners exist. */
    protected void onAfterCreate() {}

    private void loadPlatforms() {
        AppDatabase.io.execute(() -> {
            final List<Platform> list = platformRepo.getAllSync();
            runOnUiThread(() -> {
                platforms = list;
                ArrayList<String> labels = new ArrayList<>();
                for (Platform p : platforms) labels.add(p.emojiIcon + "  " + p.name);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    AddGameActivity.this, android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlatform.setAdapter(adapter);
                afterPlatformsLoaded();
            });
        });
    }

    /** Called once the platform spinner is populated. Subclasses can prefill selections here. */
    protected void afterPlatformsLoaded() {}

    protected void loadEmulatorsForPlatform(String platformId, Integer selectEmulatorId) {
        AppDatabase.io.execute(() -> {
            final List<Emulator> list = emuRepo.getByPlatformSync(platformId);
            runOnUiThread(() -> {
                currentEmulators = list;
                ArrayList<String> labels = new ArrayList<>();
                for (Emulator e : currentEmulators) labels.add(e.name);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    AddGameActivity.this, android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEmulator.setAdapter(adapter);
                if (selectEmulatorId != null) {
                    for (int i = 0; i < currentEmulators.size(); i++) {
                        if (currentEmulators.get(i).id == selectEmulatorId) {
                            spinnerEmulator.setSelection(i);
                            break;
                        }
                    }
                }
            });
        });
    }

    protected void selectPlatformById(String platformId) {
        for (int i = 0; i < platforms.size(); i++) {
            if (platforms.get(i).id.equals(platformId)) {
                spinnerPlatform.setSelection(i);
                return;
            }
        }
    }

    protected void selectGenre(String genre) {
        if (genre == null) return;
        ArrayAdapter<CharSequence> a = (ArrayAdapter<CharSequence>) spinnerGenre.getAdapter();
        for (int i = 0; i < a.getCount(); i++) {
            if (genre.equalsIgnoreCase(String.valueOf(a.getItem(i)))) {
                spinnerGenre.setSelection(i);
                return;
            }
        }
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) return;
        try {
            selectedCoverPath = ImageStorage.copyToPrivate(this, uri, "covers");
            Glide.with(coverPreview).load(new File(selectedCoverPath)).into(coverPreview);
        } catch (IOException e) {
            Toast.makeText(this, "Could not save cover", Toast.LENGTH_SHORT).show();
        }
    }

    private void onRomPicked(Uri uri) {
        if (uri == null) return;
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {}
        inputRom.setText(uri.toString());
    }

    protected void save() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) { inputTitle.setError("Required"); return; }
        int pPos = spinnerPlatform.getSelectedItemPosition();
        if (pPos < 0 || pPos >= platforms.size()) {
            Toast.makeText(this, "Pick a platform", Toast.LENGTH_SHORT).show();
            return;
        }
        int ePos = spinnerEmulator.getSelectedItemPosition();
        if (ePos < 0 || ePos >= currentEmulators.size()) {
            Toast.makeText(this, "Pick an emulator (add one in the Emulators tab first)", Toast.LENGTH_LONG).show();
            return;
        }
        GameEntry g = currentGame();
        g.title = title;
        g.platformId = platforms.get(pPos).id;
        g.emulatorId = currentEmulators.get(ePos).id;
        String romText = inputRom.getText().toString().trim();
        g.romPath = romText.isEmpty() ? null : romText;
        String args = inputArgs.getText().toString().trim();
        g.customArgs = args.isEmpty() ? null : args;
        g.genre = String.valueOf(spinnerGenre.getSelectedItem());
        if (selectedCoverPath != null) g.coverUri = selectedCoverPath;
        persistAndFinish(g);
    }

    /** New game by default; subclass overrides to edit existing. */
    protected GameEntry currentGame() { return new GameEntry(); }

    /** New game inserts; subclass overrides for update. */
    protected void persistAndFinish(GameEntry g) {
        gameRepo.insert(g, id -> runOnUiThread(this::finish));
    }
}
