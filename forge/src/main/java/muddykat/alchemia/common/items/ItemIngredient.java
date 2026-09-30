package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.GuideAccess;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.utility.StackData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import java.util.function.Consumer;

public class ItemIngredient extends Item {

    protected final Ingredients ingredient;
    protected final IngredientType ingredientType;
    protected final IngredientAlignment primaryAlignment;

    public ItemIngredient(Ingredients ingredient, IngredientType type, IngredientAlignment alignment, Properties properties) {
        super(ingredient.applyFoodProperties(properties));
        this.ingredientType = type;
        this.primaryAlignment = alignment;
        this.ingredient = ingredient;
    }

    public static int crushOf(ItemStack stack) {
        Integer crush = StackData.CRUSH.get(stack);
        return crush == null ? 0 : Math.max(0, Math.min(Ingredients.MAX_CRUSH_UNITS, crush));
    }

    public static double crushLevel(ItemStack stack) {
        return (double) crushOf(stack) / Ingredients.CRUSH_UNITS_PER_LEVEL;
    }

    public static boolean canCrush(ItemStack stack) {
        return stack.getItem() instanceof ItemIngredient && crushOf(stack) < Ingredients.MAX_CRUSH_UNITS;
    }

    public static int trimOf(ItemStack stack) {
        Integer trim = StackData.PATH_TRIM.get(stack);
        return trim == null ? 0 : Math.max(0, trim);
    }

