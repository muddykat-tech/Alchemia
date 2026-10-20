package muddykat.alchemia.common.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class GuideMastery {

    private static final String CRITERION = "unlocked";

    private GuideMastery() {
    }

    public static void check(ServerPlayer player, ItemStack guide) {
        boolean effects = RecipeDiscovery.knownTo(guide).containsAll(brewable());
        boolean secrets = WorldEffects.notesIn(guide).containsAll(WorldEffects.allMarks());
        boolean ingredients = IngredientDiscovery.found(guide) >= IngredientDiscovery.totalAll();
        boolean stages = GreatWork.known(guide) >= GreatWork.total();
        award(player, "all_effects", effects);
        award(player, "all_secrets", secrets);
        award(player, "all_ingredients", ingredients);
        award(player, "all_stages", stages);
        award(player, "complete_guide", effects && secrets && ingredients && stages);
    }

    private static Set<AlchemiaEffect> brewable() {
        Set<AlchemiaEffect> potions = new java.util.LinkedHashSet<>();
        for (AlchemiaEffect potion : muddykat.alchemia.common.potion.AlchemiaEffects.values()) {
            for (BrewBase base : BrewBases.values()) {
                if (base.allows(potion)) {
                    potions.add(potion);
                    break;
                }
            }
        }
        return potions;
    }

    private static void award(ServerPlayer player, String name, boolean earned) {
        if (!earned || player.level().getServer() == null) return;
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(Alchemia.MODID, name));
        if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, CRITERION);
        }
    }
}
