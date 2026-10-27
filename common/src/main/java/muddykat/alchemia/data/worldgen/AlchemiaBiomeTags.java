package muddykat.alchemia.data.worldgen;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.world.IngredientRange;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class AlchemiaBiomeTags {
    public static final TagKey<Biome> HAS_FIRE_PLANTS = tag("has_fire_plants");
    public static final TagKey<Biome> HAS_WATER_PLANTS = tag("has_water_plants");
    public static final TagKey<Biome> HAS_AIR_PLANTS = tag("has_air_plants");
    public static final TagKey<Biome> HAS_EARTH_PLANTS = tag("has_earth_plants");
    public static final TagKey<Biome> HAS_SPELLBLOOM = tag("has_spellbloom");
    public static final TagKey<Biome> HAS_ANCIENT_OAK = tag("has_ancient_oak");
    public static final TagKey<Biome> HAS_FROST_SAPPHIRE = tag("has_frost_sapphire");
    public static final TagKey<Biome> HAS_SULPHUR_SHELF = tag("has_sulphur_shelf");
    public static final TagKey<Biome> HAS_TERRORBUD = tag("has_terrorbud");
    public static final TagKey<Biome> HAS_MAGMA_MOREL = tag("has_magma_morel");
    public static final TagKey<Biome> HAS_LAVA_ROOT = tag("has_lava_root");
    public static final TagKey<Biome> HAS_TERRARIA = tag("has_terraria");
    public static final TagKey<Biome> HAS_GRASPING_ROOT = tag("has_grasping_root");
    public static final TagKey<Biome> HAS_DREAMBEET = tag("has_dreambeet");
    public static final TagKey<Biome> HAS_END_CRYSTAL = tag(IngredientRange.END_ISLANDS);

    public static TagKey<Biome> forAlignment(IngredientAlignment alignment) {
        return switch (alignment) {
            case Fire -> HAS_FIRE_PLANTS;
            case Water -> HAS_WATER_PLANTS;
            case Air -> HAS_AIR_PLANTS;
            case Earth -> HAS_EARTH_PLANTS;
            case Void -> null;
        };
    }

    private static TagKey<Biome> tag(String name) {
        return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(Alchemia.MODID, name));
    }
}
