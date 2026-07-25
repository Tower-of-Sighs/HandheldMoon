package cc.sighs.handheldmoon.api.compat;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlashlightControllerRegistryTest {
    @Test
    void defaultsToNoOpAndDelegatesAfterInstallation() {
        FlashlightControllerRegistry<String> registry = new FlashlightControllerRegistry<>();
        AtomicReference<String> toggled = new AtomicReference<>();

        assertFalse(registry.isUsingFlashlight("player"));
        registry.toggleFlashlight("player");

        registry.install(new FlashlightController<String>() {
            @Override
            public boolean isUsingFlashlight(String player) {
                return player.startsWith("active");
            }

            @Override
            public void toggleFlashlight(String player) {
                toggled.set(player);
            }
        });

        assertTrue(registry.isUsingFlashlight("active-player"));
        registry.toggleFlashlight("active-player");
        assertTrue("active-player".equals(toggled.get()));
    }
}
