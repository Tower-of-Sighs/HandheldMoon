package com.sighs.handheldmoon.util;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.companion.math.Pose3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nullable;

public class AeronauticsUtils {
    /**
     * A small, common-side data carrier for "where should this thing appear in the real world"
     * when it lives inside a physics sub-level.
     * <p>
     * Notes:
     * <ul>
     *     <li>{@link #renderPosition} is in parent/world coordinates (i.e., "projected out of sub-level").</li>
     *     <li>{@link #renderOrientation} is the sub-level's current pose orientation in parent/world space.</li>
     * </ul>
     */
    public record PhysicalizedRenderTransform(Vec3 renderPosition, Quaterniond renderOrientation, boolean physicalized) { }

    /**
     * @return the physics sub-level that currently contains {@code localPosition}, or {@code null} if none.
     *         {@code localPosition} is expressed in the coordinate space of {@code level} (which may be a sub-level).
     */
    public static @Nullable SubLevel getContainingPhysicsSubLevel(final Level level, final Vec3 localPosition) {
        return (SubLevel) Sable.HELPER.getContaining(level, new Vec3i((int) localPosition.x, (int) localPosition.y, (int) localPosition.z));
    }

    /**
     * @return true if {@code localPosition} is currently inside a physics sub-level.
     */
    public static boolean isPhysicalized(final Level level, final Vec3 localPosition) {
        return getContainingPhysicsSubLevel(level, localPosition) != null;
    }

    /**
     * Convenience overload: uses the block center.
     */
    public static boolean isPhysicalized(final Level level, final BlockPos localBlockPos) {
        return isPhysicalized(level, Vec3.atCenterOf(localBlockPos));
    }

    /**
     * Convenience overload: checks the block-entity's position.
     */
    public static boolean isPhysicalized(final BlockEntity blockEntity) {
        return Sable.HELPER.getContaining(blockEntity) != null;
    }

    /**
     * @return the containing sub-level's current pose (server: logical pose; client: also logical pose),
     *         or {@code null} if {@code localPosition} is not inside any sub-level.
     *
     * <p>If you need client-side interpolation, prefer calling {@code ((ClientSubLevel) subLevel).renderPose()}
     * on the client directly.</p>
     */
    public static @Nullable Pose3d getContainingPhysicsPose(final Level level, final Vec3 localPosition) {
        final SubLevel subLevel = getContainingPhysicsSubLevel(level, localPosition);
        return subLevel == null ? null : subLevel.logicalPose();
    }

    /**q
     * Projects a point out of any containing sub-level into parent/world coordinates.
     * If the point is not inside a sub-level, returns the original position (as a {@link Vec3}).
     */
    public static Vec3 getPhysicalizedRenderPosition(final Level level, final Vector3dc localPosition) {
        final Vector3d projected = Sable.HELPER.projectOutOfSubLevel(level, localPosition, new Vector3d());
        return JOMLConversion.toMojang(projected);
    }

    /**
     * Convenience overload.
     */
    public static Vec3 getPhysicalizedRenderPosition(final Level level, final Vec3 localPosition) {
        return getPhysicalizedRenderPosition(level, new Vector3d(localPosition.x, localPosition.y, localPosition.z));
    }

    /**
     * Convenience overload: uses the block center.
     */
    public static Vec3 getPhysicalizedRenderPosition(final Level level, final BlockPos localBlockPos) {
        return getPhysicalizedRenderPosition(level, JOMLConversion.atCenterOf(localBlockPos));
    }

    /**
     * Projects an offset point relative to a block's lower-corner origin (i.e., {@code (x,y,z)} in block-local units).
     * Example: use {@code new Vec3(0.5, 0.0, 0.5)} for the block center at ground level.
     */
    public static Vec3 getPhysicalizedRenderPosition(final Level level, final BlockPos localBlockPos, final Vec3 localOffsetFromBlockOrigin) {
        final Vector3d local = new Vector3d(
                localBlockPos.getX() + localOffsetFromBlockOrigin.x,
                localBlockPos.getY() + localOffsetFromBlockOrigin.y,
                localBlockPos.getZ() + localOffsetFromBlockOrigin.z
        );
        return getPhysicalizedRenderPosition(level, local);
    }

    /**
     * Convenience overload: uses the block-entity's block center.
     */
    public static @Nullable Vec3 getPhysicalizedRenderPosition(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) return null;
        return getPhysicalizedRenderPosition(level, blockEntity.getBlockPos());
    }

    /**
     * @return the tilt/orientation quaternion of the containing physics sub-level in parent/world space.
     * If {@code localPosition} is not in a sub-level, returns identity (no rotation).
     */
    public static Quaterniond getPhysicalizedRenderOrientation(final Level level, final Vec3 localPosition) {
        final Pose3dc pose = getContainingPhysicsPose(level, localPosition);
        return pose == null ? new Quaterniond() : new Quaterniond(pose.orientation());
    }

    /**
     * Convenience overload: uses the block center.
     */
    public static Quaterniond getPhysicalizedRenderOrientation(final Level level, final BlockPos localBlockPos) {
        final Vec3 center = Vec3.atCenterOf(localBlockPos);
        return getPhysicalizedRenderOrientation(level, center);
    }

    /**
     * Convenience overload: uses the block-entity's block center.
     */
    public static @Nullable Quaterniond getPhysicalizedRenderOrientation(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) return null;
        return getPhysicalizedRenderOrientation(level, blockEntity.getBlockPos());
    }

    /**
     * Returns both render position and tilt at once (for common "attach entity to a physicalized block" use cases).
     */
    public static PhysicalizedRenderTransform getPhysicalizedRenderTransform(final Level level, final Vector3dc localPosition) {
        final Vec3 localAsVec3 = JOMLConversion.toMojang(localPosition);
        final boolean physicalized = getContainingPhysicsSubLevel(level, localAsVec3) != null;
        return new PhysicalizedRenderTransform(
                getPhysicalizedRenderPosition(level, localPosition),
                getPhysicalizedRenderOrientation(level, localAsVec3),
                physicalized
        );
    }

    /**
     * Convenience overload: uses the block center.
     */
    public static PhysicalizedRenderTransform getPhysicalizedRenderTransform(final Level level, final BlockPos localBlockPos) {
        return getPhysicalizedRenderTransform(level, JOMLConversion.atCenterOf(localBlockPos));
    }

    /**
     * Convenience overload: uses the block-entity's block center.
     */
    public static @Nullable PhysicalizedRenderTransform getPhysicalizedRenderTransform(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) return null;
        return getPhysicalizedRenderTransform(level, blockEntity.getBlockPos());
    }
}
