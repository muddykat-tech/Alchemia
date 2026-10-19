package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import muddykat.alchemia.registration.registers.ItemRegistry;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.data.AlchemiaItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

public class AlchemiaItemTagsProvider extends TagsProvider<Item> {

    public AlchemiaItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, registries, Alchemia.MODID, existingFileHelper);
    }

    private static ResourceKey<Item> itemKey(String id) {
        return ItemRegistry.getItemFromRegistry(id).asItem().builtInRegistryHolder().key();
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (String log : AncientOak.LOGS) {
            tag(AlchemiaItemTags.ANCIENT_OAK_LOGS).add(itemKey(log));
            tag(ItemTags.LOGS_THAT_BURN).add(itemKey(log));
        }
        tag(ItemTags.PLANKS).add(itemKey(AncientOak.PLANKS));
        tag(ItemTags.WOODEN_STAIRS).add(itemKey(AncientOak.STAIRS));
        tag(ItemTags.WOODEN_SLABS).add(itemKey(AncientOak.SLAB));
        tag(ItemTags.WOODEN_FENCES).add(itemKey(AncientOak.FENCE));
        tag(ItemTags.FENCE_GATES).add(itemKey(AncientOak.FENCE_GATE));
        tag(ItemTags.WOODEN_DOORS).add(itemKey(AncientOak.DOOR));
        tag(ItemTags.WOODEN_TRAPDOORS).add(itemKey(AncientOak.TRAPDOOR));
        tag(ItemTags.WOODEN_BUTTONS).add(itemKey(AncientOak.BUTTON));
        tag(ItemTags.WOODEN_PRESSURE_PLATES).add(itemKey(AncientOak.PRESSURE_PLATE));
        tag(ItemTags.LEAVES).add(itemKey(AncientOak.LEAVES));
        tag(ItemTags.SAPLINGS).add(itemKey(AncientOak.SAPLING));

        var appender = tag(AlchemiaItemTags.INGREDIENTS);

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof ItemIngredient)) continue;

            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && id.getNamespace().equals(Alchemia.MODID)) {
                appender.add(item.builtInRegistryHolder().key());
            }
        }
    }
}
