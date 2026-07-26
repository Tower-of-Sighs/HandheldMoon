package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.raycone.LampConeTransformHooks;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.util.LineLightMath;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Common block-entity implementation used by versions whose lamps remain blocks. */
public final class BlockEntityLampConeSources {
    private BlockEntityLampConeSources() {
    }

    public static void append(
            Minecraft minecraft,
            Iterable<BlockPos> positions,
            List<RayConeRenderer.ConeSource> sources
    ) {
        if (minecraft.level == null) {
            return;
        }
        for (BlockPos pos : positions) {
            var blockEntity = minecraft.level.getBlockEntity(pos);
            if (blockEntity instanceof MoonlightLampBlockEntity lamp && lamp.getPowered()) {
                Vec3 direction = LineLightMath.computeDirection(
                        lamp.getYRot(), lamp.getXRot() - 90.0f, true
                ).normalize().scale(-1);
                Vec3 apex = pos.getCenter().add(direction.scale(0.24));
                LampConeTransformHooks.LampCone transformed = LampConeTransformHooks.transform(lamp, apex, direction);
                sources.add(new RayConeRenderer.ConeSource(
                        transformed.apex(),
                        transformed.direction(),
                        RayEvent.buildLampConeConfig(lamp.getLampConfig()),
                        true
                ));
            }
        }
    }
}
