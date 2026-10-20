package muddykat.alchemia.data;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AlchemiaItemTags {

    public static final TagKey<Item> ANCIENT_OAK_LOGS = tag("ancient_oak_logs");
    public static final TagKey<Item> INGREDIENTS = tag("ingredients");
    public static final TagKey<Item> IMBUEMENTS = tag("imbuements");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Alchemia.MODID, name));
    }
}
