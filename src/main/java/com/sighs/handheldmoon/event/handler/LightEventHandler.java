package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class LightEventHandler {

    private static void onClientTick(Minecraft client) {
        if (!Config.REAL_LIGHT.get()) return;

        var levelRenderer = client.levelRenderer;
        var player = client.player;
        if (player == null || client.level == null) return;

        Set<BlockPos.MutableBlockPos> posSet = new HashSet<>();

        Level level = client.level;
        for (Player clientPlayer : level.players()) {
            var entityChunkPos = clientPlayer.chunkPosition();
            int centerChunkX = entityChunkPos.x;
            int centerChunkZ = entityChunkPos.z;
            int centerSectionY = SectionPos.blockToSectionCoord(clientPlayer.getEyeY());
            int CHUNK_UPDATE_RADIUS = 2;

            for (int dx = -CHUNK_UPDATE_RADIUS; dx <= CHUNK_UPDATE_RADIUS; dx++) {
                for (int dz = -CHUNK_UPDATE_RADIUS; dz <= CHUNK_UPDATE_RADIUS; dz++) {
                    BlockPos.MutableBlockPos chunkPos = new BlockPos.MutableBlockPos(
                            centerChunkX + dx,
                            centerSectionY,
                            centerChunkZ + dz
                    );
                    if (shouldUpdateChunk(chunkPos, player)) {
                        posSet.add(chunkPos);
                    }
                }
            }

            if (Utils.isUsingFlashlight(clientPlayer)) {
                ((DynamicLightSource) clientPlayer).sdl$resetDynamicLight();
                ((DynamicLightSource) clientPlayer).sodiumdynamiclights$scheduleTrackedChunksRebuild(levelRenderer);
            }
        }

        for (BlockPos pos : posSet) {
            // 注意：scheduleChunkRebuild 应接收不可变 BlockPos
            SodiumDynamicLights.scheduleChunkRebuild(levelRenderer, pos);
        }
    }

    private static boolean shouldUpdateChunk(BlockPos chunkPos, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition(1.0F);

        double chunkCenterX = (chunkPos.getX() << 4) + 8.0;
        double chunkCenterY = (chunkPos.getY() << 4) + 8.0;
        double chunkCenterZ = (chunkPos.getZ() << 4) + 8.0;

        Vec3 toChunk = new Vec3(chunkCenterX - playerPos.x, chunkCenterY - playerPos.y, chunkCenterZ - playerPos.z).normalize();

        double dotProduct = lookVec.dot(toChunk);
        return dotProduct > 0.6;
    }
}
