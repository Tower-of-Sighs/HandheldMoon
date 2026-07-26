package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;

import java.util.Objects;
import java.util.function.Consumer;

/** Loader hook for refreshing a placed lamp's dynamic-light source after interaction. */
public final class LampInteractionHooks {
    private static Consumer<MoonlightLampBlockEntity> refresh = ignored -> {
    };

    private LampInteractionHooks() {
    }

    public static void install(Consumer<MoonlightLampBlockEntity> callback) {
        refresh = Objects.requireNonNull(callback, "callback");
    }

    public static void refresh(MoonlightLampBlockEntity lamp) {
        refresh.accept(lamp);
    }
}
