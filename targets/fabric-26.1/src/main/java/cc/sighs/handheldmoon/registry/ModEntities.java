package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.entity.FullMoonEntity;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<EntityType<FullMoonEntity>> MOONLIGHT =
            ENTITY_TYPES.register("full_moon", () ->
                    EntityType.Builder.<FullMoonEntity>of(FullMoonEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, HandheldMoon.id("full_moon")))
            );
}
