package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;

import java.util.Objects;
import java.util.function.Consumer;

/** Loader hook for refreshing a placed lamp's dynamic-light source after interaction. */
public final class LampInteractionHooks {
    private static Consumer<MoonlightLampBlockEntityAccess> refresh = ignored -> {
    };

    private LampInteractionHooks() {
    }

    public static void install(Consumer<MoonlightLampBlockEntityAccess> callback) {
        refresh = Objects.requireNonNull(callback, "callback");
    }

    public static void refresh(MoonlightLampBlockEntityAccess lamp) {
        refresh.accept(lamp);
    }
}
