package cc.sighs.handheldmoon.spi;

public interface PlatformService {
    void initialize(Object loaderContext);

    Object id(String path);

    RegistryService registry();

    ClientPlatformService client();
}
