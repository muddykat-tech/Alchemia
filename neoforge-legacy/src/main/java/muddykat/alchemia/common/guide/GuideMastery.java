package muddykat.alchemia.common.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
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

    public static void checkShack(ServerPlayer player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        for (net.minecraft.world.level.levelgen.structure.Structure structure : level.structureManager().getAllStructuresAt(player.blockPosition()).keySet()) {
            if (structure instanceof muddykat.alchemia.common.world.structure.ForgottenShackStructure) {
                award(player, "forgotten_shack", true);
                return;
            }
        }
    }

    public static String restoredName(MachineOwner owner) {
        return "restored_" + owner.getSerializedName();
    }

    public static void restored(ServerPlayer player, MachineOwner owner) {
        award(player, restoredName(owner), true);
    }

    public static boolean hasRestored(ServerPlayer player, MachineOwner owner) {
        if (player.getServer() == null) return false;
        net.minecraft.advancements.AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, restoredName(owner)));
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static Set<AlchemiaEffect> brewable() {
        Set<AlchemiaEffect> potions = new java.util.TreeSet<>(AlchemiaEffects.ORDER);
        for (AlchemiaEffect potion : AlchemiaEffects.values()) {
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
        if (!earned || player.getServer() == null) return;
        net.minecraft.advancements.AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, name));
        if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, CRITERION);
        }
    }
}
