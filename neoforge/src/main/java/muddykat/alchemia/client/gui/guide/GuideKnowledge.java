package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.items.ItemRecipePage;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class GuideKnowledge {

    private ItemStack stack = ItemStack.EMPTY;
    private List<BrewRecipe> recipes = List.of();
    private Set<AlchemiaEffect> discovered = Set.of();
    private Set<String> notes = Set.of();
    private List<Ingredients> knownIngredients = List.of();
    private Set<Item> knownReagents = Set.of();
    private Set<String> greatWork = Set.of();
    private @Nullable String selected;
    private @Nullable String bookmark;

    public boolean readPage(ItemStack page, @Nullable Player player) {
        stack = page;
        if (stack.isEmpty()) return false;

        BrewRecipe recipe = ItemRecipePage.recipe(stack);
        recipes = recipe == null ? List.of() : List.of(recipe);
        discovered = RecipeDiscovery.knownTo(player);
        notes = WorldEffects.notesOf(player);
        knownIngredients = List.of();
        knownReagents = Set.of();
        greatWork = Set.of();
        selected = null;
        bookmark = null;
        return recipe != null;
    }

    public boolean readGuide(ItemStack guide) {
        stack = guide;
        if (stack.isEmpty()) return false;

        recipes = BrewRecipeBook.recipes(stack);
        discovered = RecipeDiscovery.knownTo(stack);
        notes = WorldEffects.notesIn(stack);
        selected = BrewRecipeBook.selected(stack);
        bookmark = stack.get(DataComponentRegistry.GUIDE_BOOKMARK.get());
        knownReagents = IngredientDiscovery.knownReagents(stack);
        greatWork = Set.copyOf(GreatWork.stored(stack));
        knownIngredients = new ArrayList<>(IngredientDiscovery.knownTo(stack));
        knownIngredients.sort(Comparator.comparing(ingredient -> GuideItems.ingredientStack(ingredient).getHoverName().getString()));
        return true;
    }

    public ItemStack stack() {
        return stack;
    }

    public List<BrewRecipe> recipes() {
        return recipes;
    }

    public Set<AlchemiaEffect> discovered() {
        return discovered;
    }

    public Set<String> notes() {
        return notes;
    }

    public List<Ingredients> knownIngredients() {
        return knownIngredients;
    }

    public Set<Item> knownReagents() {
        return knownReagents;
    }

    public Set<String> greatWork() {
        return greatWork;
    }

    public @Nullable String selected() {
        return selected;
    }

    public void select(@Nullable String id) {
        selected = id;
    }

    public @Nullable String bookmark() {
        return bookmark;
    }

    public void mark(@Nullable String place) {
        bookmark = place;
    }

    public @Nullable BrewRecipe find(String id) {
        return BrewRecipeBook.find(stack, id);
    }

    public Component effectSummary(BrewRecipe recipe) {
        List<String> parts = new ArrayList<>();
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            parts.add(effectLabel(effect).getString());
        }
        return Component.literal(String.join(", ", parts));
    }

    public Component effectLabel(BrewRecipe.BrewEffect effect) {
        AlchemiaEffect potion = effect.asPotion();
        Component name = potion != null && !discovered.contains(potion)
                ? Component.translatable("alchemia.guide.effect.unknown") : effect.displayName();
        return Component.translatable("alchemia.guide.effect.line", name, Component.translatable("enchantment.level." + effect.potency()));
    }

    public List<Component> effectTooltip(BrewRecipe.BrewEffect effect, @Nullable AlchemiaEffect potion) {
        List<Component> lines = new ArrayList<>();
        lines.add(effectLabel(effect));
        if (potion != null) lines.addAll(WorldEffects.guideLines(notes, potion));
        return lines;
    }

    public List<AlchemiaEffect> effectOrder() {
        List<AlchemiaEffect> known = new ArrayList<>();
        List<AlchemiaEffect> noted = new ArrayList<>();
        for (AlchemiaEffect potion : AlchemiaEffects.values()) {
            if (discovered.contains(potion)) known.add(potion);
            else if (WorldEffects.anyVisible(notes, potion)) noted.add(potion);
        }
        known.sort(Comparator.comparing(potion -> potion.displayName().getString()));
        known.addAll(noted);
        return known;
    }

    public Component effectName(AlchemiaEffect potion) {
        return discovered.contains(potion) ? potion.displayName()
                : Component.translatable("alchemia.guide.effect.unknown");
    }

    public List<Component> flaskNames(PotionEffectIngredient flask) {
        List<Component> names = new ArrayList<>();
        for (PotionEffectIngredient.Requirement requirement : flask.required()) {
            AlchemiaEffect known = RecipeDiscovery.byEffect(requirement.effect());
            Component effectName = known != null && discovered.contains(known) ? requirement.effect().value().getDisplayName()
                    : Component.translatable("alchemia.guide.effect.unknown");
            names.add(Component.translatable("alchemia.guide.opus.flask.effect", effectName,
                    Component.translatable("enchantment.level." + (requirement.amplifier() + 1))));
        }
        return names;
    }

    public int knownIngredients(List<Ingredients> items) {
        int count = 0;
        for (Ingredients ingredient : items) {
            if (knownIngredients.contains(ingredient)) count++;
        }
        return count;
    }
}
