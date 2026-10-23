package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class PrimerRecipes {

    private static final int GRID = 3;
    static final int SMALL = 2;

    record View(List<List<ItemStack>> grid, ItemStack result, @Nullable Identifier missing, @Nullable List<List<ItemStack>> compact) {
        View(List<List<ItemStack>> grid, ItemStack result, @Nullable Identifier missing) {
            this(grid, result, missing, squeeze(grid));
        }

        boolean fitsSmall() {
            return compact != null;
        }

        List<List<ItemStack>> cells(int size) {
            return size == SMALL && compact != null ? compact : grid;
        }
    }

    private static volatile RecipeMap recipes = RecipeMap.EMPTY;

    private PrimerRecipes() {
    }

    @SubscribeEvent
    public static void received(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(RecipeType.CRAFTING)) recipes = event.getRecipeMap();
    }

    static View view(GuidePrimer.CraftSource source) {
        return switch (source) {
            case GuidePrimer.Craft craft -> manual(craft);
            case GuidePrimer.CraftRecipe recipe -> lookup(recipe.id());
        };
    }

    private static View manual(GuidePrimer.Craft craft) {
        List<List<ItemStack>> grid = empty();
        int rows = craft.pattern().size();
        int columns = craft.pattern().stream().mapToInt(String::length).max().orElse(0);
        int rowShift = (GRID - rows) / 2;
        int columnShift = (GRID - columns) / 2;
        for (int row = 0; row < rows; row++) {
            String line = craft.pattern().get(row);
            for (int column = 0; column < line.length(); column++) {
                Identifier id = craft.key().get(line.charAt(column));
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) continue;
                grid.set((row + rowShift) * GRID + column + columnShift, List.of(new ItemStack(BuiltInRegistries.ITEM.getValue(id))));
            }
        }
        ItemStack result = BuiltInRegistries.ITEM.containsKey(craft.result())
                ? new ItemStack(BuiltInRegistries.ITEM.getValue(craft.result()), craft.count()) : ItemStack.EMPTY;
        return new View(grid, result, null);
    }

    private static View lookup(Identifier id) {
        RecipeHolder<?> holder = recipes.byKey(ResourceKey.create(Registries.RECIPE, id));
        Minecraft minecraft = Minecraft.getInstance();
        if (holder == null || minecraft.level == null) return new View(empty(), ItemStack.EMPTY, id);
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        for (RecipeDisplay display : holder.value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                List<List<ItemStack>> grid = empty();
                int rowShift = (GRID - shaped.height()) / 2;
                int columnShift = (GRID - shaped.width()) / 2;
                for (int index = 0; index < shaped.ingredients().size(); index++) {
                    int row = index / shaped.width() + rowShift;
                    int column = index % shaped.width() + columnShift;
                    if (row < GRID && column < GRID) grid.set(row * GRID + column, stacks(shaped.ingredients().get(index), context));
                }
                return new View(grid, shaped.result().resolveForFirstStack(context), null);
            }
            if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                List<List<ItemStack>> grid = empty();
                List<List<ItemStack>> compact = shapeless.ingredients().size() <= SMALL * SMALL ? new ArrayList<>() : null;
                for (int index = 0; index < Math.min(shapeless.ingredients().size(), GRID * GRID); index++) {
                    List<ItemStack> options = stacks(shapeless.ingredients().get(index), context);
                    grid.set(index, options);
                    if (compact != null) compact.add(options);
                }
                while (compact != null && compact.size() < SMALL * SMALL) compact.add(List.of());
                return new View(grid, shapeless.result().resolveForFirstStack(context), null, compact);
            }
        }
        return new View(empty(), ItemStack.EMPTY, id);
    }

    private static List<ItemStack> stacks(SlotDisplay display, ContextMap context) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : display.resolveForStacks(context)) {
            if (!stack.isEmpty()) stacks.add(stack);
        }
        return stacks;
    }

    private static @Nullable List<List<ItemStack>> squeeze(List<List<ItemStack>> grid) {
        int top = GRID;
        int left = GRID;
        int bottom = -1;
        int right = -1;
        for (int index = 0; index < grid.size(); index++) {
            if (grid.get(index).isEmpty()) continue;
            top = Math.min(top, index / GRID);
            bottom = Math.max(bottom, index / GRID);
            left = Math.min(left, index % GRID);
            right = Math.max(right, index % GRID);
        }
        if (bottom - top >= SMALL || right - left >= SMALL) return null;
        List<List<ItemStack>> compact = new ArrayList<>();
        for (int row = 0; row < SMALL; row++) {
            for (int column = 0; column < SMALL; column++) {
                int source = (top + row) * GRID + left + column;
                compact.add(bottom < 0 || top + row > bottom || left + column > right ? List.of() : grid.get(source));
            }
        }
        return compact;
    }

    private static List<List<ItemStack>> empty() {
        List<List<ItemStack>> grid = new ArrayList<>();
        for (int index = 0; index < GRID * GRID; index++) grid.add(List.of());
        return grid;
    }
}
