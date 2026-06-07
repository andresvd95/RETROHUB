package com.retrohub.launcher.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.retrohub.launcher.R;
import com.retrohub.launcher.data.repository.EmulatorRepository;
import com.retrohub.launcher.data.repository.GameRepository;
import com.retrohub.launcher.ui.emulators.EmulatorFragment;
import com.retrohub.launcher.ui.home.HomeFragment;
import com.retrohub.launcher.ui.library.LibraryFragment;
import com.retrohub.launcher.ui.platforms.PlatformFragment;
import com.retrohub.launcher.ui.settings.SettingsFragment;
import com.retrohub.launcher.util.SessionTimer;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_NAV_ID = "nav_selected";

    private BottomNavigationView bottomNav;
    private MainViewModel vm;
    private GameRepository gameRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);

        applySystemBarStyling();

        vm = new ViewModelProvider(this).get(MainViewModel.class);
        gameRepo = new GameRepository(this);
        // Refresh installed flags so the emulator screen is accurate.
        new EmulatorRepository(this).refreshInstalledFlags();

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            switchTo(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            int id = savedInstanceState.getInt(STATE_NAV_ID, R.id.nav_home);
            bottomNav.setSelectedItemId(id);
        }
    }

    private void applySystemBarStyling() {
        View root = getWindow().getDecorView();
        WindowInsetsControllerCompat insets = new WindowInsetsControllerCompat(getWindow(), root);
        insets.setAppearanceLightStatusBars(false);
        insets.setAppearanceLightNavigationBars(false);
    }

    public void switchTo(int navId) {
        Fragment fragment;
        if (navId == R.id.nav_home) fragment = new HomeFragment();
        else if (navId == R.id.nav_library) fragment = new LibraryFragment();
        else if (navId == R.id.nav_platforms) fragment = new PlatformFragment();
        else if (navId == R.id.nav_emulators) fragment = new EmulatorFragment();
        else fragment = new SettingsFragment();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit();
        bottomNav.setSelectedItemId(navId);
    }

    /** Public so fragments can navigate the bottom nav (e.g. a "view platforms" link). */
    public void selectTab(int navId) {
        bottomNav.setSelectedItemId(navId);
    }

    public void openLibraryForPlatform(String platformId) {
        LibraryFragment frag = LibraryFragment.newInstance(platformId);
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, frag)
            .commit();
        bottomNav.setSelectedItemId(R.id.nav_library);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionTimer.flush(this, gameRepo);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_NAV_ID, bottomNav.getSelectedItemId());
    }
}
