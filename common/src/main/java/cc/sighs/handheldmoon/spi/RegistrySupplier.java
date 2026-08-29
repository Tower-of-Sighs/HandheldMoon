package cc.sighs.handheldmoon.spi;

import java.util.function.Supplier;

/** Loader-neutral view of a registered Minecraft object. */
public interface RegistrySupplier<T> extends Supplier<T> {
    @Override
    T get();
}
