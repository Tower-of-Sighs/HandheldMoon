package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.LightCache;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Primitive, thread-safe {@link LightCache} implementation.
 * <p>
 * Each querying thread owns its map, avoiding synchronization between Sodium
 * chunk workers. {@link #clear()} advances a shared generation; thread-local
 * maps are cleared lazily on their next access and retain their allocated
 * arrays for reuse.
 */
public class LightCacheImpl implements LightCache {
    private static final int MAX_INITIAL_CAPACITY = 256;

    private final int maxSize;
    private final AtomicLong generation = new AtomicLong();
    private final ThreadLocal<LocalCache> localCaches;

    /** @param maxSize maximum entries before auto-clear */
    public LightCacheImpl(long maxSize) {
        if (maxSize <= 0L) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = (int) Math.min(maxSize, Integer.MAX_VALUE - 8L);
        int initialCapacity = Math.min(this.maxSize, MAX_INITIAL_CAPACITY);
        this.localCaches = ThreadLocal.withInitial(() -> new LocalCache(initialCapacity));
    }

    @Override
    public double get(long key) {
        return localCaches.get().get(generation.get(), key);
    }

    @Override
    public void put(long key, double light) {
        localCaches.get().put(generation.get(), key, (float) light, maxSize);
    }

    @Override
    public void clear() {
        generation.incrementAndGet();
    }

    private static final class LocalCache {
        private final Long2FloatOpenHashMap values;
        private long generation = Long.MIN_VALUE;

        private LocalCache(int initialCapacity) {
            values = new Long2FloatOpenHashMap(initialCapacity);
            values.defaultReturnValue(Float.NaN);
        }

        private float get(long currentGeneration, long key) {
            syncGeneration(currentGeneration);
            return values.get(key);
        }

        private void put(long currentGeneration, long key, float light, int maxSize) {
            syncGeneration(currentGeneration);
            if (values.size() >= maxSize) {
                values.clear();
            }
            values.put(key, light);
        }

        private void syncGeneration(long currentGeneration) {
            if (generation != currentGeneration) {
                values.clear();
                generation = currentGeneration;
            }
        }
    }
}
