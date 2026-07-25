package cc.sighs.handheldmoon.api.compat;

import java.util.Objects;

public final class FlashlightControllerRegistry<P> {
    private volatile FlashlightController<P> controller = noOpController();

    public void install(FlashlightController<P> controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    public boolean isUsingFlashlight(P player) {
        return controller.isUsingFlashlight(player);
    }

    public void toggleFlashlight(P player) {
        controller.toggleFlashlight(player);
    }

    private static <P> FlashlightController<P> noOpController() {
        return new FlashlightController<P>() {
            @Override
            public boolean isUsingFlashlight(P player) {
                return false;
            }

            @Override
            public void toggleFlashlight(P player) {
            }
        };
    }
}
