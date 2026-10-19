package cc.sighs.handheldmoon.menu;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.item.ColorLensItem;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.registry.ModMenus;
import cc.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Container menu backing the flashlight configuration page. The same page is
 * used for a held lamp item and for a placed lamp block: it owns one lens slot
 * whose contents are written through to the target's {@code LAMP_CONFIG}.
 */
public final class FlashlightConfigMenu extends AbstractContainerMenu {
    public static final int LENS_SLOT_X = 20;
    public static final int LENS_SLOT_Y = 24;
    public static final int INVENTORY_X = 19;
    public static final int INVENTORY_Y = 160;

    /** Block target handed to the client-side menu constructor by the open packet. */
    private static volatile @Nullable BlockPos pendingClientBlockPos;

    private final Player player;
    private final @Nullable InteractionHand hand;
    private final @Nullable BlockPos blockPos;

    /** Constructor used by the client when the server opens the page. */
    public FlashlightConfigMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, takePendingBlockPos());
    }

    public FlashlightConfigMenu(int containerId, Inventory inventory, @Nullable BlockPos blockPos) {
        super(ModMenus.FLASHLIGHT_CONFIG.get(), containerId);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.hand = blockPos == null ? resolveHand(this.player) : null;

        LensSlotContainer lensContainer = new LensSlotContainer(this);
        addSlot(new Slot(lensContainer, 0, LENS_SLOT_X, LENS_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.COLOR_LENS.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
    }

    /** Remembers the block whose page the client is about to request. */
    public static void expectBlockTarget(BlockPos pos) {
        pendingClientBlockPos = pos;
    }

    public static void clearPendingBlockTarget() {
        pendingClientBlockPos = null;
    }

    private static @Nullable BlockPos takePendingBlockPos() {
        BlockPos pos = pendingClientBlockPos;
        pendingClientBlockPos = null;
        return pos;
    }

    /** Returns the hand holding the flashlight, preferring the main hand. */
    public static @Nullable InteractionHand resolveHand(Player player) {
        if (player.getMainHandItem().is(ModItems.MOONLIGHT_LAMP.get())) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().is(ModItems.MOONLIGHT_LAMP.get())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public @Nullable InteractionHand getHand() {
        return hand;
    }

    public @Nullable BlockPos getBlockPos() {
        return blockPos;
    }

    /** Reads the live configuration of the page's target. */
    public LampDeviceConfig readConfig() {
        if (blockPos != null) {
            if (player.level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntityAccess lamp) {
                return lamp.getLampConfig();
            }
            return LampDeviceConfig.fromGlobalConfig();
        }
        if (hand != null) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(ModItems.MOONLIGHT_LAMP.get())) {
                return stack.getOrDefault(
                        ModDataComponent.LAMP_CONFIG.get(), LampDeviceConfig.fromGlobalConfig());
            }
        }
        return LampDeviceConfig.fromGlobalConfig();
    }

    /**
     * Writes the configuration back to the target. On the client {@code sync}
     * sends the ordinary config packet; on the server the block entity or item
     * is updated directly.
     */
    public void writeConfig(LampDeviceConfig config, boolean sync) {
        if (blockPos != null) {
            if (player.level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntityAccess lamp) {
                lamp.setLampConfig(config, true);
            }
            if (sync && player.level().isClientSide()) {
                ClientUtils.syncMoonlightLampConfig(blockPos, config);
            }
            return;
        }
        if (hand == null) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.MOONLIGHT_LAMP.get())) {
            return;
        }
        stack.set(ModDataComponent.LAMP_CONFIG.get(), config);
        if (sync && player.level().isClientSide()) {
            ClientUtils.syncHeldMoonlightLampConfig(hand, config);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setItemInHand(hand, stack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!stack.is(ModItems.COLOR_LENS.get()) || !this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockPos != null) {
            return player.level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntityAccess
                    && player.distanceToSqr(blockPos.getCenter()) <= 64.0 * 64.0;
        }
        return hand != null && player.getItemInHand(hand).is(ModItems.MOONLIGHT_LAMP.get());
    }

    private static final class LensSlotContainer extends SimpleContainer {
        private final FlashlightConfigMenu menu;
        private boolean loading = true;
        private boolean writing;

        private LensSlotContainer(FlashlightConfigMenu menu) {
            super(1);
            this.menu = menu;
            LampDeviceConfig config = menu.readConfig();
            if (config.hasLens()) {
                super.setItem(0, ColorLensItem.create(config.lensColor()));
            }
            this.loading = false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.is(ModItems.COLOR_LENS.get());
        }

        @Override
        public void setChanged() {
            if (loading || writing) {
                return;
            }
            ItemStack lens = getItem(0);
            int color = lens.is(ModItems.COLOR_LENS.get())
                    ? ColorLensItem.getColor(lens)
                    : LampDeviceConfig.NO_LENS_COLOR;
            LampDeviceConfig config = menu.readConfig();
            LampDeviceConfig updated = config.withLensColor(color);
            if (updated.equals(config)) {
                return;
            }
            writing = true;
            menu.writeConfig(updated, true);
            writing = false;
        }
    }
}
