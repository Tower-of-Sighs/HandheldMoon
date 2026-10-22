package cc.sighs.handheldmoon.spi;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.client.ClientRuntime;
import cc.sighs.handheldmoon.client.screen.HandheldMoonConfigScreen;
import cc.sighs.handheldmoon.client.screen.LightSourceConfigScreen;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.event.handler.ShaderEventHandler;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerOpenFlashlightConfigPacket;
import cc.sighs.handheldmoon.network.ServerOpenLampBlockConfigPacket;
import cc.sighs.handheldmoon.network.ServerSetLightProfilePacket;
import cc.sighs.handheldmoon.registry.ModKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

/**
 * Client-side host logic shared by every target.
 *
 * <p>Targets keep only the concrete loader boundary: the packet transport
 * ({@link #send(CustomPacketPayload)}), key-binding/menu-screen registration and
 * whatever the loader's client initializer needs to call.
 */
public abstract class AbstractClientPlatformService implements ClientPlatformService {
    /** Sends a payload to the server through the loader's client networking API. */
    protected abstract void send(CustomPacketPayload payload);

    @Override
    public void tickClient() {
        ClientRuntime.tick();
        ShaderEventHandler.onClientTick();
    }

    @Override
    public int flashlightKeyCode() {
        return ModKeyBindings.FLASHLIGHT_SWITCH.getDefaultKey().getValue();
    }

    @Override
    public int coneSwitchKeyCode() {
        return ModKeyBindings.CONE_SWITCH.getDefaultKey().getValue();
    }

    @Override
    public int modConfigKeyCode() {
        return ModKeyBindings.OPEN_MOD_CONFIG.getDefaultKey().getValue();
    }

    @Override
    public boolean isFlashlightKeyDown() {
        return ModKeyBindings.FLASHLIGHT_SWITCH.isDown();
    }

    @Override
    public void requestOpenFlashlightConfig() {
        FlashlightConfigMenu.clearPendingBlockTarget();
        send(new ServerOpenFlashlightConfigPacket());
    }

    @Override
    public void requestOpenLampConfig(BlockPos pos) {
        FlashlightConfigMenu.expectBlockTarget(pos);
        send(new ServerOpenLampBlockConfigPacket(pos));
    }

    @Override
    public void openModConfig() {
        Minecraft.getInstance().setScreen(new HandheldMoonConfigScreen());
    }

    @Override
    public void openLightSourceConfig(
            FullMoonDynamicLightSource source, int entityId, UUID uuid, String sourceId) {
        Minecraft.getInstance().setScreen(new LightSourceConfigScreen(source, entityId, uuid, sourceId));
    }

    @Override
    public void sendLightProfile(int entityId, EntityLightProfile profile) {
        send(new ServerSetLightProfilePacket(entityId, profile));
    }

    @Override
    public void sendLampState(BlockPos pos, float xRot, float yRot, boolean powered) {
        send(new ServerMoonLightLampSyncPacket(pos, xRot, yRot, powered));
    }

    @Override
    public void sendLampConfig(BlockPos pos, LampDeviceConfig config) {
        send(new ServerMoonlightLampConfigSyncPacket(pos, config));
    }

    @Override
    public void sendFullMoonConfig(BlockPos pos, FullMoonDeviceConfig config) {
        send(new ServerFullMoonConfigSyncPacket(pos, config));
    }

    @Override
    public void sendHeldLampConfig(InteractionHand hand, LampDeviceConfig config) {
        send(new ServerHeldMoonlightLampConfigSyncPacket(hand == InteractionHand.OFF_HAND ? 1 : 0, config));
    }

    @Override
    public void sendHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config) {
        send(new ServerHeldFullMoonConfigSyncPacket(hand == InteractionHand.OFF_HAND ? 1 : 0, config));
    }
}
