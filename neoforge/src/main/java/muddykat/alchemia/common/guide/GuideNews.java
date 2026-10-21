package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketDiscovery;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class GuideNews {

    private static final int CARRIED_EVERY = 20;
    private static final int DESK_EVERY = 100;

    public enum Kind {
        INGREDIENT, REAGENT, EFFECT, SECRET, RECIPE, OPUS, CHAPTER;

        public static final StreamCodec<io.netty.buffer.ByteBuf, Kind> STREAM_CODEC =
                ByteBufCodecs.idMapper(index -> values()[index], Kind::ordinal);
    }

    public record Entry(Kind kind, String key, ItemStack icon, Component name) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                Kind.STREAM_CODEC, Entry::kind,
                ByteBufCodecs.STRING_UTF8, Entry::key,
                ItemStack.OPTIONAL_STREAM_CODEC, Entry::icon,
                ComponentSerialization.STREAM_CODEC, Entry::name,
                Entry::new);
    }

    public record Snapshot(ItemStack guide, Set<String> ingredients, Set<String> effects, Set<String> notes, Set<String> recipes,
                           Set<String> opus) {

        public static @Nullable Snapshot of(Iterable<ItemStack> guides) {
            for (ItemStack guide : guides) {
                return new Snapshot(guide, Set.copyOf(IngredientDiscovery.stored(guide)), Set.copyOf(RecipeDiscovery.stored(guide)),
                        Set.copyOf(WorldEffects.notesIn(guide)), recipeIds(guide), Set.copyOf(GreatWork.stored(guide)));
            }
            return null;
        }

        public List<Entry> news() {
            List<Entry> entries = new ArrayList<>();
            for (String key : IngredientDiscovery.stored(guide)) {
                if (!ingredients.contains(key)) ingredientEntry(key, entries);
            }

            Set<AlchemiaEffect> discovered = new LinkedHashSet<>();
            for (String name : RecipeDiscovery.stored(guide)) {
                AlchemiaEffect recipe = RecipeDiscovery.byName(name);
                if (effects.contains(name) || recipe == null) continue;
                discovered.add(recipe);
                entries.add(new Entry(Kind.EFFECT, recipe.id(), ItemStack.EMPTY, recipe.displayName()));
            }

            Set<AlchemiaEffect> secrets = new LinkedHashSet<>();
            for (String mark : WorldEffects.notesIn(guide)) {
                if (notes.contains(mark)) continue;
                AlchemiaEffect recipe = WorldEffects.recipeOfMark(mark);
                if (recipe != null && !discovered.contains(recipe)) secrets.add(recipe);
            }
            Set<AlchemiaEffect> known = RecipeDiscovery.knownTo(guide);
            for (AlchemiaEffect recipe : secrets) {
                Component name = known.contains(recipe) ? recipe.displayName()
                        : Component.translatable("alchemia.toast.unknown_effect");
                entries.add(new Entry(Kind.SECRET, recipe.id(), ItemStack.EMPTY, name));
            }

            for (BrewRecipe recipe : BrewRecipeBook.recipes(guide)) {
                if (!recipes.contains(recipe.id())) entries.add(new Entry(Kind.RECIPE, recipe.id(), potionIcon(recipe), recipe.displayName()));
            }

            for (String name : GreatWork.stored(guide)) {
                MagnumOpus.Stage stage = GreatWork.byName(name);
                if (opus.contains(name) || stage == null) continue;
                ItemStack result = GreatWork.result(stage);
                entries.add(new Entry(Kind.OPUS, name, result, result.getHoverName()));
            }
            return entries;
        }
    }

    private GuideNews() {
    }

    public static void announce(@Nullable Player player, @Nullable Snapshot before) {
        if (!(player instanceof ServerPlayer server) || before == null) return;
        List<Entry> entries = before.news();
        if (entries.isEmpty()) return;
        NetworkHandler.sendToPlayer(new PacketDiscovery(entries), server);
        GuideMastery.check(server, before.guide());
    }

    public static void survey(Player player, ItemStack guide) {
        List<ItemStack> carried = RecipeDiscovery.guidesOf(player);
        Snapshot before = !carried.isEmpty() && carried.getFirst() == guide ? Snapshot.of(List.of(guide)) : null;
        IngredientDiscovery.survey(player, guide);
        announce(player, before);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % CARRIED_EVERY != 0) return;

        Set<String> held = heldKeys(player);
        if (held.isEmpty()) return;

        List<ItemStack> carried = RecipeDiscovery.guidesOf(player);
        if (carried.isEmpty()) {
            if (player.tickCount % DESK_EVERY != 0) return;
        } else if (IngredientDiscovery.stored(carried.getFirst()).containsAll(held)) {
            return;
        }

        GuideShelf shelf = GuideShelf.around(player, player.level(), player.blockPosition());
        if (shelf.isEmpty()) return;
        for (ItemStack guide : shelf) IngredientDiscovery.learnKeys(guide, held);
        shelf.settle(player.blockPosition());
    }

    private static Set<String> heldKeys(Player player) {
        Set<String> keys = new LinkedHashSet<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            String key = IngredientDiscovery.keyOf(stack);
            if (key != null) keys.add(key);
        }
        return keys;
    }

    private static void ingredientEntry(String key, List<Entry> entries) {
        Ingredients ingredient = IngredientDiscovery.byName(key);
        if (ingredient != null) {
            ItemStack stack = new ItemStack(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()));
            entries.add(new Entry(Kind.INGREDIENT, key, stack, stack.getHoverName()));
            return;
        }
        Item reagent = IngredientDiscovery.reagentByKey(key);
        if (reagent == null) return;
        ItemStack stack = new ItemStack(reagent);
        entries.add(new Entry(Kind.REAGENT, key, stack, stack.getHoverName()));
    }

    private static Set<String> recipeIds(ItemStack guide) {
        Set<String> ids = new LinkedHashSet<>();
        for (BrewRecipe recipe : BrewRecipeBook.recipes(guide)) ids.add(recipe.id());
        return ids;
    }

    public static ItemStack potionIcon(BrewRecipe recipe) {
        ItemStack potion = new ItemStack(recipe.steps().contains(String.valueOf(BrewRecipe.STEP_SPLASH)) ? Items.SPLASH_POTION : Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(colourOf(recipe)), List.of(), Optional.empty()));
        if (recipe.steps().contains(String.valueOf(BrewRecipe.STEP_GLOW))) potion.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        muddykat.alchemia.common.potion.PotionGlasses.apply(potion, recipe.glass());
        return potion;
    }

    private static int colourOf(BrewRecipe recipe) {
        int red = 0, green = 0, blue = 0, count = 0;
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            AlchemiaEffect potion = effect.asPotion();
            if (potion == null || count == 3) continue;
            int colour = potion.color();
            red += (colour >> 16) & 255;
            green += (colour >> 8) & 255;
            blue += colour & 255;
            count++;
        }
        if (count == 0) return 0x385DC6;
        return (red / count) << 16 | (green / count) << 8 | blue / count;
    }
}
