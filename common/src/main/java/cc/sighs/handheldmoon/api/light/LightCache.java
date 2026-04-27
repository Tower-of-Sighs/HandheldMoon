package cc.sighs.handheldmoon.api.light;

import java.util.function.DoubleSupplier;

/**
 * Pluggable cache for per-block light values.
 * <p>
 * The default implementation uses a fixed-capacity {@link java.util.concurrent.ConcurrentHashMap}
 * that clears when full. Implement this interface to supply your own strategy
 * (e.g. LRU eviction, timed expiry, spatial partitioning).
 *
 * <pre>{@code
 * DynamicLightBuilder.cone()
 *     .range(24.0).luminance(12.0)
 *     .build(position, direction, active)
 *     .setCache(new MyCustomCache());
 * }</pre>
 *
 * @see cc.sighs.handheldmoon.api.light.impl.LightCacheImpl
 */
@FunctionalInterface
public interface LightCache {
    /**
     * Retrieve a cached light value or compute and store it.
     *
     * @param blockPosKey  {@link net.minecraft.core.BlockPos#asLong()} of the query position
     * @param computer     supplier to compute the value on cache miss
     * @return the light value for the given position
     */
    double getOrCompute(long blockPosKey, DoubleSupplier computer);

    /** Clear all cached values (e.g. when the light source moves). */
    default void clear() {
    }
}
