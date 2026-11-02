package cc.sighs.handheldmoon.mixin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates the Embeddium/Sodium chunk-render mixins on the mod actually being
 * installed, so {@code handheldmoon.embeddium.mixins.json} can stay loaded on
 * every installation without referencing classes that do not exist.
 */
public final class HandheldMoonMixinPlugin implements IMixinConfigPlugin {
    private static final String EMBEDDIUM_PACKAGE = "cc.sighs.handheldmoon.mixin.embeddium.";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(EMBEDDIUM_PACKAGE)) {
            return hasEmbeddium();
        }
        return true;
    }

    /** Mixin asks before FML has finished building the mod list, so both lists are queried. */
    private static boolean hasEmbeddium() {
        ModList loaded = ModList.get();
        if (loaded != null) {
            return loaded.isLoaded("embeddium") || loaded.isLoaded("sodium");
        }
        LoadingModList loading = LoadingModList.get();
        if (loading == null) {
            return false;
        }
        // Only the Sodium 0.5 generation has the hooked BlockRenderer; older
        // forks (Rubidium et al.) must not get this mixin applied.
        for (String id : new String[]{"embeddium", "sodium"}) {
            if (loading.getModFileById(id) != null) {
                return true;
            }
        }
        return false;
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
