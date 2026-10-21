package com.retrohub.launcher.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.GameEntry;
import com.retrohub.launcher.R;

import java.io.File;

public class AppLauncher {

    private AppLauncher() {}

    /** Launches a game and reports whether an activity was actually opened. */
    public static boolean launch(Context context, GameEntry game, Emulator emulator) {
        if (emulator == null) {
            Toast.makeText(context, R.string.no_emulator_assigned, Toast.LENGTH_SHORT).show();
            return false;
        }
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(emulator.packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            showNotInstalledDialog(context, emulator);
            return false;
        }
        Intent intent = buildIntent(context, game, emulator);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Intent fallback = pm.getLaunchIntentForPackage(emulator.packageName);
            if (fallback != null) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallback);
                return true;
            } else {
                Toast.makeText(context, context.getString(R.string.could_not_open_emulator, emulator.name), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private static Intent buildIntent(Context context, GameEntry game, Emulator emulator) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(emulator.packageName);

        if (game != null && game.romPath != null && !game.romPath.isEmpty()) {
            Uri romUri = null;
            Uri storedUri = Uri.parse(game.romPath);
            String scheme = storedUri.getScheme();
            if ("content".equalsIgnoreCase(scheme) || "file".equalsIgnoreCase(scheme)) {
                romUri = storedUri;
            } else {
                File romFile = new File(game.romPath);
                if (romFile.exists()) {
                    try {
                        romUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider",
                            romFile);
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            if (romUri != null) {
                intent.setData(romUri);
                String mimeType = context.getContentResolver().getType(romUri);
                if (mimeType != null) intent.setDataAndType(romUri, mimeType);
                intent.setClipData(ClipData.newRawUri("ROM", romUri));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        if (emulator.activityName != null && !emulator.activityName.isEmpty()) {
            String activity = emulator.activityName.startsWith(".")
                ? emulator.packageName + emulator.activityName
                : emulator.activityName;
            intent.setComponent(new ComponentName(emulator.packageName, activity));
        }

        if (game != null && game.customArgs != null && !game.customArgs.isEmpty()) {
            intent.putExtra("args", game.customArgs);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static void showNotInstalledDialog(Context context, Emulator emulator) {
        new AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.emulator_not_installed_title, emulator.name))
            .setMessage(R.string.emulator_not_installed_body)
            .setPositiveButton(R.string.search_action, (d, w) -> {
                Intent storeIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=" + Uri.encode(emulator.name)));
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(storeIntent);
                } catch (ActivityNotFoundException ignored) {
                    Intent web = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/search?q=" + Uri.encode(emulator.name)));
                    web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(web);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}
