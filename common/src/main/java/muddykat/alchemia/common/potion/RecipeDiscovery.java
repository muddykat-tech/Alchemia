package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RecipeDiscovery {

    public static List<String> stored(ItemStack guide) {
        return guide.getOrDefault(DataComponentRegistry.DISCOVERED_RECIPES.get(), List.of());
    }

    public static Set<PotionEnum> knownTo(ItemStack guide) {
        Set<PotionEnum> known = EnumSet.noneOf(PotionEnum.class);
        for (String name : stored(guide)) {
            PotionEnum recipe = byName(name);
            if (recipe != null) known.add(recipe);
        }
        return known;
    }

    public static Set<PotionEnum> knownTo(Player player) {
        Set<PotionEnum> known = EnumSet.noneOf(PotionEnum.class);
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() instanceof ItemAlchemiaGuide) known.addAll(knownTo(stack));
        }
        return known;
    }

    public static int record(Player player, Collection<MobEffectInstance> effects) {
        List<PotionEnum> brewed = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            PotionEnum recipe = byEffect(instance.getEffect());
            if (recipe != null) brewed.add(recipe);
        }
        if (brewed.isEmpty()) return 0;

        int discovered = 0;
        for (ItemStack stack : player.getInventory()) {
            if (!(stack.getItem() instanceof ItemAlchemiaGuide)) continue;

            List<String> names = new ArrayList<>(stored(stack));
            int before = names.size();
            for (PotionEnum recipe : brewed) {
                if (!names.contains(recipe.name())) names.add(recipe.name());
            }
            if (names.size() != before) {
                names.sort(null);
                stack.set(DataComponentRegistry.DISCOVERED_RECIPES.get(), List.copyOf(names));
                discovered = Math.max(discovered, names.size() - before);
            }
        }
        return discovered;
    }

    public static PotionEnum byName(String name) {
        for (PotionEnum recipe : PotionEnum.values()) {
            if (recipe.name().equals(name)) return recipe;
        }
        return null;
    }

    public static PotionEnum byEffect(Holder<MobEffect> effect) {
        for (PotionEnum recipe : PotionEnum.values()) {
            if (recipe.getEffect().equals(effect)) return recipe;
        }
        return null;
    }

    public static int total() {
        return PotionEnum.values().length;
    }
}
