package cc.sighs.handheldmoon.compat;

import cc.sighs.handheldmoon.api.compat.FlashlightController;
import cc.sighs.handheldmoon.api.compat.FlashlightControllerRegistry;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class FlashlightCompatHooks {
    private static final FlashlightControllerRegistry<Player> CONTROLLERS = new FlashlightControllerRegistry<>();
    private static ToIntFunction<Player> itemLuminance = ignored -> 0;

    private FlashlightCompatHooks() {
    }

    public static void install(Predicate<Player> active, Consumer<Player> toggle) {
        Objects.requireNonNull(active);
        Objects.requireNonNull(toggle);
        CONTROLLERS.install(new FlashlightController<Player>() {
            @Override
            public boolean isUsingFlashlight(Player player) {
                return active.test(player);
            }

            @Override
            public void toggleFlashlight(Player player) {
                toggle.accept(player);
            }
        });
    }

    public static boolean isUsingFlashlight(Player player) {
        return CONTROLLERS.isUsingFlashlight(player);
    }

    public static void installItemLuminance(ToIntFunction<Player> callback) {
        itemLuminance = Objects.requireNonNull(callback, "callback");
    }

    public static int itemLuminance(Player player) {
        return Math.max(0, itemLuminance.applyAsInt(player));
    }

    public static void toggleFlashlight(Player player) {
        CONTROLLERS.toggleFlashlight(player);
    }
}
