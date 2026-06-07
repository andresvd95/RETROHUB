package com.retrohub.launcher.ui.emulators;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.retrohub.launcher.R;
import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.Platform;
import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.PlatformRepository;
import com.retrohub.launcher.util.ImageStorage;
import com.retrohub.launcher.util.InstalledAppsLoader;
import com.retrohub.launcher.util.KnownEmulators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEmulatorActivity extends AppCompatActivity {

    public static final String EXTRA_EMULATOR_ID = "emulator_id";

    private EditText nameInput, packageInput, activityInput;
    private Spinner platformSpinner;
    private ImageView iconPreview;
    private String selectedIconPath;
    private int editingId = -1;

    private EmulatorRepository repo;
    private PlatformRepository platformRepo;
    private List<Platform> platformList = new ArrayList<>();

    private final ActivityResultLauncher<String> pickImage =
        registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emulator);
        repo = new EmulatorRepository(this);
        platformRepo = new PlatformRepository(this);

        nameInput = findViewById(R.id.input_name);
        packageInput = findViewById(R.id.input_package);
        activityInput = findViewById(R.id.input_activity);
        platformSpinner = findViewById(R.id.spinner_platform);
        iconPreview = findViewById(R.id.icon_preview);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_detect_apps).setOnClickListener(v -> showInstalledApps());
        findViewById(R.id.btn_known_emulators).setOnClickListener(v -> showKnownEmulators());
        findViewById(R.id.btn_change_icon).setOnClickListener(v -> pickImage.launch("image/*"));
        ((Button) findViewById(R.id.btn_save)).setOnClickListener(v -> save());

        loadPlatforms();

        editingId = getIntent().getIntExtra(EXTRA_EMULATOR_ID, -1);
        if (editingId > 0) loadForEdit(editingId);
    }

    private void loadPlatforms() {
        AppDatabase.io.execute(() -> {
            final List<Platform> result = platformRepo.getAllSync();
            runOnUiThread(() -> {
                platformList = result;
                List<String> labels = new ArrayList<>();
                for (Platform p : platformList) labels.add(p.emojiIcon + "  " + p.name);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    AddEmulatorActivity.this, android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                platformSpinner.setAdapter(adapter);
            });
        });
    }

    private void loadForEdit(int id) {
        AppDatabase.io.execute(() -> {
            Emulator e = repo.getById(id);
            if (e == null) return;
            runOnUiThread(() -> {
                nameInput.setText(e.name);
                packageInput.setText(e.packageName);
                activityInput.setText(e.activityName);
                selectedIconPath = e.iconUri;
                if (e.iconUri != null && !e.iconUri.isEmpty()) {
                    Glide.with(iconPreview).load(new File(e.iconUri)).into(iconPreview);
                }
                selectPlatform(e.platformId);
            });
        });
    }

    private void selectPlatform(String platformId) {
        if (platformId == null) return;
        for (int i = 0; i < platformList.size(); i++) {
            if (platformId.equals(platformList.get(i).id)) {
                platformSpinner.setSelection(i);
                return;
            }
        }
    }

    private void showInstalledApps() {
        List<InstalledAppsLoader.AppEntry> apps = InstalledAppsLoader.listLaunchableApps(this);
        if (apps.isEmpty()) {
            Toast.makeText(this, "No installed apps found", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) items[i] = apps.get(i).label + "\n" + apps.get(i).packageName;
        new AlertDialog.Builder(this)
            .setTitle("Installed apps")
            .setItems(items, (d, which) -> {
                InstalledAppsLoader.AppEntry a = apps.get(which);
                nameInput.setText(a.label);
                packageInput.setText(a.packageName);
            })
            .show();
    }

    private void showKnownEmulators() {
        List<KnownEmulators.Known> list = KnownEmulators.all();
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) items[i] = list.get(i).toString();
        new AlertDialog.Builder(this)
            .setTitle(R.string.known_emulator)
            .setItems(items, (d, which) -> {
                KnownEmulators.Known k = list.get(which);
                nameInput.setText(k.name);
                packageInput.setText(k.packageName);
                if (k.platformId != null) selectPlatform(k.platformId);
            })
            .show();
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) return;
        try {
            selectedIconPath = ImageStorage.copyToPrivate(this, uri, "emulator_icons");
            Glide.with(iconPreview).load(new File(selectedIconPath)).into(iconPreview);
        } catch (IOException e) {
            Toast.makeText(this, "Could not save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        String pkg = packageInput.getText().toString().trim();
        String activity = activityInput.getText().toString().trim();
        if (name.isEmpty()) { nameInput.setError("Required"); return; }
        if (pkg.isEmpty()) { packageInput.setError("Required"); return; }
        int sel = platformSpinner.getSelectedItemPosition();
        if (sel < 0 || sel >= platformList.size()) {
            Toast.makeText(this, "Pick a platform", Toast.LENGTH_SHORT).show();
            return;
        }
        Emulator e = new Emulator();
        if (editingId > 0) e.id = editingId;
        e.name = name;
        e.packageName = pkg;
        e.activityName = activity.isEmpty() ? null : activity;
        e.platformId = platformList.get(sel).id;
        e.iconUri = selectedIconPath;
        if (editingId > 0) {
            repo.update(e);
            runOnUiThread(this::finish);
        } else {
            repo.insert(e, () -> runOnUiThread(this::finish));
        }
    }
}
