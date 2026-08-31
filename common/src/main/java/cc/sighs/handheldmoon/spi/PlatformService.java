package cc.sighs.handheldmoon.spi;

import java.nio.file.Path;

public interface PlatformService {
    void initialize(Object loaderContext);

    Object id(String path);

    RegistryService registry();

    ClientPlatformService client();

    /** Returns the loader-specific configuration directory. */
    Path configDirectory();
}
