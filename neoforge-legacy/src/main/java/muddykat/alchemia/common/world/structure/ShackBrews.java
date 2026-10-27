package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.AlchemicalPotions;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

final class ShackBrews {

    private static final List<DeferredHolder<MobEffect, MobEffect>> SPECIALS = List.of(MobEffectRegistry.RETURNING,
            MobEffectRegistry.TELEPORTATION, MobEffectRegistry.ATTRACTION, MobEffectRegistry.DANGERSENSE, MobEffectRegistry.THORNS,
            MobEffectRegistry.RECALL, MobEffectRegistry.WILD_GROWTH, MobEffectRegistry.FLAMES);

    private final ShackCanvas canvas;
    private final List<Holder<MobEffect>> specials;

    ShackBrews(ShackCanvas canvas) {
        this.canvas = canvas;
        this.specials = shuffled(canvas.random(0, 0, 0, 91));
    }

    private static List<Holder<MobEffect>> shuffled(RandomSource random) {
        List<Holder<MobEffect>> pool = new ArrayList<>();
        for (DeferredHolder<MobEffect, MobEffect> effect : SPECIALS) {
            if (AlchemiaEffects.byEffect(effect) != null) pool.add(effect);
        }
        if (pool.isEmpty()) pool.addAll(SPECIALS);
        for (int index = pool.size() - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            Holder<MobEffect> held = pool.get(index);
            pool.set(index, pool.get(swap));
            pool.set(swap, held);
        }
        return pool;
    }

    ItemStack special(int slot) {
        Holder<MobEffect> effect = specials.get(slot % specials.size());
        Item item = effect.is(MobEffectRegistry.FLAMES.getKey()) ? Items.SPLASH_POTION : Items.POTION;
        return brew(item, effect, slot);
    }

    ItemStack treasureVision() {
        return brew(Items.POTION, MobEffectRegistry.TREASURE_VISION, 40);
    }

    private ItemStack brew(Item item, Holder<MobEffect> effect, int slot) {
        ItemStack stack = AlchemicalPotions.of(item, effect, 1 + canvas.pick(slot, 1, 0, 94, 2));
        PotionGlasses.apply(stack, ShackContents.glass(canvas, slot));
        return stack;
    }
}
