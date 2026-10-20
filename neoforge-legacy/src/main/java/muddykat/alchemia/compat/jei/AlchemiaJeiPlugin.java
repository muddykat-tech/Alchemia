package muddykat.alchemia.compat.jei;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemiaSprites;
import muddykat.alchemia.client.gui.AlchemyMachineScreen;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.registration.registers.BlockRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AlchemiaJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AlchemyMachineRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiGreatWork.attach(runtime);
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiGreatWork.detach();
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        List<ItemLike> stations = new ArrayList<>();
        stations.add(BlockRegistry.BLOCK_REGISTRY.get("alchemy_machine_core").get());
        for (UpgradeSide side : UpgradeSide.values()) {
            stations.add(BlockRegistry.BLOCK_REGISTRY.get(side.registryName()).get());
        }
        registration.addRecipeCatalysts(AlchemyMachineRecipeCategory.TYPE, stations.toArray(ItemLike[]::new));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(AlchemyMachineScreen.class, new IGuiContainerHandler<AlchemyMachineScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AlchemyMachineScreen screen) {
                return screen.getPanelAreas();
            }
        });

        registration.addRecipeClickArea(AlchemyMachineScreen.class,
                AlchemyMachineScreen.COOK_X, AlchemyMachineScreen.COOK_Y,
                AlchemiaSprites.COOK_W, AlchemiaSprites.COOK_H,
                AlchemyMachineRecipeCategory.TYPE);
    }
}
