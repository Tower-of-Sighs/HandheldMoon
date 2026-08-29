package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraft.world.entity.MobCategory;
import cc.sighs.handheldmoon.spi.RegistrySupplier;

public final class ModEntities {
    public static final RegistrySupplier<?> MOONLIGHT =
            HandheldMoon.registry().registerFullMoonEntity("full_moon", MobCategory.MISC, 0.5F, 0.5F);

    private ModEntities() {
    }
}
