package muddykat.alchemia.client.gui;

#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
#else
import com.mojang.blaze3d.pipeline.RenderPipeline;
#endif
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GuideInk {

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/guide_ink"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/guide_ink"))
#if MC_VER >= 26300
            .withShaderDefine("ALCHEMIA_LOCATIONS")
#endif
            .build();

    public static final int PLAIN = 0xFFFFFFFF;
    public static final int SHORT = 0x70FFFFFF;

    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int WATER_BOTTLE_U = 240;
    private static final int WATER_BOTTLE_V = 68;

    private static final ItemStackRenderState STATE = new ItemStackRenderState();
    private static final RandomSource RANDOM = RandomSource.create();

    private GuideInk() {
    }

    public static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int colour) {
        if (!sprite(graphics, stack, x, y, colour)) graphics.item(stack, x, y);
    }

    public static void waterBottle(GuiGraphicsExtractor graphics, int x, int y, int colour) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, x, y, WATER_BOTTLE_U, WATER_BOTTLE_V, 16, 16, 256, 256, colour);
    }

    private static boolean sprite(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int colour) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(STATE, stack, ItemDisplayContext.GUI, minecraft.level, null, 0);
        RANDOM.setSeed(0L);
        Material.Baked material = STATE.pickParticleMaterial(RANDOM);
        if (material == null) return false;
        graphics.blitSprite(PIPELINE, material.sprite(), x, y, 16, 16, colour);
        return true;
    }
}
