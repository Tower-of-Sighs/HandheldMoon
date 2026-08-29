package cc.sighs.handheldmoon.spi;

import java.util.ServiceLoader;

/** Single entry point for all loader-specific behavior used by common code. */
public final class PlatformServices {
    private static PlatformService service;

    private PlatformServices() {
    }

    public static void initialize(Object loaderContext) {
        if (service != null) {
            return;
        }
        service = ServiceLoader.load(PlatformService.class).findFirst()
                .orElseThrow(() -> new IllegalStateException("No HandheldMoon PlatformService implementation found"));
        service.initialize(loaderContext);
    }

    public static PlatformService require() {
        if (service == null) {
            throw new IllegalStateException("HandheldMoon platform services have not been initialized");
        }
        return service;
    }
}
