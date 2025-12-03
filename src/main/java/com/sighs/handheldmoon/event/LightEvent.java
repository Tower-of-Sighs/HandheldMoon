package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.init.ClientUtils;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class LightEvent {
    private static final Set<DynamicLightSource> lightSourceList = new HashSet<>();

    public static Set<DynamicLightSource> getLightSourceList() {
        return lightSourceList;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Config.REAL_LIGHT.get()) return;
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        lightSourceList.clear();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player player) {
                if (Utils.isUsingFlashlight(player)) {
                    lightSourceList.add((DynamicLightSource) player);
                }
            }
            if (entity instanceof FullMoonEntity) {
                var be = entity.level().getBlockEntity(entity.blockPosition());
                if (be instanceof MoonlightLampBlockEntity lamp) {
                    if (lamp.getPowered()) lightSourceList.add((DynamicLightSource) lamp);
                } else if (be instanceof FullMoonBlockEntity) {
                    lightSourceList.add((DynamicLightSource) be);
                }
            }
        }

        LevelRenderer levelRenderer = mc.levelRenderer;
        Set<BlockPos> posSet = new HashSet<>();

        for (DynamicLightSource dynamicLightSource : lightSourceList) {
            ChunkPos lightChunkPos = null;
            int centerSectionY = 0;

            if (dynamicLightSource instanceof Entity entity) {
                lightChunkPos = entity.chunkPosition();
                centerSectionY = SectionPos.blockToSectionCoord(entity.getEyeY());
            } else if (dynamicLightSource instanceof BlockEntity be) {
                lightChunkPos = new ChunkPos(be.getBlockPos());
                centerSectionY = SectionPos.blockToSectionCoord(be.getBlockPos().getY());
            }
            if (lightChunkPos == null) continue;

            int centerChunkX = lightChunkPos.x;
            int centerChunkZ = lightChunkPos.z;
            int CHUNK_UPDATE_RADIUS = 2;

            System.out.print(new BlockPos(
                    centerChunkX,
                    centerSectionY,
                    centerChunkZ
            )+"\n");

            for (int dx = -CHUNK_UPDATE_RADIUS; dx <= CHUNK_UPDATE_RADIUS; dx++) {
                for (int dz = -CHUNK_UPDATE_RADIUS; dz <= CHUNK_UPDATE_RADIUS; dz++) {
                    BlockPos blockPos = new BlockPos(
                            centerChunkX + dx,
                            centerSectionY,
                            centerChunkZ + dz
                    );
                    posSet.add(blockPos);
                }
            }
            dynamicLightSource.sdl$resetDynamicLight();
        }

        for (BlockPos pos : posSet) {
            SodiumDynamicLights.scheduleChunkRebuild(levelRenderer, pos);
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

        // 归一化方向向量
        toChunk = toChunk.normalize();

        // 计算视线方向与区块方向的点积
        double dotProduct = lookVec.dot(toChunk);

        // 这个阈值可以调整：值越大，视野范围越窄；值越小，视野范围越宽
        return dotProduct > 0.6;
    }
}
