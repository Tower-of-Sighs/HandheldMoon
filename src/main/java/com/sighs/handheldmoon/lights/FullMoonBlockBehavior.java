package com.sighs.handheldmoon.lights;

import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FullMoonBlockBehavior implements DynamicLightBehavior {
    private final BlockPos pos;
    private static final double RANGE = 18.0;

    public FullMoonBlockBehavior(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        double dx = query.getX() + 0.5 - (pos.getX() + 0.5);
        double dy = query.getY() + 0.5 - (pos.getY() + 0.5);
        double dz = query.getZ() + 0.5 - (pos.getZ() + 0.5);
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = RANGE * RANGE;

        if (distSq > rangeSq) return 0.0;

        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double t3 = (distSq * dist) / (RANGE * RANGE * RANGE);
        double distanceMultiplier = 1.0 - t3;
        double luminance = 15.0;
        return luminance * distanceMultiplier;
    }

    @Override
    public BoundingBox getBoundingBox() {
        int r = (int) Math.ceil(RANGE);
        return new BoundingBox(
                pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                pos.getX() + r, pos.getY() + r, pos.getZ() + r
        );
    }

    @Override
    public boolean hasChanged() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return true;
        var be = level.getBlockEntity(pos);
        return !(be instanceof FullMoonBlockEntity);
    }

    @Override
    public boolean isRemoved() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return true;
        BlockEntity be = level.getBlockEntity(pos);
        return !(be instanceof FullMoonBlockEntity);
    }
}