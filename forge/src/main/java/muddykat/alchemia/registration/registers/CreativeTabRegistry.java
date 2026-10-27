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
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;

import java.util.List;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Alchemia.MODID);

    public static final RegistryObject<CreativeModeTab> ALCHEMIA = CREATIVE_MODE_TABS.register("alchemia",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Alchemia.MODID))
                    .icon(() -> new ItemStack(BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get()))
                    .displayItems((parameters, output) -> {
                        ItemRegistry.ITEM_REGISTRY.values().forEach(entry -> output.accept(entry.get()));
                        for (Item bottle : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                            for (RegistryObject<MobEffect> effect : MobEffectRegistry.MOB_EFFECTS.getEntries()) {
                                output.accept(AlchemicalPotions.of(bottle, effect.get(), 1));
                            }
                        }
                    })
                    .build());

    public static void hideHollow(BuildCreativeModeTabContentsEvent event) {
        List<ItemStack> hollow = new java.util.ArrayList<>();
        for (var entry : event.getEntries()) if (AlchemicalPotions.hollow(entry.getKey())) hollow.add(entry.getKey());
        for (ItemStack stack : hollow) event.getEntries().remove(stack);
    }
}
