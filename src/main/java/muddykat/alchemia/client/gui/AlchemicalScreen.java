package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/alchemical_guide_pages.png");
    public AlchemicalScreen(AlchemicalCauldronMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    public void render(@NotNull PoseStack ms, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(ms);

        ms.scale(0.75f, 0.75f, 0.75f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(ms, this.leftPos - 40, this.topPos, 0, 0, 412, 200, 512, 512);

        RenderSystem.setShaderTexture(1, BACKGROUND_TEXTURE);
        blit(ms, this.leftPos - 40, this.topPos, 0, 200, 412, 200, 512, 512);

        this.renderAlchemicalPosition(ms, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack ms, float partialTick, int mouseX, int mouseY) {
        // Render UI background
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null)
            return;
    }

    private void renderAlchemicalPosition(PoseStack ms, int mouseX, int mouseY){
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();

        int x = cauldron.getXAlignment();
        int y = cauldron.getYAlignment();
        int xpos = this.leftPos + 400;
        int ypos = this.topPos + 225;
        ms.pushPose();
        ms.scale(0.5f, 0.5f, 0.5f);
            RenderSystem.setShaderTexture(1, BACKGROUND_TEXTURE);
            this.blit(ms, xpos, ypos, 412, 76, 15, 15, 512, 512);

        for (String key : PotionMap.INSTANCE.effectHashMap.keySet()) {
            //Alchemia.LOGGER.info("Positions: " + key);
            int xp = Integer.parseInt(String.valueOf(key.substring(0, key.indexOf(","))));
            int yp = Integer.parseInt(key.substring(key.indexOf(",")+1));
            blit(ms, (xpos + (xp * 15)) - (x * 15), (ypos + (yp * 15)) - (y * 15), 412 + ((x == xp && y == yp) ? 16 : 0), 92, 15, 15, 512, 512);
        }

        ms.popPose();
    }
}
