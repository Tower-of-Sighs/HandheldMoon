package cc.sighs.handheldmoon.api.light.impl;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightCacheImplTest {
    @Test
    void storesPrimitiveLightValuesAndReportsMisses() {
        LightCacheImpl cache = new LightCacheImpl(8);

        assertTrue(Double.isNaN(cache.get(10L)));
        cache.put(10L, 7.25);

        assertEquals(7.25, cache.get(10L), 1.0E-6);
    }

    @Test
    void clearsTheLocalMapWhenCapacityIsReached() {
        LightCacheImpl cache = new LightCacheImpl(2);
        cache.put(1L, 1.0);
        cache.put(2L, 2.0);
        cache.put(3L, 3.0);

        assertTrue(Double.isNaN(cache.get(1L)));
        assertTrue(Double.isNaN(cache.get(2L)));
        assertEquals(3.0, cache.get(3L), 1.0E-6);
    }

    @Test
    void invalidatesExistingWorkerThreadCachesLazily() throws Exception {
        LightCacheImpl cache = new LightCacheImpl(8);
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            Future<Double> stored = worker.submit(() -> {
                cache.put(42L, 9.0);
                return cache.get(42L);
            });
            assertEquals(9.0, stored.get(), 1.0E-6);

            cache.clear();

            Future<Double> afterClear = worker.submit(() -> cache.get(42L));
            assertTrue(Double.isNaN(afterClear.get()));
        } finally {
            worker.shutdownNow();
        }
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LightCacheImpl(0));
    }
}
