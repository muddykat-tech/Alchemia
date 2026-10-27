package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.AlchemicalPotions;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

final class ShackBrews {

    private static final List<RegistryObject<MobEffect>> SPECIALS = List.of(MobEffectRegistry.RETURNING,
            MobEffectRegistry.TELEPORTATION, MobEffectRegistry.ATTRACTION, MobEffectRegistry.DANGERSENSE, MobEffectRegistry.THORNS,
            MobEffectRegistry.RECALL, MobEffectRegistry.WILD_GROWTH, MobEffectRegistry.FLAMES);

    private final ShackCanvas canvas;
    private final List<MobEffect> specials;

    ShackBrews(ShackCanvas canvas) {
        this.canvas = canvas;
        this.specials = shuffled(canvas.random(0, 0, 0, 91));
    }

    private static List<MobEffect> shuffled(RandomSource random) {
        List<MobEffect> pool = new ArrayList<>();
        for (RegistryObject<MobEffect> effect : SPECIALS) {
            if (AlchemiaEffects.byEffect(effect.get()) != null) pool.add(effect.get());
        }
        if (pool.isEmpty()) {
            for (RegistryObject<MobEffect> effect : SPECIALS) pool.add(effect.get());
        }
        for (int index = pool.size() - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            MobEffect held = pool.get(index);
            pool.set(index, pool.get(swap));
            pool.set(swap, held);
        }
        return pool;
    }

    ItemStack special(int slot) {
        MobEffect effect = specials.get(slot % specials.size());
        Item item = effect == MobEffectRegistry.FLAMES.get() ? Items.SPLASH_POTION : Items.POTION;
        return brew(item, effect, slot);
    }

    ItemStack treasureVision() {
        return brew(Items.POTION, MobEffectRegistry.TREASURE_VISION.get(), 40);
    }

    private ItemStack brew(Item item, MobEffect effect, int slot) {
        ItemStack stack = AlchemicalPotions.of(item, effect, 1 + canvas.pick(slot, 1, 0, 94, 2));
        PotionGlasses.apply(stack, ShackContents.glass(canvas, slot));
        return stack;
    }
}
