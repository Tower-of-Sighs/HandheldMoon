package cc.sighs.handheldmoon.fabric.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates the Sodium chunk-render mixins on Sodium actually being installed, so
 * the shared Embeddium/Sodium config can stay loaded on every installation.
 */
public final class HandheldMoonFabricMixinPlugin implements IMixinConfigPlugin {
    private static final String SODIUM_PACKAGE = "cc.sighs.handheldmoon.mixin.embeddium.";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(SODIUM_PACKAGE)) {
            FabricLoader loader = FabricLoader.getInstance();
            return loader.isModLoaded("sodium") || loader.isModLoaded("embeddium");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
