package cc.sighs.handheldmoon.api.compat;

public interface FlashlightController<P> {
    boolean isUsingFlashlight(P player);

    void toggleFlashlight(P player);
}
