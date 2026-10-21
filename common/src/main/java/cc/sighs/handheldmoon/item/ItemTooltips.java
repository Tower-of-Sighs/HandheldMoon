package cc.sighs.handheldmoon.item;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shared tooltip line builder. The item override itself is version-specific
 * (26.1 uses a {@code Consumer}, 1.21.1 uses a {@code List}), but both sides
 * emit the same translation keys through these helpers.
 */
public final class ItemTooltips {
    private ItemTooltips() {
    }

    public static void addLines(Consumer<Component> builder, String... keys) {
        for (String key : keys) {
            builder.accept(Component.translatable(key));
        }
    }

    public static void addLines(List<Component> lines, String... keys) {
        for (String key : keys) {
            lines.add(Component.translatable(key));
        }
    }
}
