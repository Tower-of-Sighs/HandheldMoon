package com.sighs.handheldmoon.util;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nullable;

/**
 * Optional bridge for Aeronautics/Sable physics sub-level integration.
 * <p>
 * All Sable API calls are isolated in the inner class {@link SableInternal},
 * which is only loaded after confirming Sable is present on the classpath.
 * If Sable is not installed, every method returns a sensible default
 * (false, null, identity quaternion, or the local position unchanged).
 */
public class AeronauticsUtils {

    public record PhysicalizedRenderTransform(Vec3 renderPosition, Quaterniond renderOrientation, boolean physicalized) {
    }

    private static final boolean AVAILABLE;

    static {
        boolean flag = false;
        try {
            Class.forName("dev.ryanhcode.sable.Sable");
            flag = true;
        } catch (ClassNotFoundException e) {
            flag = false;
        }
        AVAILABLE = flag;
    }

    private static boolean isAvailable() {
        return AVAILABLE;
    }

    // ==============================================================
    //  Public API — all Sable-free signatures
    // ==============================================================

    public static boolean isPhysicalized(final Level level, final Vec3 localPosition) {
        if (!isAvailable()) return false;
        return SableInternal.isInsideSubLevel(level, localPosition);
    }

    public static boolean isPhysicalized(final Level level, final BlockPos localBlockPos) {
        return isPhysicalized(level, Vec3.atCenterOf(localBlockPos));
    }

    public static boolean isPhysicalized(final BlockEntity blockEntity) {
        if (!isAvailable()) return false;
        return SableInternal.isInsideSubLevel(blockEntity);
    }

    public static boolean isPhysicalized(final Entity entity) {
        if (!isAvailable()) return false;
        return SableInternal.isInsideSubLevel(entity);
    }

    public static Vec3 getPhysicalizedRenderPosition(final Level level, final Vector3dc localPosition) {
        if (!isAvailable()) return toMojang(localPosition);
        return SableInternal.projectOutOfSubLevel(level, localPosition);
    }

    public static Vec3 getPhysicalizedRenderPosition(final Level level, final Vec3 localPosition) {
        return getPhysicalizedRenderPosition(level, new Vector3d(localPosition.x, localPosition.y, localPosition.z));
    }

    public static Vec3 getPhysicalizedRenderPosition(final Level level, final BlockPos localBlockPos) {
        return getPhysicalizedRenderPosition(level, atCenterOf(localBlockPos));
    }

    public static Vec3 getPhysicalizedRenderPosition(final Level level, final BlockPos localBlockPos, final Vec3 localOffsetFromBlockOrigin) {
        final Vector3d local = new Vector3d(
                localBlockPos.getX() + localOffsetFromBlockOrigin.x,
                localBlockPos.getY() + localOffsetFromBlockOrigin.y,
                localBlockPos.getZ() + localOffsetFromBlockOrigin.z
        );
        return getPhysicalizedRenderPosition(level, local);
    }

    @Nullable
    public static Vec3 getPhysicalizedRenderPosition(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) return null;
        return getPhysicalizedRenderPosition(level, blockEntity.getBlockPos());
    }

    public static Quaterniond getPhysicalizedRenderOrientation(final Level level, final Vec3 localPosition) {
        if (!isAvailable()) return new Quaterniond();
        return SableInternal.getOrientation(level, localPosition);
    }

    public static Quaterniond getPhysicalizedRenderOrientation(final Level level, final BlockPos localBlockPos) {
        return getPhysicalizedRenderOrientation(level, Vec3.atCenterOf(localBlockPos));
    }

    @Nullable
    public static Quaterniond getPhysicalizedRenderOrientation(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) return null;
        return getPhysicalizedRenderOrientation(level, blockEntity.getBlockPos());
    }

    public static PhysicalizedRenderTransform getPhysicalizedRenderTransform(final Level level, final Vector3dc localPosition) {
        final Vec3 localAsVec3 = toMojang(localPosition);
        final boolean physicalized = isPhysicalized(level, localAsVec3);
        return new PhysicalizedRenderTransform(
                getPhysicalizedRenderPosition(level, localPosition),
                getPhysicalizedRenderOrientation(level, localAsVec3),
                physicalized
        );
    }

    public static PhysicalizedRenderTransform getPhysicalizedRenderTransform(final Level level, final BlockPos localBlockPos) {
        return getPhysicalizedRenderTransform(level, atCenterOf(localBlockPos));
    }

    @Nullable
    public static PhysicalizedRenderTransform getPhysicalizedRenderTransform(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) return null;
        return getPhysicalizedRenderTransform(level, blockEntity.getBlockPos());
    }

    // ==============================================================
    //  Internal helpers (JOML only — no Sable dependency)
    // ==============================================================

    private static Vec3 toMojang(Vector3dc v) {
        return new Vec3(v.x(), v.y(), v.z());
    }

    private static Vector3d atCenterOf(BlockPos pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    // ==============================================================
    //  Sable-dependent implementations
    //
    //  This inner class is ONLY loaded by the JVM on first access.
    //  Since every outer method guards with isAvailable() first, this
    //  class is never loaded when Sable is absent, avoiding
    //  NoClassDefFoundError.
    // ==============================================================

    private static class SableInternal {

        private static boolean isInsideSubLevel(final Level level, final Vec3 localPosition) {
            return Sable.HELPER.getContaining(
                    level,
                    new Vec3i((int) localPosition.x, (int) localPosition.y, (int) localPosition.z)
            ) != null;
        }

        private static boolean isInsideSubLevel(final BlockEntity blockEntity) {
            return Sable.HELPER.getContaining(blockEntity) != null;
        }

        private static boolean isInsideSubLevel(final Entity entity) {
            return Sable.HELPER.getContaining(entity) != null;
        }

        private static Vec3 projectOutOfSubLevel(final Level level, final Vector3dc localPosition) {
            final var projected = Sable.HELPER.projectOutOfSubLevel(level, localPosition, new Vector3d());
            return JOMLConversion.toMojang(projected);
        }

        private static Quaterniond getOrientation(final Level level, final Vec3 localPosition) {
            final var raw = Sable.HELPER.getContaining(
                    level,
                    new Vec3i((int) localPosition.x, (int) localPosition.y, (int) localPosition.z)
            );
            if (raw instanceof SubLevel sl) {
                final var pose = sl.logicalPose();
                return new Quaterniond(pose.orientation());
            }
            return new Quaterniond();
        }
    }
}
