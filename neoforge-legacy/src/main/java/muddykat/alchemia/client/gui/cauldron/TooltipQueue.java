package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.InkTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;

import java.util.List;

public final class TooltipQueue {

    private record Pending(List<ClientTooltipComponent> lines, int x, int y, @Nullable ResourceLocation style,
                           boolean spaced, ItemStack stack) {
    }

    private @Nullable Pending pending;

    public void clear() {
        pending = null;
    }

    public void queue(List<ClientTooltipComponent> lines, int guiX, int guiY, @Nullable ResourceLocation style,
                      boolean spaced, ItemStack stack) {
        if (pending != null || lines.isEmpty()) return;
        pending = new Pending(List.copyOf(lines), guiX, guiY, style, spaced, stack);
    }

    public void ink(InkTooltip tooltip, int guiX, int guiY) {
        queue(List.of(ClientTooltipComponent.create(tooltip)), guiX, guiY, InkTooltip.STYLE, false, ItemStack.EMPTY);
    }

    public void draw(GuiGraphics graphics, Font font, float scale, int screenW, int screenH) {
        Pending tip = pending;
        pending = null;
        if (tip == null) return;

        ClientTooltipPositioner positioner = (width, height, x, y, w, h) ->
                DefaultTooltipPositioner.INSTANCE.positionTooltip(screenW, screenH, x, y, w, h);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0F);
        GuiDraw.nextStratum(graphics);
        GuiDraw.tooltip(graphics, font, tip.lines(), tip.x(), tip.y(), positioner, tip.style());
        pose.popPose();
    }
}
