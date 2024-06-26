package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyBindingMap;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/book.png");
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/book_frame.png");
    private static final ResourceLocation BOOKMARKS_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/bookmarks.png");
    private static final ResourceLocation ALCHEMICAL_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/symbols.png");

    public AlchemicalScreen(AlchemicalCauldronMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 310;
        this.imageHeight = 218;
        resetViewPos();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        super.resize(pMinecraft, pWidth, pHeight);
        resetViewPos();
    }

    @Override
    public void render(@NotNull PoseStack ms, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(ms);

        assert this.minecraft != null;

        ms.scale(0.75f, 0.75f, 0.75f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        blit(ms, this.getGuiLeft(), this.getGuiTop(), 0, 0, 310, 218, 310, 218);


        RenderSystem.setShaderTexture(0, ALCHEMICAL_TEXTURE);
        this.renderAlchemicalPosition(ms, mouseX, mouseY);

        RenderSystem.setShaderTexture(0, FRAME_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        blit(ms, this.getGuiLeft(), this.getGuiTop(), 0, 0, 310, 218, 310, 218);

    }

    @Override
    protected void renderBg(@NotNull PoseStack ms, float partialTick, int mouseX, int mouseY) {
        // Render UI background
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null)
            return;
    }
    private boolean isDragging = false;
    private int startX, startY; // Starting position of mouse when dragging started

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int pButton) {

        if(pButton == InputConstants.MOUSE_BUTTON_LEFT)
        {
            isDragging = true;
            startX = (int) mouseX;
            startY = (int) mouseY;
        }

        if(pButton == InputConstants.MOUSE_BUTTON_RIGHT)
        {
            resetViewPos();
        }

        return super.mouseClicked(mouseX, mouseY, pButton);
    }

    int offsetX, offsetY;

    int xpos = this.getGuiLeft() + 380;
    int ypos = this.getGuiTop() + 220;

    private void resetViewPos(){
        xpos = (int) (this.getGuiLeft() * 2) + (imageWidth);
        ypos = (int) (this.getGuiTop() * 2) + (imageHeight);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == InputConstants.MOUSE_BUTTON_LEFT)
        {
            isDragging = false;
            offsetX = (int) (pMouseX - startX);
            offsetY = (int) (pMouseY - startY);
            xpos += offsetX;
            ypos += offsetY;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private void renderAlchemicalPosition(PoseStack ms, int mouseX, int mouseY){
        assert this.minecraft != null;
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();

        int xAlignment = cauldron.getXAlignment();
        int yAlignment = cauldron.getYAlignment();

        // Double everything as we scale to half later on, (for the icons, the background is still at the same scale)
        int visibleWidth = (299) * 2;
        int visibleHeight = (205) * 2;

        int initialXGuiPos = (int) (this.getGuiLeft() * 2);
        int initialYGuiPos = (int) (this.getGuiTop() * 2);

        // Calculate offset due to drag
        offsetX = isDragging ? mouseX - startX : 0;
        offsetY = isDragging ? mouseY - startY : 0;

        // Update positions considering drag offset
        int x_pos = xpos + offsetX;
        int y_pos = ypos + offsetY;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ms.pushPose();

        ms.scale(0.5f, 0.5f, 0.5f);

        if (x_pos > initialXGuiPos && x_pos < initialXGuiPos + visibleWidth &&
                y_pos > initialYGuiPos && y_pos < initialYGuiPos + visibleHeight) {
            blit(ms, x_pos, y_pos, 0, 0, 21, 23, 21, 45);
        }

        for (String key : PotionMap.INSTANCE.effectHashMap.keySet()) {
            //Alchemia.LOGGER.info("Positions: " + key);
            int xp = Integer.parseInt(String.valueOf(key.substring(0, key.indexOf(","))));
            int yp = Integer.parseInt(key.substring(key.indexOf(",")+1));
            int color = PotionMap.INSTANCE.effectHashMap.get(key).getEffect().getColor();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;

            int xPotionPos = (x_pos + (xp * 21)) - (xAlignment * 21);
            int yPotionPos = (y_pos + (yp * 22)) - (yAlignment * 22);

            // Check if potion position is within visible bounds
            if (xPotionPos > initialXGuiPos && xPotionPos < initialXGuiPos + visibleWidth &&
                    yPotionPos > initialYGuiPos && yPotionPos < initialYGuiPos + visibleHeight) {
                RenderSystem.setShaderColor(((float) red / 255), ((float) green / 255), ((float) blue / 255), 1.0f);
                blit(ms, xPotionPos, yPotionPos, 0, 24, 21, 21, 21, 45);
            }
        }
        ms.popPose();
    }


}
