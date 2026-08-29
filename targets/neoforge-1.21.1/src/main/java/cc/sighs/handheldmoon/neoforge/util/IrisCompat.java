package cc.sighs.handheldmoon.neoforge.util;

import net.irisshaders.iris.api.v0.IrisApi;

/** Iris boundary for the legacy renderer; Iris owns the active pipeline. */
public final class IrisCompat {
    private static final boolean PRESENT = detect();

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        return PRESENT && IrisApi.getInstance().isShaderPackInUse();
    }

    private static boolean detect() {
        try {
            Class.forName("net.irisshaders.iris.api.v0.IrisApi", false, IrisCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
