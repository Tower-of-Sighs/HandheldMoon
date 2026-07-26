package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.api.raycone.impl.RayConeRendererImpl;
import cc.sighs.handheldmoon.compat.accessory.AccessoryCompat;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.event.handler.BlockEntityLampConeSources;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.event.handler.LampInteractionHooks;
import cc.sighs.handheldmoon.event.handler.ShaderEventHandler;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.ModKeyBindings;
import cc.sighs.handheldmoon.network.ClientNetworkHooks;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import net.minecraft.client.Minecraft;

public final class HandheldMoonClient {
    private HandheldMoonClient() {
    }

    public static void initClient() {
        RayConeRenderer.installBackend(RayConeRendererImpl::render);
        LampConeSourceHooks.install((minecraft, sources) -> BlockEntityLampConeSources.append(
                minecraft, HandheldMoonDynamicLightsInitializer.getActiveLampPositions(), sources
        ));
        LampInteractionHooks.install(HandheldMoonDynamicLightsInitializer::syncLampBehavior);
        ClientNetworkHooks.installLampState(lamp ->
                new ServerMoonLightLampSyncPacket(lamp.getBlockPos(), lamp.getXRot(), lamp.getYRot(), lamp.getPowered()).sendToServer()
        );
        ClientNetworkHooks.installDeviceConfigSenders(
                (pos, config) -> new ServerMoonlightLampConfigSyncPacket(pos, config).sendToServer(),
                (pos, config) -> new ServerFullMoonConfigSyncPacket(pos, config).sendToServer(),
                (hand, config) -> new ServerHeldMoonlightLampConfigSyncPacket(hand == net.minecraft.world.InteractionHand.OFF_HAND ? 1 : 0, config).sendToServer(),
                (hand, config) -> new ServerHeldFullMoonConfigSyncPacket(hand == net.minecraft.world.InteractionHand.OFF_HAND ? 1 : 0, config).sendToServer()
        );
        ModKeyBindings.register();
        AccessoryCompat.init();
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (DynamicLightManager.syncLevel(minecraft)) {
            HandheldMoonDynamicLightsInitializer.reset();
        }
        HandheldMoonDynamicLightsInitializer.updatePlayerBehaviors();
        HandheldMoonDynamicLightsInitializer.updateItemBehaviors();
        DynamicLightManager.tick(minecraft);
        ShaderEventHandler.onClientTick();
    }
}
