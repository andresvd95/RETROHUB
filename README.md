# RetroHub

RetroHub is a native Android launcher for organizing and opening retro games through emulators installed on the device. The app works offline: it stores platforms, emulators, games, covers, favorites, play history, and accumulated play time in a local Room database.

## What It Does

- Registers emulators by name, package name, optional activity name, platform, and custom icon.
- Detects installed launchable apps to make emulator setup faster.
- Includes a curated list of known Android emulators such as RetroArch, PPSSPP, Dolphin, AetherSX2, ePSXe, DraStic, Winlator, MAME4droid, Redream, and others.
- Lets users add games with title, platform, assigned emulator, ROM/ISO path, cover image, genre, favorite flag, and optional custom launch arguments.
- Launches games by opening the selected emulator and passing the ROM URI when a local file path exists.
- Falls back to opening the emulator directly if the specific launch intent cannot be resolved.
- Shows a Play Store search prompt when the assigned emulator is not installed.
- Tracks play sessions and adds elapsed time when the user returns to RetroHub.
- Provides Home, Library, Platforms, Emulators, Search, and Settings sections.
- Supports filtering by platform, favorites, and recent games.
- Supports sorting the library by name, most played, recent, or platform.
- Stores selected cover and emulator icon images in app-private storage.

## Technology

- **Language:** Java 17
- **Platform:** Android
- **Build system:** Gradle 8.7 with Android Gradle Plugin 8.5.0
- **Android SDK:** compileSdk 34, minSdk 26, targetSdk 34
- **UI:** Android Views, XML layouts, AppCompat, Material Components, ConstraintLayout, RecyclerView, ViewPager2, CoordinatorLayout
- **Architecture:** Activity/Fragment UI with ViewModel and LiveData
- **Database:** Room 2.6.1
- **Image loading:** Glide 4.16.0
- **File sharing:** AndroidX FileProvider for ROM URI access

## Main Screens

- **Home:** summary of total games/platforms, featured recent game, recently played games, recommended games, and platform shortcuts.
- **Library:** searchable game grid with platform chips, sorting, edit/delete actions, favorite toggle, and game detail bottom sheet.
- **Platforms:** seeded catalog of retro platforms used to group games and emulators.
- **Emulators:** emulator management, installed-app detection, known-emulator presets, and install-state refresh.
- **Search:** global search across games and platforms.
- **Settings:** app information and local data reset.

## Local Data Model

RetroHub uses a Room database named `retrohub.db` with these entities:

- `Platform`: predefined platform catalog with id, name, short name, color, icon, and sort order.
- `Emulator`: emulator metadata, Android package/activity, platform relation, icon path, install status, and enabled flag.
- `GameEntry`: game metadata, platform, emulator relation, ROM path, cover path, genre, favorite flag, last played time, and total play time.

The platform catalog is seeded on first database creation with systems such as NES, SNES, Nintendo 64, GameCube, Wii, Game Boy, Nintendo DS, PlayStation, PS2, PS3, PSP, Xbox, Sega Genesis, Saturn, Dreamcast, Arcade/MAME, PICO-8, GameHub, and Winlator.

## Android Permissions

The app declares these permissions:

- `READ_EXTERNAL_STORAGE` up to Android 12 for reading selected local media/files.
- `READ_MEDIA_IMAGES` for image selection on newer Android versions.
- `QUERY_ALL_PACKAGES` so RetroHub can inspect installed apps and determine emulator availability.

## Project Structure

```text
app/src/main/java/com/retrohub/launcher/
  adapter/          RecyclerView adapters for games, platforms, and emulators
  data/db/          Room database and DAOs
  data/model/       Room entities
  data/repository/  Repository layer for database operations
  ui/               Activities, fragments, and ViewModels
  util/             Launcher, image storage, app detection, timers, helpers

app/src/main/res/
  layout/           XML screens and item layouts
  drawable/         Icons and UI backgrounds
  values/           Colors, dimensions, strings, and themes
  menu/             Bottom navigation and library menu
  xml/              Backup rules and FileProvider paths
```

## Build

Open the project in Android Studio and sync Gradle. Then build or run the `app` module on an Android device or emulator.

If the Gradle wrapper scripts are available in the local checkout, the debug build can be generated with:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Notes

- RetroHub does not include emulators or ROMs.
- Users must install their own emulators and provide their own game files.
- Launch behavior depends on whether each emulator supports opening ROM files through Android intents.
