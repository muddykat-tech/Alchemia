package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
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
        Integer crush = stack.get(DataComponentRegistry.CRUSH.get());
        return crush == null ? 0 : Math.max(0, Math.min(Ingredients.MAX_CRUSH_UNITS, crush));
    }

    public static double crushLevel(ItemStack stack) {
        return (double) crushOf(stack) / Ingredients.CRUSH_UNITS_PER_LEVEL;
    }

    public static boolean canCrush(ItemStack stack) {
        return stack.getItem() instanceof ItemIngredient && crushOf(stack) < Ingredients.MAX_CRUSH_UNITS;
    }

    public static void crush(ItemStack stack) {
        if (!canCrush(stack)) return;
        stack.set(DataComponentRegistry.CRUSH.get(), crushOf(stack) + 1);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltips, flag);

        tooltips.accept(TextUtils.brewingSource());

        int crush = crushOf(stack);
        tooltips.accept(Component.translatable("alchemia.tooltip.potency",
                String.format("%.2f", getPotency(stack))).withStyle(ChatFormatting.GRAY));
        tooltips.accept(Component.translatable("alchemia.tooltip.drift", describeDrift(getDriftX(stack), getDriftY(stack))).withStyle(ChatFormatting.DARK_GRAY));

        if (IngredientPath.of(ingredient).teleports()) {
            tooltips.accept(Component.translatable("alchemia.tooltip.teleport", PotionMap.teleportDistance(stack)).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltips.accept(Component.translatable("alchemia.tooltip.pathlength", pathCellCount(stack)).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (crush < Ingredients.MAX_CRUSH_UNITS) {
            tooltips.accept(Component.translatable("alchemia.tooltip.grind", crush, Ingredients.MAX_CRUSH_UNITS).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltips.accept(Component.translatable("alchemia.tooltip.ground").withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltips.accept(Component.translatable("alchemia.tooltip.path").withStyle(ChatFormatting.DARK_GRAY));
    }

    private List<int[]> pathOffsets(double potency) {
        int dx = alignmentX();
        int dy = alignmentY();
        IngredientPath shape = IngredientPath.of(ingredient);

        if (shape.needsDirection() && dx == 0 && dy == 0) return List.of();
        return IngredientPath.pathFor(ingredient, dx, dy, potency);
    }

    private int alignmentX() {
        return Integer.signum(ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX());
    }

    private int alignmentY() {
        return Integer.signum(ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY());
    }

    private int pathCellCount(ItemStack stack) {
        return pathOffsets(getPotency(stack)).size();
    }

    private static final double TELEPORT_PREVIEW_RADIUS = 8.0;

    private List<int[]> previewCells(double potency, double teleportScale) {
        IngredientPath shape = IngredientPath.of(ingredient);
        int dx = alignmentX();
        int dy = alignmentY();

        if (shape.teleports()) {
            int[] direction = shape == IngredientPath.SPIRAL_TELEPORT
                    ? IngredientPath.spiralDirection(potency)
                    : new int[]{dx, dy};
            if (direction[0] == 0 && direction[1] == 0) return List.of();
            return shape.teleportPreview(direction[0], direction[1], potency, teleportScale);
        }

        return pathOffsets(potency);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        IngredientPath shape = IngredientPath.of(ingredient);
        double maxPotency = ingredient.getPotency(Ingredients.MAX_CRUSH);
        double teleportScale = TELEPORT_PREVIEW_RADIUS / IngredientPath.teleportDistance(maxPotency);

        List<int[]> full = previewCells(maxPotency, teleportScale);
        if (full.isEmpty()) return Optional.empty();

        Component label = Component.translatable(shape.teleports()
                ? "alchemia.tooltip.label.teleport" : "alchemia.tooltip.label.path");

        int taken = previewCells(getPotency(stack), teleportScale).size();
        return Optional.of(new IngredientPathTooltip(full, Math.min(taken, full.size()), shape.teleports(), label));
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

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + this.ingredientType.name().toLowerCase() + "s/" + ingredient.name().toLowerCase());
    }

    public Identifier getCrushedTextureLocation() {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + this.ingredientType.name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "_crushed");
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
