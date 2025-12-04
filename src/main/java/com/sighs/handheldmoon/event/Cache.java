package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import toni.sodiumdynamiclights.DynamicLightSource;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class Cache {
    private static Set<DynamicLightSource> realLightSourceList = new HashSet<>();
    private static Set<DynamicLightSource> selfLightSourceList = new HashSet<>();
    private static Set<DynamicLightSource> rayLightSourceList = new HashSet<>();

    public static Set<DynamicLightSource> getRealLightSourceList() {
        return realLightSourceList;
    }

    public static Set<DynamicLightSource> getRayLightSourceList() {
        return rayLightSourceList;
    }

    public static Set<DynamicLightSource> getSelfLightSourceList() {
        return selfLightSourceList;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean realLight = Config.REAL_LIGHT.get();
        boolean playerRay = Config.PLAYER_RAY.get();

        Set<DynamicLightSource> _realLightSourceList = new HashSet<>();
        Set<DynamicLightSource> _selfLightSourceList = new HashSet<>();
        Set<DynamicLightSource> _rayLightSourceList = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player player) {
                DynamicLightSource lightSource = (DynamicLightSource) player;
                if (Utils.takeFlashlight(player) || player.getMainHandItem().is(Items.FULL_MOON.get()) || player.getOffhandItem().is(Items.FULL_MOON.get())) {
                    _selfLightSourceList.add(lightSource);
                    if (Utils.isUsingFlashlight(player)) {
                        if (!player.getUUID().equals(mc.player.getUUID())) {
                            if (playerRay) _rayLightSourceList.add(lightSource);
                        }
                        if (realLight) _realLightSourceList.add(lightSource);
                    }
                }
            }
            if (entity instanceof FullMoonEntity) {
                DynamicLightSource lightSource = (DynamicLightSource) entity;
                _selfLightSourceList.add(lightSource);
                if (realLight) _realLightSourceList.add(lightSource);
                var be = entity.level().getBlockEntity(entity.blockPosition());
                if (be instanceof MoonlightLampBlockEntity lamp) {
                    if (lamp.getPowered()) {
                        if (playerRay) _rayLightSourceList.add((DynamicLightSource) lamp);
                    }
                } else if (be instanceof FullMoonBlockEntity) {
                    _realLightSourceList.add(lightSource);
                }
            }
        }
        realLightSourceList = _realLightSourceList;
        selfLightSourceList = _selfLightSourceList;
        rayLightSourceList = _rayLightSourceList;
    }
}
