package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.light.EntityLightProfileAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Sets a light-source entity's profile from the wand GUI. Distances are
 * deliberately not checked: the wand is meant to tune any loaded source.
 */
public final class ServerSetLightProfilePacket implements ServerPacket {
    private final int entityId;
    private final EntityLightProfile profile;

    public ServerSetLightProfilePacket(int entityId, EntityLightProfile profile) {
        this.entityId = entityId;
        this.profile = profile;
    }

    public ServerSetLightProfilePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        Tag tag = buf.readNbt();
        this.profile = tag == null ? null
                : EntityLightProfile.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeNbt((CompoundTag) EntityLightProfile.CODEC
                .encodeStart(NbtOps.INSTANCE, profile).result().orElseThrow());
    }

    public void apply(ServerPlayer player) {
        if (profile == null) {
            return;
        }
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof EntityLightProfileAccess access) {
            access.setLightProfile(profile);
        }
    }
}
