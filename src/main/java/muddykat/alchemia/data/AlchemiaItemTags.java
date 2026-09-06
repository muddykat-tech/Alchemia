package muddykat.alchemia.data;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AlchemiaItemTags {

    public static final TagKey<Item> INGREDIENTS = tag("ingredients");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Alchemia.MODID, name));
    }
}
