package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;

import java.util.UUID;

/** A light-source entity selected by the moonlight wand. */
public record LightSourceTarget(
        FullMoonDynamicLightSource source,
        int entityId,
        UUID uuid,
        String sourceId
) {
}
