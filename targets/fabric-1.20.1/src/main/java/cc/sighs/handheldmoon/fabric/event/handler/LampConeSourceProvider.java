package cc.sighs.handheldmoon.fabric.event.handler;

import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.event.handler.PlacedLampConeSources;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Fabric 1.20.1 host for the placed-lamp cone sources. */
public final class LampConeSourceProvider {
    private LampConeSourceProvider() {
    }

    public static void append(Minecraft minecraft, List<RayConeRenderer.ConeSource> sources) {
        PlacedLampConeSources.append(minecraft, List.of(), sources);
    }
}
