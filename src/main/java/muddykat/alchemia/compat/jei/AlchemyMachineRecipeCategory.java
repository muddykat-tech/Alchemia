package muddykat.alchemia.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.List;

public class AlchemyMachineRecipeCategory implements IRecipeCategory<MagnumOpus.Stage> {

    public static final RecipeType<MagnumOpus.Stage> TYPE =
            RecipeType.create(Alchemia.MODID, "alchemy_machine", MagnumOpus.Stage.class);

    private static final int WIDTH = 134;
    private static final int HEIGHT = 60;

    private static final int SLOT_PITCH = 18;
    private static final int POTION_X = 1;
    private static final int POTION_Y = 1;
    private static final int POTION_COLUMNS = 3;

    private static final int INPUT_X = 60;
    private static final int INPUT_Y = 10;
    private static final int FLAME_X = 86;
    private static final int FLAME_Y = 32;
    private static final int ARROW_X = 82;
    private static final int ARROW_Y = 12;
    private static final int OUTPUT_X = 112;
    private static final int OUTPUT_Y = 10;

    private static final int FLAME_TICKS = 300;
    private static final int TEXT_HEIGHT = 10;

    private final IDrawable icon;

    public AlchemyMachineRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ItemRegistry.getItemFromRegistry("alchemy_machine_core").asItem()));
    }

    @Override
    public RecipeType<MagnumOpus.Stage> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("alchemia.jei.alchemy_machine");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MagnumOpus.Stage stage, IFocusGroup focuses) {
        List<PotionEffectIngredient> potions = stage.potions();
        for (int index = 0; index < potions.size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT,
                            POTION_X + (index % POTION_COLUMNS) * SLOT_PITCH,
                            POTION_Y + (index / POTION_COLUMNS) * SLOT_PITCH)
                    .setStandardSlotBackground()
                    .addItemStack(potions.get(index).sampleStack());
        }

        String previous = stage.previous();
        if (previous != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y)
                    .setStandardSlotBackground()
                    .addItemLike(ItemRegistry.getItemFromRegistry(previous));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .setOutputSlotBackground()
                .addItemLike(ItemRegistry.getItemFromRegistry(stage.result()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MagnumOpus.Stage stage, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(TileEntityAlchemyMachineCore.COOK_DURATION)
                .setPosition(ARROW_X, ARROW_Y);

        builder.addAnimatedRecipeFlame(FLAME_TICKS)
                .setPosition(FLAME_X, FLAME_Y);

        builder.addText(Component.translatable("alchemia.jei.alchemy_machine.slots", stage.potionCount()),
                        WIDTH - 2, TEXT_HEIGHT)
                .setPosition(POTION_X, HEIGHT - TEXT_HEIGHT);
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(MagnumOpus.Stage stage) {
        return new ResourceLocation(Alchemia.MODID, stage.result());
    }
}
