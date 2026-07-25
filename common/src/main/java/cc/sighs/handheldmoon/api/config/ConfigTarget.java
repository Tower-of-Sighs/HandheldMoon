package cc.sighs.handheldmoon.api.config;

public interface ConfigTarget<C> {
    C get();

    void apply(C config);
}
