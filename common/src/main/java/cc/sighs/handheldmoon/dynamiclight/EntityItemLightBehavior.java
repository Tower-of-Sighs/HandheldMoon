package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.IntSupplier;

/** Point light attached to an entity and driven by an explicit item luminance supplier. */
public final class EntityItemLightBehavior implements DynamicLightBehavior {
    private static final double RANGE = 15.0;

    private final Entity entity;
    private final IntSupplier luminanceSupplier;
    private RayLightBehavior delegate;
    private int lastLuminance;

    public EntityItemLightBehavior(Entity entity, IntSupplier luminanceSupplier) {
        this.entity = entity;
        this.luminanceSupplier = luminanceSupplier;
        this.lastLuminance = luminanceSupplier.getAsInt();
        this.delegate = createDelegate(lastLuminance);
    }

    private RayLightBehavior createDelegate(int luminance) {
        return new RayLightBehavior(
                DynamicLightBuilder.point()
                        .range(RANGE)
                        .luminance(luminance)
                        .occlusion(false)
                        .buildConfig(),
                () -> entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0),
                () -> Vec3.ZERO,
                () -> !entity.isRemoved() && luminanceSupplier.getAsInt() > 0
        );
    }

    @Override
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        return delegate.lightAt(blockX, blockY, blockZ, falloffRatio);
    }

    @Override
    public Bounds getBounds() {
        return delegate.getBounds();
    }

    @Override
    public boolean hasChanged() {
        int luminance = luminanceSupplier.getAsInt();
        if (luminance != lastLuminance) {
            lastLuminance = luminance;
            delegate = createDelegate(luminance);
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        return entity.isRemoved() || luminanceSupplier.getAsInt() <= 0;
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        return delegate.getBatchLightSnapshot();
    }
}
