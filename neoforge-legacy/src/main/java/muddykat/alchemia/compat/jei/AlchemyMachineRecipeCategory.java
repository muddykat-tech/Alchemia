package muddykat.alchemia.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
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
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;

import java.util.List;

public class AlchemyMachineRecipeCategory implements IRecipeCategory<AlchemyMachineRecipeCategory.Entry> {

    public record Entry(@Nullable MagnumOpus.Stage stage, boolean known, List<String> hidden) {

        public static Entry known(MagnumOpus.Stage stage) {
            return new Entry(stage, true, List.of());
        }

        public static Entry undiscovered(List<String> hidden) {
            return new Entry(null, false, List.copyOf(hidden));
        }
    }

    public static final RecipeType<Entry> TYPE =
            RecipeType.create(Alchemia.MODID, "alchemy_machine", Entry.class);

    private static final int WIDTH = 152;
    private static final int HEIGHT = 66;

    private static final int SLOT_PITCH = 18;
    private static final int POTION_X = 1;
    private static final int POTION_Y = 1;
    private static final int POTION_COLUMNS = 4;

    private static final int INPUT_X = 78;
    private static final int INPUT_Y = 10;
    private static final int FLAME_X = 104;
    private static final int FLAME_Y = 32;
    private static final int ARROW_X = 100;
    private static final int ARROW_Y = 12;
    private static final int OUTPUT_X = 130;
    private static final int OUTPUT_Y = 10;

    private static final int FLAME_TICKS = 300;
    private static final int HINT_TEXT_Y = 12;
    private static final int HINT_ROW_Y = 26;
    private static final int BRUSH_X = 26;
    private static final int PLUS_X = 46;
    private static final int GROUND_X = 61;
    private static final int DIG_ARROW_X = 81;
    private static final int TABLET_X = 107;
    private static final int HINT_COLOUR = 0xFF8B8B8B;
    private static final int TEXT_HEIGHT = 10;

    private final IDrawable icon;

    public AlchemyMachineRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ItemRegistry.getItemFromRegistry("alchemy_machine_core").asItem()));
    }

    @Override
    public RecipeType<Entry> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        MagnumOpus.Stage stage = entry.stage();
        if (!entry.known() || stage == null) {
            hint(builder, entry.hidden());
            return;
        }
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

    private static void hint(IRecipeLayoutBuilder builder, List<String> hidden) {
        List<ItemStack> results = new java.util.ArrayList<>();
        for (String result : hidden) results.add(new ItemStack(ItemRegistry.getItemFromRegistry(result)));
        if (!results.isEmpty()) builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(results);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, BRUSH_X, HINT_ROW_Y)
                .setStandardSlotBackground()
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("alchemia.jei.undiscovered.brush")
                        .withStyle(ChatFormatting.GRAY)))
                .addItemLike(Items.BRUSH);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, GROUND_X, HINT_ROW_Y)
                .setStandardSlotBackground()
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("alchemia.jei.undiscovered.ruins")
                        .withStyle(ChatFormatting.DARK_GRAY)))
                .addItemStacks(List.of(new ItemStack(Items.SUSPICIOUS_SAND), new ItemStack(Items.SUSPICIOUS_GRAVEL)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, TABLET_X, HINT_ROW_Y)
                .setOutputSlotBackground()
                .addItemLike(ItemRegistry.getItemFromRegistry(ItemAlchemyTablet.ID));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Entry entry, IFocusGroup focuses) {
        MagnumOpus.Stage stage = entry.stage();
        if (!entry.known() || stage == null) {
            builder.addText(Component.translatable("alchemia.jei.undiscovered"), WIDTH, TEXT_HEIGHT)
                    .setPosition(0, HINT_TEXT_Y)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(HINT_COLOUR);
            builder.addRecipePlusSign().setPosition(PLUS_X, HINT_ROW_Y + 2);
            builder.addRecipeArrow().setPosition(DIG_ARROW_X, HINT_ROW_Y + 1);
            return;
        }
        builder.addAnimatedRecipeArrow(TileEntityAlchemyMachineCore.COOK_DURATION)
                .setPosition(ARROW_X, ARROW_Y);

        builder.addAnimatedRecipeFlame(FLAME_TICKS)
                .setPosition(FLAME_X, FLAME_Y);

        builder.addText(Component.translatable("alchemia.jei.alchemy_machine.slots", stage.potionCount()),
                        WIDTH - 2, TEXT_HEIGHT)
                .setPosition(POTION_X, HEIGHT - TEXT_HEIGHT);
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(Entry entry) {
        MagnumOpus.Stage stage = entry.stage();
        if (!entry.known() || stage == null) {
            return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "undiscovered/" + String.join(".", entry.hidden()));
        }
        return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, stage.result() + "/" + Integer.toUnsignedString(stage.potions().hashCode(), 36));
    }
}
