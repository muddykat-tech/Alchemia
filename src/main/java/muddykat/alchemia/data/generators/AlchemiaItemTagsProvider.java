package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.data.AlchemiaItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class AlchemiaItemTagsProvider extends TagsProvider<Item> {

    public AlchemiaItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.ITEM, registries, Alchemia.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
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
