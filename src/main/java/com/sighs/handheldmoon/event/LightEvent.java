package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.init.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class LightEvent {
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        if (player != null) {
            for (AbstractClientPlayer clientPlayer : Minecraft.getInstance().level.players()) {
                if (Utils.isUsingFlashlight(player)) try {
                    Field field = Player.class.getDeclaredField("sodiumdynamiclights$luminance");
                    field.setAccessible(true);
                    field.setInt(clientPlayer, 15);
                } catch (NoSuchFieldException | IllegalAccessException ignored) { }
                SodiumDynamicLights.get().addLightSource((DynamicLightSource) clientPlayer);
            }
            ChunkPos entityChunkPos = player.chunkPosition();
            int centerChunkX = entityChunkPos.x;
            int centerChunkZ = entityChunkPos.z;
            int centerSectionY = SectionPos.blockToSectionCoord(player.getEyeY());
            int CHUNK_UPDATE_RADIUS = 2;

            for (int dx = -CHUNK_UPDATE_RADIUS; dx <= CHUNK_UPDATE_RADIUS; dx++) {
                for (int dz = -CHUNK_UPDATE_RADIUS; dz <= CHUNK_UPDATE_RADIUS; dz++) {
                    BlockPos.MutableBlockPos chunkPos = new BlockPos.MutableBlockPos(
                            centerChunkX + dx,
                            centerSectionY,
                            centerChunkZ + dz
                    );
                    if (shouldUpdateChunk(chunkPos)) {
                        SodiumDynamicLights.scheduleChunkRebuild(levelRenderer, chunkPos);
                    }
                }
            }
            ((DynamicLightSource) player).sodiumdynamiclights$scheduleTrackedChunksRebuild(levelRenderer);
            ((DynamicLightSource) player).sdl$resetDynamicLight();
        }
    }

    private static boolean shouldUpdateChunk(BlockPos.MutableBlockPos chunkPos) {
        Player player = Minecraft.getInstance().player;
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition(1.0F);

        double chunkCenterX = (chunkPos.getX() << 4) + 8;
        double chunkCenterY = (chunkPos.getY() << 4) + 8;
        double chunkCenterZ = (chunkPos.getZ() << 4) + 8;

        // 计算从玩家到区块的方向向量
        Vec3 toChunk = new Vec3(chunkCenterX - playerPos.x, chunkCenterY - playerPos.y, chunkCenterZ - playerPos.z);

        double distance = toChunk.length();
        if (distance > 64.0) { // 64格外的区块不更新
            return false;
        }

        // 归一化方向向量
        toChunk = toChunk.normalize();

        // 计算视线方向与区块方向的点积
        double dotProduct = lookVec.dot(toChunk);

        // 点积大于0.5表示区块在玩家前方约60度锥形范围内
        // 这个阈值可以调整：值越大，视野范围越窄；值越小，视野范围越宽
        return dotProduct > 0.5;
    }
}
