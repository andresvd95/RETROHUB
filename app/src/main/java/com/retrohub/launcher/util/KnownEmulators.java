package com.retrohub.launcher.util;

import java.util.ArrayList;
import java.util.List;

/** Curated catalog of known Android emulator packages. */
public class KnownEmulators {

    public static class Known {
        public final String name;
        public final String packageName;
        public final String platformId; // may be null for "multi"
        public Known(String name, String packageName, String platformId) {
            this.name = name;
            this.packageName = packageName;
            this.platformId = platformId;
        }
        @Override public String toString() { return name + "  ·  " + packageName; }
    }

    public static List<Known> all() {
        List<Known> list = new ArrayList<>();
        list.add(new Known("RetroArch", "com.retroarch", null));
        list.add(new Known("RetroArch Plus", "com.retroarch.aarch64", null));
        list.add(new Known("PPSSPP", "org.ppsspp.ppsspp", "psp"));
        list.add(new Known("PPSSPP Gold", "org.ppsspp.ppssppgold", "psp"));
        list.add(new Known("Dolphin", "org.dolphinemu.dolphinemu", "gcn"));
        list.add(new Known("Citra (3DS)", "org.citra_emu.citra", null));
        list.add(new Known("Yuzu / Suyu", "org.yuzu.yuzu_emu", null));
        list.add(new Known("RPCS3 / Volt", "com.rpcs3.rpcs3android", "ps3"));
        list.add(new Known("AetherSX2", "xyz.aethersx2.android", "ps2"));
        list.add(new Known("ePSXe", "com.epsxe.ePSXe", "ps1"));
        list.add(new Known("FPse", "com.emulator.fpse", "ps1"));
        list.add(new Known("DraStic DS", "com.dsemu.drastic", "nds"));
        list.add(new Known("ClassicBoy", "com.portableandroid.classicboy", null));
        list.add(new Known("Winlator", "com.winlator", "winlator"));
        list.add(new Known("GameHub", "com.github.dotsworthy.gamehub", "gamehub"));
        list.add(new Known("MAME4droid", "com.seleuco.mame4droid", "arcade_mame"));
        list.add(new Known("MD.emu", "com.explusalpha.MdEmu", "sega_genesis"));
        list.add(new Known("Saturn.emu", "com.explusalpha.SaturnEmu", "saturn"));
        list.add(new Known("Reicast", "com.reicast.emulator", "dreamcast"));
        list.add(new Known("Redream", "io.recompiled.redream", "dreamcast"));
        return list;
    }
}
