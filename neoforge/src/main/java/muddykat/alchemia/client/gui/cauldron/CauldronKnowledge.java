package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.guide.GuideShelf;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WhirlNotes;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class CauldronKnowledge {

    private Set<AlchemiaEffect> known = Set.of();
    private Set<String> notes = Set.of();
    private Set<String> whirlExits = Set.of();
    private Set<String> ingredients = Set.of();
    private List<ItemStack> guides = List.of();
    private boolean deskInReach;

    private Set<AlchemiaEffect> knownSeen;
    private Set<String> notesSeen;
    private Set<String> whirlExitsSeen;
    private Set<String> ingredientsSeen;

    public void refresh(@Nullable Player player, TileEntityAlchemyCauldron cauldron) {
        if (player == null || cauldron.getLevel() == null) {
            known = Set.of();
            notes = Set.of();
            whirlExits = Set.of();
            guides = List.of();
            ingredients = Set.of();
            deskInReach = false;
            return;
        }

        Set<AlchemiaEffect> learned = new LinkedHashSet<>();
        Set<String> noted = new HashSet<>();
        Set<String> handled = new HashSet<>();
        Set<String> exits = new HashSet<>();
        List<ItemStack> readable = GuideShelf.readable(player, cauldron.getLevel(), cauldron.getBlockPos());
        for (ItemStack guide : readable) {
            learned.addAll(RecipeDiscovery.knownTo(guide));
            noted.addAll(WorldEffects.notesIn(guide));
            handled.addAll(IngredientDiscovery.stored(guide));
            exits.addAll(WhirlNotes.exitsIn(guide));
        }
        guides = readable;
        known = learned;
        notes = noted;
        whirlExits = exits;
        ingredients = handled;
        deskInReach = readable.size() > RecipeDiscovery.guidesOf(player).size();
    }

    public void announce(Consumer<Component> notice) {
        watchWhirls(notice);
        watchNotes(notice);
        watchKnown(notice);
        watchIngredients(notice);
    }

    public Set<AlchemiaEffect> known() {
        return known;
    }

    public boolean knows(@Nullable AlchemiaEffect recipe) {
        return recipe != null && known.contains(recipe);
    }

    public Set<String> notes() {
        return notes;
    }

    public Set<String> whirlExits() {
        return whirlExits;
    }

    public List<ItemStack> guides() {
        return guides;
    }

    public boolean deskInReach() {
        return deskInReach;
    }

    public boolean noted(@Nullable AlchemiaEffect recipe) {
        return recipe != null && WorldEffects.anyVisible(notes, recipe);
    }

    public @Nullable Component dormantHint(AlchemiaEffect recipe) {
        WorldEffects.Requirement requirement = WorldEffects.requirement(recipe);
        return requirement != null && WorldEffects.wakeVisible(notes, recipe) ? requirement.hint() : null;
    }

    public @Nullable Component levelHint(AlchemiaEffect recipe, int tier) {
        return WorldEffects.tierVisible(notes, recipe, tier) ? WorldEffects.levelHint(recipe, tier) : null;
    }

    private void watchNotes(Consumer<Component> notice) {
        if (notesSeen == null) {
            notesSeen = new HashSet<>(notes);
            return;
        }

        Component news = WorldEffects.newsOf(notesSeen, notes);
        if (news != null) notice.accept(Component.translatable("alchemia.gui.noted", news));
        notesSeen = new HashSet<>(notes);
    }

    private void watchWhirls(Consumer<Component> notice) {
        if (whirlExitsSeen == null) {
            whirlExitsSeen = new HashSet<>(whirlExits);
            return;
        }
        if (!whirlExitsSeen.containsAll(whirlExits)) notice.accept(Component.translatable("alchemia.gui.whirl.noted"));
        whirlExitsSeen = new HashSet<>(whirlExits);
    }

    private void watchIngredients(Consumer<Component> notice) {
        if (ingredientsSeen == null) {
            ingredientsSeen = new HashSet<>(ingredients);
            return;
        }

        Component added = null;
        for (String key : ingredients) {
            if (ingredientsSeen.contains(key)) continue;
            Component name = ingredientName(key);
            if (name == null) continue;
            added = added == null ? name : Component.empty().append(added).append(", ").append(name);
        }
        if (added != null) notice.accept(Component.translatable("alchemia.gui.recorded", added));
        ingredientsSeen = new HashSet<>(ingredients);
    }

    private void watchKnown(Consumer<Component> notice) {
        if (knownSeen == null) {
            knownSeen = new HashSet<>(known);
            return;
        }

        Component added = null;
        for (AlchemiaEffect recipe : known) {
            if (knownSeen.contains(recipe)) continue;

            Component name = recipe.displayName();
            added = added == null ? name : Component.empty().append(added).append(", ").append(name);
        }

        if (added != null) notice.accept(Component.translatable("alchemia.gui.recorded", added));
        knownSeen = new HashSet<>(known);
    }

    private static @Nullable Component ingredientName(String key) {
        Ingredients ingredient = IngredientDiscovery.byName(key);
        if (ingredient != null) {
            return new ItemStack(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())).getHoverName();
        }
        Item reagent = IngredientDiscovery.reagentByKey(key);
        return reagent == null ? null : new ItemStack(reagent).getHoverName();
    }
}
