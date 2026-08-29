package cc.sighs.handheldmoon.api.light;

/**
 * Pluggable cache for per-block light values.
 * <p>
 * Cache misses are represented by {@link #CACHE_MISS}. Implementations must
 * accept primitive packed block-position keys so querying light does not box
 * coordinates on the chunk-rendering hot path.
 *
 * <pre>{@code
 * DynamicLightBuilder.cone()
 *     .range(24.0).luminance(12.0)
 *     .build(position, direction, active)
 *     .setLightCache(new MyCustomCache());
 * }</pre>
 *
 * @see cc.sighs.handheldmoon.api.light.impl.LightCacheImpl
 */
public interface LightCache {
    /** Sentinel returned by {@link #get(long)} when an entry is not cached. */
    double CACHE_MISS = Double.NaN;

    /**
     * Retrieve a cached light value.
     *
     * @param blockPosKey stable packed key of the query position
     * @return the cached value, or {@link #CACHE_MISS}
     */
    double get(long blockPosKey);

    /** Store a light value for a packed block position. */
    void put(long blockPosKey, double light);

    /** Clear all cached values (e.g. when the light source moves). */
    default void clear() {
    }

    /**
     * Return a shared no-op cache. This keeps cache configuration optional
     * without adding a nullable branch to API consumers.
     */
    static LightCache none() {
        return DisabledLightCache.INSTANCE;
    }
}

final class DisabledLightCache implements LightCache {
    static final DisabledLightCache INSTANCE = new DisabledLightCache();

    private DisabledLightCache() {
    }

    @Override
    public double get(long blockPosKey) {
        return CACHE_MISS;
    }

    @Override
    public void put(long blockPosKey, double light) {
    }
}
