package com.sighs.handheldmoon.api;

import net.minecraft.client.renderer.PostPass;

import java.util.List;

public interface PostChainAccessor {
    List<PostPass> getPasses();
}
