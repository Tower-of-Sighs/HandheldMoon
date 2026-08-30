package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.light.EntityLightRuntimeState;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import net.minecraft.world.phys.Vec3;

/** Shared light calculation for full-moon and lamp entities. */
public final class FullMoonEntityLightBehavior implements DynamicLightBehavior {
    private final FullMoonDynamicLightSource source;
    private RayLightBehavior delegate;
    private EntityLightProfile lastProfile;
    private boolean lastEnabled;

    public FullMoonEntityLightBehavior(FullMoonDynamicLightSource source) {
        this.source = source;
        refreshDelegate();
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
        EntityLightProfile profile = source.getLightProfile();
        EntityLightRuntimeState runtime = source.getLightRuntimeState();
        if (!profile.equals(lastProfile) || runtime.enabled() != lastEnabled) {
            refreshDelegate();
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        EntityLightRuntimeState runtime = source.getLightRuntimeState();
        return source.isLightRemoved()
                || source.getAnchorPos() == null
                || !runtime.enabled()
                || !source.getLightProfile().realLight();
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        return delegate.getBatchLightSnapshot();
    }

    private void refreshDelegate() {
        lastProfile = source.getLightProfile();
        EntityLightRuntimeState runtime = source.getLightRuntimeState();
        lastEnabled = runtime.enabled();
        delegate = createDelegate(lastProfile);
    }

    private RayLightBehavior createDelegate(EntityLightProfile profile) {
        if (profile.shape() == EntityLightProfile.Shape.CONE) {
            return new RayLightBehavior(
                    DynamicLightBuilder.cone()
                            .range(profile.range())
                            .angle(profile.innerAngle(), profile.outerAngle())
                            .luminance(profile.luminance())
                            .occlusion(profile.occlusion())
                            .buildConfig(),
                    () -> lightPosition(profile),
                    () -> source.getLightRuntimeState().direction(),
                    () -> profile.realLight() && source.getLightRuntimeState().enabled()
            );
        }

        return new RayLightBehavior(
                DynamicLightBuilder.point()
                        .range(profile.range())
                        .luminance(profile.luminance())
                        .occlusion(profile.occlusion())
                        .buildConfig(),
                () -> lightPosition(profile),
                () -> Vec3.ZERO,
                () -> profile.realLight() && source.getLightRuntimeState().enabled()
        );
    }

    private Vec3 lightPosition(EntityLightProfile profile) {
        return source.getLightRuntimeState().position().add(profile.positionOffset());
    }
}
