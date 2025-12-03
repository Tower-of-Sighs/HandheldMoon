package com.sighs.handheldmoon.entity;

import com.sighs.handheldmoon.registry.Entities;
import com.sighs.handheldmoon.registry.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class FullMoonEntity extends ThrowableItemProjectile {
    private int radius = 16;

    public FullMoonEntity(Level p_37392_) {
        this(Entities.MOONLIGHT.get(), p_37392_);
    }

    public FullMoonEntity(EntityType<? extends FullMoonEntity> p_37391_, Level p_37392_) {
        super(p_37391_, p_37392_);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
//            if (!(level().getBlockEntity(blockPosition()) instanceof CameraStalkerBlockEntity)) {
//                discard();
//            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FULL_MOON.get();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        radius = tag.getByte("radius");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("radius", radius);
    }
}
