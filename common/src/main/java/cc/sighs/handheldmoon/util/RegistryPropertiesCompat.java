package cc.sighs.handheldmoon.util;

import net.minecraft.core.registries.Registries;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Applies 26.1 registration ids while remaining callable on 1.21.1. */
public final class RegistryPropertiesCompat {
    private RegistryPropertiesCompat() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T withId(T properties, String registry, String path) {
        try {
            Class<?> locationType;
            Method locationFactory;
            try {
                locationType = Class.forName("net.minecraft.resources.Identifier");
                locationFactory = locationType.getMethod("fromNamespaceAndPath", String.class, String.class);
            } catch (ClassNotFoundException ignored) {
                locationType = Class.forName("net.minecraft.resources.ResourceLocation");
                locationFactory = locationType.getMethod("fromNamespaceAndPath", String.class, String.class);
            }
            Object location = locationFactory.invoke(null, "handheldmoon", path);
            Object registryKey = switch (registry) {
                case "block" -> Registries.BLOCK;
                case "item" -> Registries.ITEM;
                default -> throw new IllegalArgumentException("Unknown registry: " + registry);
            };
            Class<?> resourceKeyType = Class.forName("net.minecraft.resources.ResourceKey");
            Object resourceKey = resourceKeyType.getMethod("create", registryKey.getClass(), locationType)
                    .invoke(null, registryKey, location);
            Method setId = properties.getClass().getMethod("setId", resourceKeyType);
            return (T) setId.invoke(properties, resourceKey);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return properties;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to apply registry id to Minecraft properties", exception);
        }
    }
}
