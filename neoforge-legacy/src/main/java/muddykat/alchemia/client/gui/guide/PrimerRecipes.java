package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PrimerRecipes {

    private static final int GRID = 3;
    static final int SMALL = 2;

    record View(List<List<ItemStack>> grid, ItemStack result, @Nullable ResourceLocation missing, @Nullable List<List<ItemStack>> compact) {
        View(List<List<ItemStack>> grid, ItemStack result, @Nullable ResourceLocation missing) {
            this(grid, result, missing, squeeze(grid));
        }

        boolean fitsSmall() {
            return compact != null;
        }

        List<List<ItemStack>> cells(int size) {
            return size == SMALL && compact != null ? compact : grid;
        }
    }

    private PrimerRecipes() {
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
                ResourceLocation id = craft.key().get(line.charAt(column));
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) continue;
                grid.set((row + rowShift) * GRID + column + columnShift, List.of(new ItemStack(BuiltInRegistries.ITEM.get(id))));
            }
        }
        ItemStack result = BuiltInRegistries.ITEM.containsKey(craft.result())
                ? new ItemStack(BuiltInRegistries.ITEM.get(craft.result()), craft.count()) : ItemStack.EMPTY;
        return new View(grid, result, null);
    }

    private static View lookup(ResourceLocation id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return new View(empty(), ItemStack.EMPTY, id);
        RecipeHolder<?> holder = minecraft.level.getRecipeManager().byKey(id).orElse(null);
        if (holder == null) return new View(empty(), ItemStack.EMPTY, id);
        Recipe<?> recipe = holder.value();
        ItemStack result = recipe.getResultItem(minecraft.level.registryAccess()).copy();
        if (recipe instanceof ShapedRecipe shaped) {
            List<List<ItemStack>> grid = empty();
            int rowShift = (GRID - shaped.getHeight()) / 2;
            int columnShift = (GRID - shaped.getWidth()) / 2;
            for (int index = 0; index < shaped.getIngredients().size(); index++) {
                int row = index / shaped.getWidth() + rowShift;
                int column = index % shaped.getWidth() + columnShift;
                if (row < GRID && column < GRID) grid.set(row * GRID + column, stacks(shaped.getIngredients().get(index)));
            }
            return new View(grid, result, null);
        }
        if (recipe instanceof ShapelessRecipe shapeless) {
            List<List<ItemStack>> grid = empty();
            List<List<ItemStack>> compact = shapeless.getIngredients().size() <= SMALL * SMALL ? new ArrayList<>() : null;
            for (int index = 0; index < Math.min(shapeless.getIngredients().size(), GRID * GRID); index++) {
                List<ItemStack> options = stacks(shapeless.getIngredients().get(index));
                grid.set(index, options);
                if (compact != null) compact.add(options);
            }
            while (compact != null && compact.size() < SMALL * SMALL) compact.add(List.of());
            return new View(grid, result, null, compact);
        }
        return new View(empty(), ItemStack.EMPTY, id);
    }

    private static List<ItemStack> stacks(Ingredient ingredient) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : ingredient.getItems()) {
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
