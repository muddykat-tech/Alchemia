package muddykat.alchemia.client;

import muddykat.alchemia.client.particle.AncientOakLeafParticle;
import muddykat.alchemia.client.particle.BrewBubbleParticle;
import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.potion.VanillaIngredients;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static muddykat.alchemia.Alchemia.MOD_NAME;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Alchemia.LOGGER.info(MOD_NAME + " Client Side Initialization");
            MenuScreens.register(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalScreen::new);
            MenuScreens.register(MenuTypeRegistry.ALCHEMY_MACHINE.get(), AlchemyMachineScreen::new);

            for (RegistryObject<Item> entry : ItemRegistry.ITEM_REGISTRY.values()) {
                Item item = entry.get();
                if (item instanceof ItemIngredient) {
                    ItemProperties.register(item, CrushProperty.ID, CrushProperty.INSTANCE);
                }
            }

            ItemProperties.register(Items.POTION, new ResourceLocation(Alchemia.MODID, "potion_glass"),
                    (stack, level, entity, seed) -> PotionGlasses.of(stack).ordinal());
        });
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
        GuideAccess.bindReader(() -> GuideAccess.carriedBy(Minecraft.getInstance().player));
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("grind_path", GrindOverlay::render);
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
