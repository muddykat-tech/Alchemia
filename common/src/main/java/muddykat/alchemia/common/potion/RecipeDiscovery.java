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
        return record(guidesOf(player), effects);
    }

    public static int record(Iterable<ItemStack> guides, Collection<MobEffectInstance> effects) {
        List<String> brewed = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            PotionEnum recipe = byEffect(instance.getEffect());
            if (recipe != null) brewed.add(recipe.name());
        }
        return learn(guides, brewed);
    }

    public static int learn(Iterable<ItemStack> guides, Collection<String> names) {
        if (names.isEmpty()) return 0;

        int discovered = 0;
        for (ItemStack stack : guides) {
            List<String> known = new ArrayList<>(stored(stack));
            int before = known.size();
            for (String name : names) {
                if (byName(name) != null && !known.contains(name)) known.add(name);
            }
            if (known.size() == before) continue;

            known.sort(null);
            stack.set(DataComponentRegistry.DISCOVERED_RECIPES.get(), List.copyOf(known));
            discovered = Math.max(discovered, known.size() - before);
        }
        return discovered;
    }

    public static List<ItemStack> guidesOf(Player player) {
        List<ItemStack> guides = new ArrayList<>();
        if (player == null) return guides;
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() instanceof ItemAlchemiaGuide) guides.add(stack);
        }
        return guides;
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
