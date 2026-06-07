package com.retrohub.launcher.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.retrohub.launcher.data.model.Emulator;
import com.retrohub.launcher.data.model.GameEntry;

import java.io.File;

public class AppLauncher {

    private AppLauncher() {}

    public static void launch(Context context, GameEntry game, Emulator emulator) {
        if (emulator == null) {
            Toast.makeText(context, "No emulator assigned to this game", Toast.LENGTH_SHORT).show();
            return;
        }
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(emulator.packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            showNotInstalledDialog(context, emulator);
            return;
        }
        Intent intent = buildIntent(context, game, emulator);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Intent fallback = pm.getLaunchIntentForPackage(emulator.packageName);
            if (fallback != null) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallback);
            } else {
                Toast.makeText(context, "Could not open " + emulator.name, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static Intent buildIntent(Context context, GameEntry game, Emulator emulator) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(emulator.packageName);

        if (game != null && game.romPath != null && !game.romPath.isEmpty()) {
            File romFile = new File(game.romPath);
            if (romFile.exists()) {
                Uri romUri;
                try {
                    romUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        romFile);
                } catch (IllegalArgumentException ex) {
                    romUri = Uri.fromFile(romFile);
                }
                intent.setData(romUri);
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
            .setTitle(emulator.name + " is not installed")
            .setMessage("Do you want to look for it on the Play Store?")
            .setPositiveButton("Search", (d, w) -> {
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
            .setNegativeButton("Cancel", null)
            .show();
    }
}
