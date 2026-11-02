package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.light.EntityLightProfileAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Sets a light-source entity's profile from the wand GUI. Distances are
 * deliberately not checked: the wand is meant to tune any loaded source.
 */
public record ServerSetLightProfilePacket(int entityId, EntityLightProfile profile)
        implements CustomPacketPayload {
    public static final Type<ServerSetLightProfilePacket> TYPE =
            new Type<>(HandheldMoon.id("server_set_light_profile"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerSetLightProfilePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerSetLightProfilePacket::entityId,
                    ByteBufCodecs.fromCodec(EntityLightProfile.CODEC), ServerSetLightProfilePacket::profile,
                    ServerSetLightProfilePacket::new);

    public void apply(ServerPlayer player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof EntityLightProfileAccess access) {
            access.setLightProfile(profile);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
