package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.common.guide.GuideNews;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class GuideItems {

    private static final long PRIMER_CYCLE_MS = 1200L;

    private GuideItems() {
    }

    public static int cycle(int count) {
        return count <= 1 ? 0 : (int) (Util.getMillis() / PRIMER_CYCLE_MS % count);
    }

    public static ItemStack potionFor(BrewRecipe recipe) {
        return GuideNews.potionIcon(recipe);
    }

    public static @Nullable Item baseItem(BrewRecipe recipe) {
        for (muddykat.alchemia.common.potion.BrewBase.Fill fill : recipe.brewBase().fills()) {
            Item item = fill.itemValue();
            if (item != null) return item;
        }
        return null;
    }

    public static boolean splash(BrewRecipe recipe) {
        return recipe.steps().contains(String.valueOf(BrewRecipe.STEP_SPLASH));
    }

    public static boolean isReagent(Item item) {
        return IngredientDiscovery.isReagent(new ItemStack(item));
    }

    public static List<Item> sortedReagents(boolean lures) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<Item, VanillaIngredients.Drift> entry : VanillaIngredients.drifts().entrySet()) {
            if (entry.getValue().lure() == lures) items.add(entry.getKey());
        }
        items.sort(Comparator.comparing(item -> new ItemStack(item).getHoverName().getString()));
        return items;
    }

    public static List<Item> directReagents() {
        return sortedReagents(false);
    }

    public static List<Item> lureReagents() {
        return sortedReagents(true);
    }

    public static List<Item> generalReagents() {
        return directReagents();
    }

    public static String reagentKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static @Nullable Component treatment(ItemStack item) {
        List<Component> parts = new ArrayList<>();
        int crush = ItemIngredient.crushOf(item);
        if (crush > 0) parts.add(Component.translatable("alchemia.guide.step.ground", Math.round(100.0F * crush / Ingredients.MAX_CRUSH_UNITS)));
        if (ItemIngredient.trimOf(item) > 0) parts.add(Component.translatable("alchemia.guide.step.trimmed"));
        int rotation = ItemIngredient.rotationOf(item);
        if (rotation > 0) parts.add(Component.translatable("alchemia.guide.step.turned", rotation * 45));
        if (parts.isEmpty()) return null;

        Component joined = parts.getFirst();
        for (int index = 1; index < parts.size(); index++) {
            joined = Component.translatable("alchemia.guide.step.join", joined, parts.get(index));
        }
        return joined;
    }

    public static ItemStack tabletIcon() {
        return new ItemStack(ItemRegistry.getItemFromRegistry(ItemAlchemyTablet.ID));
    }

    public static ItemStack healingPotion() {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HEALING));
        return potion;
    }

    public static Component alignmentName(IngredientAlignment alignment) {
        return Component.translatable("alchemia.alignment." + alignment.name().toLowerCase());
    }

    public static ItemStack ingredientStack(Ingredients ingredient) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Alchemia.MODID, ingredient.getRegistryName())));
    }

    public static ItemStack flaskPotion(PotionEffectIngredient flask) {
        int red = 0, green = 0, blue = 0;
        for (PotionEffectIngredient.Requirement requirement : flask.required()) {
            int colour = requirement.effect().value().getColor();
            red += (colour >> 16) & 255;
            green += (colour >> 8) & 255;
            blue += colour & 255;
        }
        int count = Math.max(1, flask.required().size());
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(),
                Optional.of((red / count) << 16 | (green / count) << 8 | blue / count), List.of(), Optional.empty()));
        return potion;
    }

    public static List<Ingredients> ingredientsOf(IngredientType type) {
        List<Ingredients> items = new ArrayList<>();
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType() == type) items.add(ingredient);
        }
        items.sort(Comparator.comparing(ingredient -> ingredientStack(ingredient).getHoverName().getString()));
        return items;
    }
}
