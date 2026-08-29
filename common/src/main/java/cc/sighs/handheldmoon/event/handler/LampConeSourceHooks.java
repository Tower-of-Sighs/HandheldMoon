package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Loader hook for collecting placed-lamp cone sources from the version-specific world model. */
public final class LampConeSourceHooks {
    private static BiConsumer<Minecraft, List<RayConeRenderer.ConeSource>> appender = (ignored, sources) -> {
    };

    private LampConeSourceHooks() {
    }

    public static void install(BiConsumer<Minecraft, List<RayConeRenderer.ConeSource>> callback) {
        appender = Objects.requireNonNull(callback, "callback");
    }

    public static void append(Minecraft minecraft, List<RayConeRenderer.ConeSource> sources) {
        appender.accept(minecraft, sources);
    }
}
