package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.List;

public record InkTooltip(Component title, int titleColour, List<Component> lines, ItemStack icon, @Nullable ResourceLocation sprite) implements TooltipComponent {
    public static final ResourceLocation STYLE = new ResourceLocation(Alchemia.MODID, "parchment");
    public static final int INK = 0xFF3B2A1E;
    public static final int INK_RED = 0xFF8E2A22;

    public static InkTooltip of(Component title, List<Component> lines) {
        return new InkTooltip(title, INK, List.copyOf(lines), ItemStack.EMPTY, null);
    }

    public InkTooltip withIcon(ItemStack icon) {
        return new InkTooltip(title, titleColour, lines, icon, null);
    }

    public InkTooltip withSprite(ResourceLocation sprite) {
        return new InkTooltip(title, titleColour, lines, ItemStack.EMPTY, sprite);
    }

    public InkTooltip tinted(int titleColour) {
        return new InkTooltip(title, titleColour, lines, icon, sprite);
    }

    public boolean hasIcon() {
        return !icon.isEmpty() || sprite != null;
    }
}
