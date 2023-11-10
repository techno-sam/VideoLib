package com.igrium.videolib.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.util.Identifier;

public class VideoLibConfig extends MidnightConfig {
    @Entry public static Implementation backend = Implementation.VLC;
    @Entry public static boolean showMissingNativesToast = true;

    public Identifier getImplementation() {
        return backend.id;
    }

    public enum Implementation {
        VLC(new Identifier("videolib", "vlcj"));

        Implementation(Identifier id) {
            this.id = id;
        }
        public final Identifier id;
    }
}
