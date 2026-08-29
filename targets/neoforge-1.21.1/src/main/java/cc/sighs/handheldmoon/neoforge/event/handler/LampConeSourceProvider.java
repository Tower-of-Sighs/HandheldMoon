package cc.sighs.handheldmoon.neoforge.event.handler;

import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.event.handler.BlockEntityLampConeSources;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class LampConeSourceProvider {
    private LampConeSourceProvider() {
    }

    public static void append(Minecraft minecraft, List<RayConeRenderer.ConeSource> sources) {
        BlockEntityLampConeSources.append(minecraft, List.of(), sources);
    }
}
