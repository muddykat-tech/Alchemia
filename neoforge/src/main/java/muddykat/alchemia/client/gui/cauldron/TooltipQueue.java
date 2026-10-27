package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.InkTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class TooltipQueue {

    private record Pending(List<ClientTooltipComponent> lines, int x, int y, @Nullable Identifier style,
                           boolean spaced, ItemStack stack) {
    }

    private @Nullable Pending pending;

    public void clear() {
        pending = null;
    }

    public void queue(List<ClientTooltipComponent> lines, int guiX, int guiY, @Nullable Identifier style,
                      boolean spaced, ItemStack stack) {
        if (pending != null || lines.isEmpty()) return;
        pending = new Pending(List.copyOf(lines), guiX, guiY, style, spaced, stack);
    }

    public void ink(InkTooltip tooltip, int guiX, int guiY) {
        queue(List.of(ClientTooltipComponent.create(tooltip)), guiX, guiY, InkTooltip.STYLE, false, ItemStack.EMPTY);
    }

    public void draw(GuiGraphicsExtractor graphics, Font font, float scale, int screenW, int screenH) {
        Pending tip = pending;
        pending = null;
        if (tip == null) return;

        ClientTooltipPositioner positioner = (width, height, x, y, w, h) ->
                DefaultTooltipPositioner.INSTANCE.positionTooltip(screenW, screenH, x, y, w, h);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(scale, scale);
        graphics.nextStratum();
#if MC_VER >= 26300
        graphics.tooltip(font, tip.lines(), tip.x(), tip.y(), positioner, tip.style(), tip.spaced(), tip.stack());
#else
        graphics.tooltip(font, tip.lines(), tip.x(), tip.y(), positioner, tip.style());
#endif
        pose.popMatrix();
    }
}
