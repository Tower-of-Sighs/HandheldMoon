package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HandheldMoon.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FullMoonEntity>> MOONLIGHT =
            ENTITY_TYPES.register("full_moon", () ->
                    EntityType.Builder.<FullMoonEntity>of(FullMoonEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build(HandheldMoon.id("full_moon").toString())
            );
}
