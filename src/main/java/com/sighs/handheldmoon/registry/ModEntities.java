package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.util.RegisterHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static EntityType<FullMoonEntity> MOONLIGHT;

    public static void init() {
        MOONLIGHT = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "full_moon"),
                EntityType.Builder.<FullMoonEntity>of(FullMoonEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F)
                        .build(RegisterHelper.entityKey("full_moon"))
        );
    }
}
