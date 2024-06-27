package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.realmsclient.util.TextRenderingUtils;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.EffectRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/book.png");
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/book_frame.png");
    private static final ResourceLocation BOOKMARKS_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/bookmarks.png");
    private static final ResourceLocation ALCHEMICAL_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/symbols.png");

    int offsetX, offsetY;

    int xpos = 0;
    int ypos = 0;

    public AlchemicalScreen(AlchemicalCauldronMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 310;
        this.imageHeight = 218;
        xpos = -1;
        ypos = -1;
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
        if(xpos == ypos && ypos == -1)
        {
            resetViewPos();
        }
        this.renderBg(ms, partialTick, mouseX, mouseY);

        assert this.minecraft != null;


        RenderSystem.setShaderTexture(0, ALCHEMICAL_TEXTURE);
        this.renderAlchemicalPosition(ms, mouseX, mouseY);

        try
        {
            RenderSystem.setShaderTexture(0, FRAME_TEXTURE);
            RenderSystem.disableDepthTest(); // Disable depth testing for overlay
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            blit(ms, this.getGuiLeft(), this.getGuiTop(), 0, 0, 310, 218, 310, 218);
            RenderSystem.enableDepthTest(); // Disable depth testing for overlay
        } catch (Exception ignored)
        {

        }

    }

    @Override
    protected void renderBg(@NotNull PoseStack ms, float partialTick, int mouseX, int mouseY) {
        // Render UI background
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(ms, this.getGuiLeft(), this.getGuiTop(), 0, 0, 310, 218, 310, 218);
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

    private void resetViewPos(){
        int initialXGuiPos = (int) (this.getGuiLeft());
        int initialYGuiPos = (int) (this.getGuiTop());

        xpos = initialXGuiPos + (imageWidth / 2);
        ypos = initialYGuiPos + (imageHeight / 2);
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
        int visibleWidth = (290);
        int visibleHeight = (205);

        int initialXGuiPos = (int) (this.getGuiLeft() + 10);
        int initialYGuiPos = (int) (this.getGuiTop() + 10);

        // Calculate offset due to drag
        offsetX = isDragging ? mouseX - startX : 0;
        offsetY = isDragging ? mouseY - startY : 0;

        // Update positions considering drag offset
        int x_pos = xpos + offsetX;
        int y_pos = ypos + offsetY;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);



        ms.pushPose();
        //ms.scale(0.5f, 0.5f, 0.5f);

        ArrayList<Pair<Integer, Integer>> line_data = new ArrayList<>();
        for (String key : PotionMap.INSTANCE.effectHashMap.keySet()) {
            //Alchemia.LOGGER.info("Positions: " + key);
            MobEffect effect = PotionMap.INSTANCE.effectHashMap.get(key).getEffect();
            int xp = Integer.parseInt(String.valueOf(key.substring(0, key.indexOf(","))));
            int yp = Integer.parseInt(key.substring(key.indexOf(",")+1));
            int color = effect.getColor();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;

            int xPotionPos = (x_pos + (xp * 21)) - (xAlignment * 21);
            int yPotionPos = (y_pos + (yp * 22)) - (yAlignment * 22);

            drawLine(ms, x_pos, y_pos, xPotionPos, yPotionPos, initialXGuiPos, initialYGuiPos, initialXGuiPos + visibleWidth, initialYGuiPos + visibleHeight);

            // Check if potion position is within visible bounds
            if (xPotionPos > initialXGuiPos && xPotionPos < initialXGuiPos + visibleWidth &&
                    yPotionPos > initialYGuiPos && yPotionPos < initialYGuiPos + visibleHeight) {
                //RenderSystem.setShaderColor(((float) red / 255), ((float) green / 255), ((float) blue / 255), 1.0f);
                //
                ItemStack potionItem = new ItemStack(Items.POTION);
                PotionUtils.setPotion(potionItem, PotionMap.INSTANCE.effectHashMap.get(key).getPotion());

                ms.pushPose();
                MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
                TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(effect);
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                blit(ms, xPotionPos - 11, yPotionPos - 14, this.getBlitOffset(), 18, 18, textureatlassprite);

                if(mouseX > xPotionPos-8 && mouseX < xPotionPos + 8 && mouseY > yPotionPos - 10 && mouseY < yPotionPos + 8)
                {
                    List<Component> text = new ArrayList<>();
                    text.add(new TextComponent(effect.getDisplayName().getString()));
                    renderComponentTooltip(ms, text, xPotionPos, yPotionPos);
                }
                ms.popPose();
            }
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (x_pos > initialXGuiPos && x_pos < initialXGuiPos + visibleWidth &&
                y_pos > initialYGuiPos && y_pos < initialYGuiPos + visibleHeight) {
            //blit(ms, x_pos -11, y_pos - 14, 0, 0, 21, 23, 21, 45);
            ItemStack potionItem = new ItemStack(Items.POTION);
            PotionUtils.setPotion(potionItem, Potions.WATER);
            ms.pushPose();
            itemRenderer.renderGuiItem(potionItem, x_pos - 8, y_pos - 11);
            ms.popPose();
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ms.popPose();
    }

    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void drawLine(PoseStack ms, int xStart, int yStart, int xEnd, int yEnd, int clipXMin, int clipYMin, int clipXMax, int clipYMax) {
        // Clip xStart within bounds

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int originX = xStart;
        int originY = yStart;
        int finalX = xEnd;
        int finalY = yEnd;

        if (xStart < clipXMin) {
            if (xEnd != xStart) {
                yStart = yStart + (clipXMin - xStart) * (yEnd - yStart) / (xEnd - xStart);
            }
            xStart = clipXMin;
        } else if (xStart > clipXMax) {
            if (xEnd != xStart) {
                yStart = yStart + (clipXMax - xStart) * (yEnd - yStart) / (xEnd - xStart);
            }
            xStart = clipXMax;
        }

        // Clip yStart within bounds
        if (yStart < clipYMin) {
            if (yEnd != yStart) {
                xStart = xStart + (clipYMin - yStart) * (xEnd - xStart) / (yEnd - yStart);
            }
            yStart = clipYMin;
        } else if (yStart > clipYMax) {
            if (yEnd != yStart) {
                xStart = xStart + (clipYMax - yStart) * (xEnd - xStart) / (yEnd - yStart);
            }
            yStart = clipYMax;
        }

        // Clip xEnd within bounds
        if (xEnd < clipXMin) {
            if (xStart != xEnd) {
                yEnd = yEnd + (clipXMin - xEnd) * (yStart - yEnd) / (xStart - xEnd);
            }
            xEnd = clipXMin;
        } else if (xEnd > clipXMax) {
            if (xStart != xEnd) {
                yEnd = yEnd + (clipXMax - xEnd) * (yStart - yEnd) / (xStart - xEnd);
            }
            xEnd = clipXMax;
        }

        // Clip yEnd within bounds
        if (yEnd < clipYMin) {
            if (yStart != yEnd) {
                xEnd = xEnd + (clipYMin - yEnd) * (xStart - xEnd) / (yStart - yEnd);
            }
            yEnd = clipYMin;
        } else if (yEnd > clipYMax) {
            if (yStart != yEnd) {
                xEnd = xEnd + (clipYMax - yEnd) * (xStart - xEnd) / (yStart - yEnd);
            }
            yEnd = clipYMax;
        }

        // Calculate total distance between origin and final
        double totalDistance = Math.sqrt(Math.pow(finalX - originX, 2) + Math.pow(finalY - originY, 2));

        // Calculate distance from final to end point (xEnd, yEnd)
        double distanceEnd = Math.sqrt(Math.pow(xEnd - originX, 2) + Math.pow(yEnd - originY, 2));
        double percentageDistanceEnd = (distanceEnd / totalDistance);

        // Calculate distance from origin to start point (xStart, yStart)
        double distanceStart = Math.sqrt(Math.pow(xStart - originX, 2) + Math.pow(yStart - originY, 2));
        double percentageDistanceStart = (distanceStart / totalDistance);

        // Calculate alpha values based on percentage distances
        int startAlpha = (int) Math.floor(percentageDistanceStart * 10);
        int endAlpha = (int) Math.floor(percentageDistanceEnd * 200);

        int thickness = 4;

        try {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            RenderSystem.disableTexture();

            // Calculate line direction and normalize it
            Vec3 lineDirection = new Vec3(xEnd - xStart, yEnd - yStart, 0).normalize();

            // Calculate perpendicular vector
            Vec3 perpendicular = new Vec3(-lineDirection.y(), lineDirection.x(), 0).normalize().scale(thickness / 2.0);

            // Calculate offsets for start and end vertices
            Vec3 startOffset = perpendicular.scale(0.5); // Offset from center to start
            Vec3 endOffset = perpendicular.scale(-0.5);  // Offset from center to end

            // Begin drawing in QUADS mode
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Define vertices for the quad
            buffer.vertex(xStart - startOffset.x(), yStart - startOffset.y(), 0.0).color(40, 40, 40, startAlpha).endVertex(); // Bottom-left
            buffer.vertex(xStart + startOffset.x(), yStart + startOffset.y(), 0.0).color(40, 40, 40, startAlpha).endVertex(); // Top-left
            buffer.vertex(xEnd - endOffset.x(), yEnd - endOffset.y(), 0.0).color(40, 40, 40, endAlpha).endVertex();     // Top-right
            buffer.vertex(xEnd + endOffset.x(), yEnd + endOffset.y(), 0.0).color(40, 40, 40, endAlpha).endVertex();     // Bottom-right

            tessellator.end();

            RenderSystem.enableTexture();
        } catch (Exception error)
        {
            Alchemia.LOGGER.info("Bad Code: " + error.getMessage());
        }
    }
}
