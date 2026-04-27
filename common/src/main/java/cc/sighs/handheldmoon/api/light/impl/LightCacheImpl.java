package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.LightCache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;

/**
 * Default {@link LightCache} implementation backed by a
 * {@link ConcurrentHashMap} with a capacity limit.
 * <p>
 * When the map reaches the limit the entire cache is cleared
 * (a strategy suitable for frame-coherent workloads where
 * locality changes slowly).
 */
public class LightCacheImpl implements LightCache {
    private final ConcurrentHashMap<Long, Float> cache;
    private final long maxSize;

    /** @param maxSize maximum entries before auto-clear */
    public LightCacheImpl(long maxSize) {
        this.cache = new ConcurrentHashMap<>((int) Math.min(maxSize, 65536));
        this.maxSize = maxSize;
    }

    @Override
    public double getOrCompute(long key, DoubleSupplier computer) {
        Float cached = cache.get(key);
        if (cached != null) return cached;
        double value = computer.getAsDouble();
        if (cache.mappingCount() >= maxSize) cache.clear();
        cache.put(key, (float) value);
        return value;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
