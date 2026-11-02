package cc.sighs.handheldmoon.util;

import java.lang.reflect.Method;

/**
 * Iris boundary for the 1.20.1 renderer. The 1.20.1 target does not compile
 * against the Iris API, so presence and shader-pack state are resolved
 * reflectively and cached once.
 */
public final class IrisCompat {
    private static final Object API;
    private static final Method IS_SHADER_PACK_IN_USE;

    static {
        Object api = null;
        Method method = null;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi", false,
                    IrisCompat.class.getClassLoader());
            api = apiClass.getMethod("getInstance").invoke(null);
            method = apiClass.getMethod("isShaderPackInUse");
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            api = null;
            method = null;
        }
        API = api;
        IS_SHADER_PACK_IN_USE = method;
    }

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        if (IS_SHADER_PACK_IN_USE == null) {
            return false;
        }
        try {
            Object active = IS_SHADER_PACK_IN_USE.invoke(API);
            return active instanceof Boolean && (Boolean) active;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
