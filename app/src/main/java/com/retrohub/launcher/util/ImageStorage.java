package com.retrohub.launcher.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ImageStorage {
    private ImageStorage() {}

    /** Copy a content/file Uri into the app's private storage and return the absolute path. */
    public static String copyToPrivate(Context context, Uri source, String subdir) throws IOException {
        File dir = new File(context.getFilesDir(), subdir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create " + dir.getAbsolutePath());
        }
        File target = new File(dir, UUID.randomUUID().toString() + ".img");
        try (InputStream in = context.getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) throw new IOException("Cannot open " + source);
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
        return target.getAbsolutePath();
    }
}
