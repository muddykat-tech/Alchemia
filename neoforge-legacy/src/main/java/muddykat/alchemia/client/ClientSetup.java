package muddykat.alchemia.client;

import muddykat.alchemia.client.particle.AncientOakLeafParticle;
import muddykat.alchemia.client.particle.BrewBubbleParticle;
import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.client.gui.AlchemyMachineScreen;
import muddykat.alchemia.client.gui.ClientIngredientPathTooltip;
import muddykat.alchemia.client.gui.GrindOverlay;
import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.client.render.AlchemyMachineUpgradeRenderer;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.client.render.PlacedPotionsRenderer;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.GuideAccess;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import muddykat.alchemia.common.potion.PotionGlasses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.potion.VanillaIngredients;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import static muddykat.alchemia.Alchemia.MOD_NAME;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Alchemia.LOGGER.info(MOD_NAME + " Client Side Initialization");
            for (net.neoforged.neoforge.registries.DeferredItem<Item> entry : ItemRegistry.ITEM_REGISTRY.values()) {
                Item item = entry.get();
                if (item instanceof ItemIngredient) {
                    ItemProperties.register(item, CrushProperty.ID, CrushProperty.INSTANCE);
                }
            }

            ItemProperties.register(Items.POTION, ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "potion_glass"),
                    (stack, level, entity, seed) -> PotionGlasses.of(stack).ordinal());
        });
    }

    @SubscribeEvent
    public static void onRegisterScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
        event.register(MenuTypeRegistry.ALCHEMY_MACHINE.get(), AlchemyMachineScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : VanillaIngredients.drifts().keySet()) event.register(item, ClientSetup::grindBar);
    }

    private static boolean grindBar(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
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
    public static void onRegisterShaders(net.neoforged.neoforge.client.event.RegisterShadersEvent event) throws java.io.IOException {
        muddykat.alchemia.client.gui.GuideInk.register(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "grind_path"), GrindOverlay::render);
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypeRegistry.BREW_BUBBLE.get(), BrewBubbleParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.ANCIENT_OAK_LEAF.get(), AncientOakLeafParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> level != null && pos != null
                ? BiomeColors.getAverageFoliageColor(level, pos)
                : FoliageColor.getDefaultColor(), AncientOak.block(AncientOak.LEAVES));
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> FoliageColor.getDefaultColor(), AncientOak.block(AncientOak.LEAVES));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalCauldronRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get(), AlchemyMachineUpgradeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.PLACED_POTIONS.get(), PlacedPotionsRenderer::new);
    }
}
