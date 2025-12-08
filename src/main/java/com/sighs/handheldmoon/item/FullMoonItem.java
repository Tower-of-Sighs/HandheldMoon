package com.sighs.handheldmoon.item;

import com.sighs.handheldmoon.registry.ModBlocks;
import com.sighs.handheldmoon.util.RegisterHelper;
import net.minecraft.world.item.BlockItem;

public class FullMoonItem extends BlockItem {
    public FullMoonItem() {
        super(ModBlocks.FULL_MOON, new Properties().setId(RegisterHelper.itemKey("full_moon")));
    }
}
