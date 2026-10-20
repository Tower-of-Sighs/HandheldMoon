package cc.sighs.handheldmoon.fabric.client.screen;

import cc.sighs.handheldmoon.client.screen.AbstractLightSourceConfigScreen;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;

import java.util.UUID;

public final class LightSourceConfigScreen extends AbstractLightSourceConfigScreen {
    public LightSourceConfigScreen(
            FullMoonDynamicLightSource source, int entityId, UUID uuid, String sourceId
    ) {
        super(source, entityId, uuid, sourceId);
    }
}
