package com.sighs.handheldmoon.registry;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sighs.handheldmoon.HandheldMoon.MODID;

public class Entities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<FullMoonEntity>> MOONLIGHT = ENTITIES.register(
            "full_moon",
            () -> EntityType.Builder.<FullMoonEntity>of(FullMoonEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build(new ResourceLocation(MODID, "full_moon").toString())
    );
}
