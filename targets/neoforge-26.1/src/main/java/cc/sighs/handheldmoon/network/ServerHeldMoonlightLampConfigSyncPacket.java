package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@NetworkPacket(modId = HandheldMoon.MOD_ID, id = "server_held_moonlight_lamp_config_sync", side = Side.BOTH)
public record ServerHeldMoonlightLampConfigSyncPacket(
        int handId,
        LampDeviceConfig config
) implements INetworkPacket<ServerHeldMoonlightLampConfigSyncPacket> {
    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(() -> {
            if (context.sender() == null) {
                return;
            }
            InteractionHand hand = handId == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack stack = context.sender().getItemInHand(hand);
            if (!stack.is(ModItems.MOONLIGHT_LAMP.get())) {
                return;
            }
            stack.set(ModDataComponent.LAMP_CONFIG.get(), config);
            context.sender().setItemInHand(hand, stack);
        });
    }
}
