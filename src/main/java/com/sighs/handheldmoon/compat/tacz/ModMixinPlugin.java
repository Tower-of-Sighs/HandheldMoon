package com.sighs.handheldmoon.compat.tacz;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModMixinPlugin implements IMixinConfigPlugin {


    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("com.sighs.handheldmoon.mixin.lamb.LambDynLightsMixin")) {
            return isClassLoaded("com.tacz.guns.GunMod");
        }

        return true;
    }

    private static boolean isClassLoaded(String className) {
        try {
            // 使用当前线程的上下文类加载器（Minecraft 使用的类加载器）
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ModMixinPlugin.class.getClassLoader(); // fallback
            }
            cl.loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}