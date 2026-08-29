package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.api.light.EntityLightSourceController;
import net.minecraft.client.Minecraft;

/** Installs the built-in entity light providers into the shared controller. */
public final class HandheldMoonDynamicLightsInitializer {
    private static final EntityDynamicLightTracker ENTITY_TRACKER = new EntityDynamicLightTracker();
    private static final FullMoonEntityDynamicLightTracker FULL_MOON_TRACKER = new FullMoonEntityDynamicLightTracker();
    private static final EntityLightSourceController CONTROLLER = createController();

    private HandheldMoonDynamicLightsInitializer() {
    }

    private static EntityLightSourceController createController() {
        EntityDynamicLightController controller = new EntityDynamicLightController();
        controller.register(ENTITY_TRACKER);
        controller.register(FULL_MOON_TRACKER);
        return controller;
    }

    public static void reset() {
        CONTROLLER.reset();
    }

    /** Returns the shared controller so integrations can register entity light providers. */
    public static EntityLightSourceController controller() {
        return CONTROLLER;
    }

    /**
     * Kept as a compatibility hook for loader interaction events. Placed lamps
     * are represented by their synchronized entity and discovered on the next tick.
     */
    public static void syncLampBehavior(MoonlightLampBlockEntityAccess lamp) {
    }

    public static void updateEntityBehaviors() {
        CONTROLLER.update(Minecraft.getInstance());
    }

    /** Compatibility entry point for integrations that update player lights directly. */
    public static void updatePlayerBehaviors() {
        updateEntityBehaviors();
    }

    /** Compatibility entry point for integrations that update full-moon lights directly. */
    public static void updateFullMoonEntityBehaviors() {
        updateEntityBehaviors();
    }

    /** Compatibility entry point for integrations that update dropped-item lights directly. */
    public static void updateItemBehaviors() {
        updateEntityBehaviors();
    }
}
