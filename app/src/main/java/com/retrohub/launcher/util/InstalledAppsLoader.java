package com.retrohub.launcher.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InstalledAppsLoader {

    public static class AppEntry {
        public final String label;
        public final String packageName;
        public AppEntry(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
        @Override public String toString() { return label + "\n" + packageName; }
    }

    /** Returns user-installed launchable apps, sorted alphabetically. */
    public static List<AppEntry> listLaunchableApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = pm.queryIntentActivities(main, 0);
        List<AppEntry> result = new ArrayList<>();
        String selfPkg = context.getPackageName();
        for (ResolveInfo ri : resolved) {
            ApplicationInfo info = ri.activityInfo.applicationInfo;
            if (selfPkg.equals(info.packageName)) continue;
            boolean isSystem = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;
            if (isSystem) continue;
            String label = String.valueOf(info.loadLabel(pm));
            result.add(new AppEntry(label, info.packageName));
        }
        Collections.sort(result, new Comparator<AppEntry>() {
            @Override public int compare(AppEntry a, AppEntry b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });
        return result;
    }
}
