package com.retrohub.launcher.ui.game;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
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
import com.retrohub.launcher.ui.emulators.AddEmulatorActivity;
import com.retrohub.launcher.util.ImageStorage;
import com.retrohub.launcher.util.RomFolderScanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddGameActivity extends AppCompatActivity {

    protected EditText inputTitle, inputRom, inputArgs;
    protected Spinner spinnerPlatform, spinnerEmulator, spinnerGenre;
    protected ImageView coverPreview;
    protected String selectedCoverPath;
    private View noEmulatorPanel;

    protected GameRepository gameRepo;
    protected EmulatorRepository emuRepo;
    protected PlatformRepository platformRepo;
    protected List<Platform> platforms = new ArrayList<>();
    protected List<Emulator> currentEmulators = new ArrayList<>();

    private final ActivityResultLauncher<String> pickImage =
        registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    private final ActivityResultLauncher<String[]> pickRom =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onRomPicked);

    private final ActivityResultLauncher<Uri> pickFolder =
        registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), this::onFolderPicked);
    private String folderPlatformId;
    private int folderEmulatorId;

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
        noEmulatorPanel = findViewById(R.id.no_emulator_panel);

        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(
            this, R.array.genres, android.R.layout.simple_spinner_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_pick_cover).setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btn_pick_rom).setOnClickListener(v -> pickRom.launch(new String[]{"*/*"}));
        findViewById(R.id.btn_add_emulator).setOnClickListener(v ->
            startActivity(new Intent(this, AddEmulatorActivity.class)));
        ((Button) findViewById(R.id.btn_save)).setOnClickListener(v -> save());

        View importPanel = findViewById(R.id.import_folder_panel);
        if (getClass() == AddGameActivity.class) {
            findViewById(R.id.btn_import_folder).setOnClickListener(v -> startFolderImport());
        } else {
            importPanel.setVisibility(View.GONE); // Editing a single game: no bulk import.
        }

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
                boolean hasEmulator = !currentEmulators.isEmpty();
                noEmulatorPanel.setVisibility(hasEmulator ? View.GONE : View.VISIBLE);
                spinnerEmulator.setVisibility(hasEmulator ? View.VISIBLE : View.GONE);
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
        if (inputTitle.getText().toString().trim().isEmpty()) {
            String displayName = displayNameFor(uri);
            if (displayName != null) inputTitle.setText(stripExtension(displayName));
        }
    }

    /** Beacon-style setup: one folder per platform, every game imported at once. */
    private void startFolderImport() {
        int pPos = spinnerPlatform.getSelectedItemPosition();
        if (pPos < 0 || pPos >= platforms.size()) {
            Toast.makeText(this, R.string.pick_platform_error, Toast.LENGTH_SHORT).show();
            return;
        }
        int ePos = spinnerEmulator.getSelectedItemPosition();
        if (ePos < 0 || ePos >= currentEmulators.size()) {
            Toast.makeText(this, R.string.pick_emulator_error, Toast.LENGTH_LONG).show();
            return;
        }
        folderPlatformId = platforms.get(pPos).id;
        folderEmulatorId = currentEmulators.get(ePos).id;
        pickFolder.launch(null);
    }

    private void onFolderPicked(Uri treeUri) {
        if (treeUri == null || folderPlatformId == null) return;
        try {
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {}

        Toast.makeText(this, R.string.import_folder_scanning, Toast.LENGTH_SHORT).show();
        final String platformId = folderPlatformId;
        final int emulatorId = folderEmulatorId;
        AppDatabase.io.execute(() -> {
            List<GameEntry> games = RomFolderScanner.scan(
                this, treeUri, platformId, emulatorId, gameRepo.getAllRomPathsSync());
            if (!games.isEmpty()) gameRepo.insertAllSync(games);
            runOnUiThread(() -> {
                if (games.isEmpty()) {
                    Toast.makeText(this, R.string.import_folder_none, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getResources().getQuantityString(
                        R.plurals.import_folder_done, games.size(), games.size()), Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }

    private String displayNameFor(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
            uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (RuntimeException ignored) {}
        return uri.getLastPathSegment();
    }

    static String stripExtension(String name) {
        if (name == null) return null;
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!platforms.isEmpty()) {
            int position = spinnerPlatform.getSelectedItemPosition();
            if (position >= 0 && position < platforms.size()) {
                int emulatorPosition = spinnerEmulator.getSelectedItemPosition();
                Integer selectedId = emulatorPosition >= 0 && emulatorPosition < currentEmulators.size()
                    ? currentEmulators.get(emulatorPosition).id : null;
                loadEmulatorsForPlatform(platforms.get(position).id, selectedId);
            }
        }
    }

    protected void save() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) { inputTitle.setError(getString(R.string.required_field)); return; }
        int pPos = spinnerPlatform.getSelectedItemPosition();
        if (pPos < 0 || pPos >= platforms.size()) {
            Toast.makeText(this, R.string.pick_platform_error, Toast.LENGTH_SHORT).show();
            return;
        }
        int ePos = spinnerEmulator.getSelectedItemPosition();
        if (ePos < 0 || ePos >= currentEmulators.size()) {
            Toast.makeText(this, R.string.pick_emulator_error, Toast.LENGTH_LONG).show();
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
