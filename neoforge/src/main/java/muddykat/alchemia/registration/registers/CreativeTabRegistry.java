package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.AlchemicalPotions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Alchemia.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ALCHEMIA = CREATIVE_MODE_TABS.register("alchemia",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Alchemia.MODID))
                    .icon(() -> new ItemStack(BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get()))
                    .displayItems((parameters, output) -> {
                        ItemRegistry.ITEM_REGISTRY.values().forEach(output::accept);
                        for (Item bottle : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                            for (DeferredHolder<MobEffect, ? extends MobEffect> effect : MobEffectRegistry.MOB_EFFECTS.getEntries()) {
                                output.accept(AlchemicalPotions.of(bottle, effect, 1));
                            }
                        }
                    })
                    .build());
}
