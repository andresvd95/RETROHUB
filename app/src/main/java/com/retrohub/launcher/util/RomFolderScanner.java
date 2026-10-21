package com.retrohub.launcher.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.retrohub.launcher.data.model.GameEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lists game files inside a folder picked with the system folder picker
 * (ACTION_OPEN_DOCUMENT_TREE), so a whole platform can be imported in one step.
 */
public final class RomFolderScanner {

    private static final int MAX_DEPTH = 3;

    /** Files that are never games: saves, artwork, notes, metadata. */
    private static final Set<String> IGNORED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "txt", "nfo", "xml", "json", "ini", "cfg", "dat", "log", "md", "pdf", "html",
        "jpg", "jpeg", "png", "gif", "webp", "bmp",
        "srm", "sav", "state", "st0", "st1", "st2", "auto", "ss", "mcr", "rtc", "bak", "tmp",
        "mp3", "ogg", "wav", "mp4", "mkv"
    ));

    private RomFolderScanner() {}

    /** Builds one GameEntry per game file found under {@code treeUri}, skipping known ROM paths. */
    public static List<GameEntry> scan(Context context, Uri treeUri, String platformId,
                                       int emulatorId, Set<String> existingRomPaths) {
        List<GameEntry> found = new ArrayList<>();
        String rootId = DocumentsContract.getTreeDocumentId(treeUri);
        walk(context.getContentResolver(), treeUri, rootId, 0, platformId, emulatorId,
            existingRomPaths, found);
        return found;
    }

    private static void walk(ContentResolver resolver, Uri treeUri, String parentId, int depth,
                             String platformId, int emulatorId, Set<String> existing,
                             List<GameEntry> out) {
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId);
        String[] projection = {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        };
        try (Cursor c = resolver.query(children, projection, null, null, null)) {
            if (c == null) return;
            while (c.moveToNext()) {
                String docId = c.getString(0);
                String name = c.getString(1);
                String mime = c.getString(2);
                if (name == null || name.startsWith(".")) continue;

                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    if (depth < MAX_DEPTH) {
                        walk(resolver, treeUri, docId, depth + 1, platformId, emulatorId, existing, out);
                    }
                    continue;
                }
                if (!looksLikeGame(name)) continue;

                String romPath = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId).toString();
                if (!existing.add(romPath)) continue;

                GameEntry g = new GameEntry();
                g.title = cleanTitle(name);
                g.platformId = platformId;
                g.emulatorId = emulatorId;
                g.romPath = romPath;
                out.add(g);
            }
        } catch (RuntimeException ignored) {
            // A provider can refuse a sub-folder; keep what was found so far.
        }
    }

    static boolean looksLikeGame(String name) {
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) return false;
        String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        return !IGNORED_EXTENSIONS.contains(ext);
    }

    /** "Super Mario World (USA) [!].sfc" -> "Super Mario World". */
    static String cleanTitle(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String cleaned = base.replaceAll("\\s*[\\(\\[][^\\)\\]]*[\\)\\]]", "")
            .replace('_', ' ')
            .replaceAll("\\s+", " ")
            .trim();
        return cleaned.isEmpty() ? base : cleaned;
    }
}
