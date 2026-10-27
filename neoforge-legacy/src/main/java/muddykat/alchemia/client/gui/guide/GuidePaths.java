package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.PathPanel;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class GuidePaths {

    private GuidePaths() {
    }

    public record PathVisual(PathPanel panel, Component label) {
    }

    public static @Nullable double[] grindTable(Ingredients ingredient) {
        double[] taken = new double[Ingredients.MAX_CRUSH_UNITS + 1];
        ItemStack stack = GuideItems.ingredientStack(ingredient);
        if (!(stack.getItem() instanceof ItemIngredient item)) return null;
        for (int units = 0; units <= Ingredients.MAX_CRUSH_UNITS; units++) {
            taken[units] = PathGeometry.length(item.previewPath(ingredient.getPotencyForUnits(units), 0, 0));
        }
        return taken;
    }

    public static PathPanel driftPanel(VanillaIngredients.Drift drift) {
        PotionMap map = PotionMap.get(BrewBases.defaultBase());
        List<PathPoint> full = map.fixedDriftOffsets(drift.target(), Ingredients.MAX_CRUSH_UNITS);
        double base = PathGeometry.length(map.fixedDriftOffsets(drift.target(), 0));
        return new PathPanel(Component.translatable("alchemia.tooltip.label.path"), full, base, false, 96, 44);
    }

    public static @Nullable double[] driftGrindTable(VanillaIngredients.Drift drift) {
        PotionMap map = PotionMap.get(BrewBases.defaultBase());
        double[] taken = new double[Ingredients.MAX_CRUSH_UNITS + 1];
        for (int units = 0; units <= Ingredients.MAX_CRUSH_UNITS; units++) {
            taken[units] = PathGeometry.length(map.fixedDriftOffsets(drift.target(), units));
        }
        return taken;
    }

    public static @Nullable PathPanel pathPanel(Ingredients ingredient) {
        PathVisual visual = pathVisual(ingredient);
        return visual == null ? null : visual.panel();
    }

    public static @Nullable PathVisual pathVisual(Ingredients ingredient) {
        IngredientPath shape = IngredientPath.of(ingredient);
        int dx = Integer.signum(ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX());
        int dy = Integer.signum(ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY());
        Component label = Component.translatable(shape.teleports() ? "alchemia.tooltip.label.teleport" : "alchemia.tooltip.label.path");
        ItemStack stack = GuideItems.ingredientStack(ingredient);
        if (stack.getItem() instanceof ItemIngredient item) {
            Optional<TooltipComponent> tooltip = item.getTooltipImage(stack);
            if (tooltip.orElse(null) instanceof IngredientPathTooltip path) {
                PathPanel panel = new PathPanel(path.label(), path.points(), path.taken(), path.teleport(), 96, 44);
                if (panel.drawable()) return new PathVisual(panel, path.label());
            }
        }

        if (shape.needsDirection() && dx == 0 && dy == 0) return null;
        double ceiling = IngredientPath.teleportCeiling(ingredient);
        List<PathPoint> full = shape.teleports()
                ? shape.teleportPreview(dx, dy, ceiling, ceiling)
                : IngredientPath.pathFor(ingredient, dx, dy, ingredient.getPotency(Ingredients.MAX_CRUSH));
        List<PathPoint> base = shape.teleports()
                ? shape.teleportPreview(dx, dy, ceiling, IngredientPath.reachFor(ingredient.getPotency(0), 0))
                : IngredientPath.pathFor(ingredient, dx, dy, ingredient.getPotency(0));
        PathPanel panel = new PathPanel(label, full, PathGeometry.length(base), shape.teleports(), 96, 44);
        return panel.drawable() ? new PathVisual(panel, label) : null;
    }

    public static @Nullable Ingredients primerPathIngredient(String itemName) {
        String name = itemName;
        int separator = name.indexOf(':');
        if (separator >= 0) {
            if (!name.substring(0, separator).equals(Alchemia.MODID)) return null;
            name = name.substring(separator + 1);
        }
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getRegistryName().equalsIgnoreCase(name)) return ingredient;
        }
        return null;
    }
}
