package muddykat.alchemia.client;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.client.gui.AlchemyMachineScreen;
import muddykat.alchemia.client.render.AlchemyMachineUpgradeRenderer;
import muddykat.alchemia.client.gui.ClientIngredientPathTooltip;
import muddykat.alchemia.client.gui.GrindOverlay;
import muddykat.alchemia.common.items.helper.GuideAccess;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.client.render.PlacedPotionsRenderer;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.items.ItemIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import muddykat.alchemia.client.particle.AncientOakLeafParticle;
import muddykat.alchemia.client.particle.BrewBubbleParticle;
import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import java.util.List;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import static muddykat.alchemia.Alchemia.MOD_NAME;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(muddykat.alchemia.client.gui.GuideInk.PIPELINE);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        Alchemia.LOGGER.info(MOD_NAME + " Client Side Initialization");
        event.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
        event.register(MenuTypeRegistry.ALCHEMY_MACHINE.get(), AlchemyMachineScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Alchemia.MODID, "crush"), CrushProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : VanillaIngredients.drifts().keySet()) event.register(item, ClientSetup::grindBar);
    }

    private static boolean grindBar(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        int crush = ItemIngredient.crushOf(stack);
        if (crush <= 0) return false;

        graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
        graphics.fill(x + 2, y + 13, x + 2 + ItemIngredient.crushBarWidth(crush), y + 14,
                0xFF000000 | ItemIngredient.crushBarColor(crush));
        return false;
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IngredientPathTooltip.class, ClientIngredientPathTooltip::new);
        GuideAccess.bindReader(() -> GuideAccess.carriedBy(Minecraft.getInstance().player)
                || muddykat.alchemia.client.gui.AlchemicalScreen.readingAtDesk());
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(Alchemia.MODID, "grind_path"), GrindOverlay::render);
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypeRegistry.BREW_BUBBLE.get(), BrewBubbleParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.ANCIENT_OAK_LEAF.get(), AncientOakLeafParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.foliage()), AncientOak.block(AncientOak.LEAVES));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalCauldronRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get(), AlchemyMachineUpgradeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.PLACED_POTIONS.get(), PlacedPotionsRenderer::new);
    }
}