    public static boolean canTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient item)) return false;
        return IngredientPath.of(item.ingredient).teleports()
                ? PotionMap.teleportReach(stack) > IngredientPath.TRIM_STEP
                : item.pathSpan(stack) > IngredientPath.TRIM_STEP;
    }

    public static int rotationOf(ItemStack stack) {
        Integer rotation = StackData.PATH_ROTATION.get(stack);
        return rotation == null ? 0 : Math.floorMod(rotation, IngredientPath.ROTATION_STEPS);
    }

    public static boolean canRotate(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient item)) return false;
        return IngredientPath.of(item.ingredient).teleports() || item.pathSpan(stack) > 0;
    }

    public static void rotate(ItemStack stack, int steps) {
        if (!canRotate(stack)) return;
        StackData.PATH_ROTATION.set(stack, Math.floorMod(rotationOf(stack) + steps, IngredientPath.ROTATION_STEPS));
    }

    public static void trim(ItemStack stack) {
        if (!canTrim(stack)) return;
        StackData.PATH_TRIM.set(stack, trimOf(stack) + 1);
    }

    public static void crush(ItemStack stack) {
        if (!canCrush(stack)) return;
        StackData.CRUSH.set(stack, crushOf(stack) + 1);
    }

    public double getPotency(ItemStack stack) {
        return ingredient.getPotency(crushLevel(stack));
    }

    public int getDriftX(ItemStack stack) {
        return ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX();
    }

    public int getDriftY(ItemStack stack) {
        return ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return crushOf(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * crushOf(stack) / Ingredients.MAX_CRUSH_UNITS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float progress = (float) crushOf(stack) / Ingredients.MAX_CRUSH_UNITS;
        int red = Math.round(201 - progress * 94);
        int green = Math.round(169 - progress * 95);
        int blue = Math.round(110 - progress * 70);
        return (red << 16) | (green << 8) | blue;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltips, flag);

        if (!GuideAccess.reading()) return;

        tooltips.add(TextUtils.brewingSource());

        int crush = crushOf(stack);
        tooltips.add(Component.translatable("alchemia.tooltip.potency",
                String.format("%.2f", getPotency(stack))).withStyle(ChatFormatting.GRAY));
        tooltips.add(Component.translatable("alchemia.tooltip.drift", describeDrift(getDriftX(stack), getDriftY(stack))).withStyle(ChatFormatting.DARK_GRAY));

        if (IngredientPath.of(ingredient).teleports()) {
            tooltips.add(Component.translatable("alchemia.tooltip.teleport", PotionMap.teleportDistance(stack)).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltips.add(Component.translatable("alchemia.tooltip.pathlength", Math.round(pathSpan(stack))).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (crush < Ingredients.MAX_CRUSH_UNITS) {
            tooltips.add(Component.translatable("alchemia.tooltip.grind", crush, Ingredients.MAX_CRUSH_UNITS).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltips.add(Component.translatable("alchemia.tooltip.ground").withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltips.add(Component.translatable("alchemia.tooltip.path").withStyle(ChatFormatting.DARK_GRAY));

        int trim = trimOf(stack);
        if (trim > 0) {
            tooltips.add(Component.translatable("alchemia.tooltip.path.shortened", trim).withStyle(ChatFormatting.DARK_GRAY));
        }

        int rotation = rotationOf(stack);
        if (rotation > 0) {
            tooltips.add(Component.translatable("alchemia.tooltip.path.rotated", rotation * 45).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private List<PathPoint> pathOffsets(double potency, int trim, int rotation) {
        int dx = alignmentX();
        int dy = alignmentY();
        IngredientPath shape = IngredientPath.of(ingredient);

        if (shape.needsDirection() && dx == 0 && dy == 0) return List.of();
        return IngredientPath.pathFor(ingredient, dx, dy, potency, trim, rotation);
    }

    private int alignmentX() {
        return Integer.signum(ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX());
    }

    private int alignmentY() {
        return Integer.signum(ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY());
    }

    private double pathSpan(ItemStack stack) {
        return PathGeometry.length(pathOffsets(getPotency(stack), trimOf(stack), rotationOf(stack)));
    }

    public static double pathLength(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient item)) return 0.0;
        return IngredientPath.of(item.ingredient).teleports()
                ? PotionMap.teleportReach(stack)
                : item.pathSpan(stack);
    }

    public List<PathPoint> previewPath(double potency, int trim, int rotation) {
        IngredientPath shape = IngredientPath.of(ingredient);

        if (!shape.teleports()) return pathOffsets(potency, trim, rotation);

        double ceiling = IngredientPath.teleportCeiling(ingredient);
        double reach = IngredientPath.reachFor(potency, trim);

        int[] direction = shape == IngredientPath.SPIRAL_TELEPORT
                ? IngredientPath.spiralDirection(ceiling, reach)
                : new int[]{alignmentX(), alignmentY()};
        if (direction[0] == 0 && direction[1] == 0) return List.of();

        return IngredientPath.rotated(
                shape.teleportPreview(direction[0], direction[1], ceiling, reach), rotation);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!GuideAccess.reading()) return Optional.empty();

        IngredientPath shape = IngredientPath.of(ingredient);
        double maxPotency = ingredient.getPotency(Ingredients.MAX_CRUSH);

        List<PathPoint> full = previewPath(maxPotency, trimOf(stack), rotationOf(stack));
        if (full.size() < 2) return Optional.empty();

        Component label = Component.translatable(shape.teleports()
                ? "alchemia.tooltip.label.teleport" : "alchemia.tooltip.label.path");

        double taken = PathGeometry.length(previewPath(getPotency(stack), trimOf(stack), rotationOf(stack)));
        return Optional.of(new IngredientPathTooltip(full,
                Math.min(taken, PathGeometry.length(full)), shape.teleports(), label));
    }

    public static Component describeDrift(int driftX, int driftY) {
        if (driftX == 0 && driftY == 0) {
            return Component.translatable("alchemia.tooltip.inert").withStyle(ChatFormatting.DARK_GRAY);
        }

        MutableComponent line = Component.empty();
        boolean first = true;

        if (driftX != 0) {
            line.append(axis(driftX < 0 ? "fire" : "water", Math.abs(driftX), driftX < 0 ? ChatFormatting.RED : ChatFormatting.AQUA));
            first = false;
        }
        if (driftY != 0) {
            if (!first) line.append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY));
            line.append(axis(driftY < 0 ? "air" : "earth", Math.abs(driftY), driftY < 0 ? ChatFormatting.WHITE : ChatFormatting.GREEN));
        }
        return line;
    }

    private static Component axis(String key, int amount, ChatFormatting colour) {
        return Component.translatable("alchemia.alignment." + key).append(Component.literal(" " + amount)).withStyle(colour);
    }

    public IngredientType getIngredientType() {
        return this.ingredientType;
    }

    public IngredientAlignment getPrimaryAlignment() {
        return this.primaryAlignment;
    }

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(Alchemia.MODID, "item/" + this.ingredientType.name().toLowerCase() + "s/" + ingredient.name().toLowerCase());
    }

    public ResourceLocation getCrushedTextureLocation() {
        return new ResourceLocation(Alchemia.MODID, "item/" + this.ingredientType.name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "_crushed");
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
