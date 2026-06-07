package com.retrohub.launcher.ui.settings;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.retrohub.launcher.R;
import com.retrohub.launcher.data.db.AppDatabase;
import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.GameRepository;

public class SettingsFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        TextView version = v.findViewById(R.id.version_text);
        version.setText(versionString());

        v.findViewById(R.id.btn_refresh_install).setOnClickListener(view ->
            new EmulatorRepository(requireContext()).refreshInstalledFlags());

        v.findViewById(R.id.btn_clear_data).setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_reset_db)
                .setMessage(R.string.settings_reset_db_msg)
                .setPositiveButton(R.string.delete, (d, w) -> clearAll())
                .setNegativeButton(R.string.cancel, null)
                .show();
        });
    }

    private void clearAll() {
        GameRepository gr = new GameRepository(requireContext());
        EmulatorRepository er = new EmulatorRepository(requireContext());
        AppDatabase.io.execute(() -> {
            for (com.retrohub.launcher.data.model.GameEntry g : gr.getAllSync()) gr.delete(g);
            for (com.retrohub.launcher.data.model.Emulator e : er.getAllSync()) er.delete(e);
        });
    }

    private String versionString() {
        try {
            PackageInfo info = requireContext().getPackageManager()
                .getPackageInfo(requireContext().getPackageName(), 0);
            return getString(R.string.settings_version) + " " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return getString(R.string.settings_version);
        }
    }
}
