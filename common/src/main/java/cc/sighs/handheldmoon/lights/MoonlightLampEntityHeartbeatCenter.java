package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.util.TickHeartbeatCenter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class MoonlightLampEntityHeartbeatCenter {
    private static final TickHeartbeatCenter<ResourceKey<Level>, UUID> HEARTBEATS = new TickHeartbeatCenter<>();

    private MoonlightLampEntityHeartbeatCenter() {
    }

    public static void report(Level level, UUID uuid) {
        HEARTBEATS.report(level.dimension(), level.getGameTime(), uuid);
    }

    public static boolean isAlive(Level level, UUID uuid) {
        return HEARTBEATS.isAlive(level.dimension(), level.getGameTime(), uuid);
    }
}

