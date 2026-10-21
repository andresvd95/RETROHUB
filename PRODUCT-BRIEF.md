# RetroHub product brief

## Product direction

RetroHub is an offline Android game shelf for people who already own ROMs and use standalone emulators. Its job is to turn a scattered set of files and emulator apps into one fast, console-like library.

The target experience is: open RetroHub, recognize the game by its cover, and start playing with one tap. Emulator configuration is setup work and should stay out of the way after the first successful launch.

## User and pain

- **Primary user:** an Android retro-gaming enthusiast with ROMs stored through Android's document provider and one or more installed emulators.
- **Current pain:** setup order is unclear, adding a ROM requires manual naming, launching document-provider URIs is unreliable, and common actions take extra taps.
- **First win:** one configured emulator and one game that launches successfully.

## MVP promise

1. Guide an empty-library user through emulator then game setup.
2. Accept modern `content://` ROM URIs and persist access.
3. Fill the game title from the selected filename.
4. Launch a saved game directly from the library.
5. Remain fully offline and never bundle ROMs or emulators.

## Success measures

- A new user can reach a playable library entry without guessing which screen comes first.
- A ROM selected through Android's document picker is passed to its emulator.
- Returning users can start a game from the library in one tap.
- Controls remain usable at phone and tablet widths and have 48 dp touch targets for primary icon actions.

## Anti-goals

- Shipping copyrighted games, BIOS files, or emulator binaries.
- Pretending every emulator accepts the same intent contract.
- Requiring accounts, cloud services, or connectivity for the core library.

## Next highest-value work

1. Emulator-specific launch profiles and verified intent contracts.
2. Multi-ROM import with duplicate detection and platform inference.
3. Optional local metadata/cover matching with an explicit privacy boundary.
4. TV/controller navigation and landscape layouts.
5. Backup/export of the local catalog.

## Decision

**Build.** The existing app already has the correct offline data model and launcher foundation. Reliability and first-run guidance have higher impact than adding more catalog screens.
