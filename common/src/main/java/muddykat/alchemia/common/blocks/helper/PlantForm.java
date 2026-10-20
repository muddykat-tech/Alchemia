package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockIngredientBerry;
import muddykat.alchemia.common.blocks.BlockIngredientKelp;
import muddykat.alchemia.common.blocks.BlockIngredientPatch;
import muddykat.alchemia.common.blocks.BlockIngredientShelf;
import muddykat.alchemia.common.blocks.BlockIngredientTall;
import muddykat.alchemia.common.blocks.BlockIngredientTangle;
import muddykat.alchemia.common.blocks.BlockIngredientWild;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockKelpStalk;
import muddykat.alchemia.common.blocks.BlockTangleBranch;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.common.items.ItemIngredientSpores;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.EnumMap;
import java.util.Map;

public enum PlantForm {
    SINGLE,
    TALL,
    BERRY_BUSH,
    TANGLE,
    SHELF,
    KELP,
    PATCH,
    BEET;

    public static final String BRANCH = "_branch";
    public static final String STALK = "_stalk";

    private static final Map<Ingredients, PlantForm> FORMS = new EnumMap<>(Ingredients.class);

    static {
        FORMS.put(Ingredients.Evergreen_Fern, TALL);
        FORMS.put(Ingredients.Goldthorn, TALL);
        FORMS.put(Ingredients.Thornstick, TALL);
        FORMS.put(Ingredients.Mageberry, BERRY_BUSH);
        FORMS.put(Ingredients.Tangleweed, TANGLE);
        FORMS.put(Ingredients.Sulphur_Shelf, SHELF);
        FORMS.put(Ingredients.Flameweed, KELP);
        FORMS.put(Ingredients.Waterbloom, PATCH);
        FORMS.put(Ingredients.Dreambeet, BEET);
    }

    public static PlantForm of(Ingredients ingredient) {
        return FORMS.getOrDefault(ingredient, SINGLE);
    }

    public BlockIngredient createBlock(Ingredients ingredient, IngredientType type, BlockBehaviour.Properties properties) {
        return switch (this) {
            case SINGLE -> BlockIngredientWild.hasFullGrowth(ingredient)
                    ? new BlockIngredientWild(ingredient, type, properties)
                    : new BlockIngredient(ingredient, type, properties);
            case TALL -> new BlockIngredientTall(ingredient, type, properties);
            case BERRY_BUSH -> new BlockIngredientBerry(ingredient, type, properties);
            case TANGLE -> new BlockIngredientTangle(ingredient, type, properties);
            case SHELF -> new BlockIngredientShelf(ingredient, type, properties);
            case KELP -> new BlockIngredientKelp(ingredient, type, properties);
            case PATCH -> new BlockIngredientPatch(ingredient, type, properties);
            case BEET -> new BlockIngredient(ingredient, type, properties);
        };
    }

    public BlockBehaviour.Properties adjust(BlockBehaviour.Properties properties) {
        if (this == SINGLE) return properties;
        properties.offsetType(BlockBehaviour.OffsetType.NONE);
        return this == SHELF ? properties.sound(SoundType.FUNGUS) : properties;
    }

    public Item createSeed(Block block, Ingredients ingredient, IngredientType type, IngredientAlignment alignment, Item.Properties properties) {
        return this == SHELF
                ? new ItemIngredientSpores(block, ingredient, type, alignment, properties)
                : new ItemIngredientSeed(block, ingredient, type, alignment, properties);
    }

    public void registerParts(Ingredients ingredient) {
        switch (this) {
            case TANGLE -> BlockRegistry.registerBlock(ingredient.getRegistryName() + BRANCH,
                    properties -> new BlockTangleBranch(ingredient, properties), PlantForm::aquaticPart);
            case KELP -> BlockRegistry.registerBlock(ingredient.getRegistryName() + STALK,
                    properties -> new BlockKelpStalk(ingredient, properties), PlantForm::aquaticPart);
            case SHELF -> {
                for (InfectableWood.Wood wood : InfectableWood.WOODS) {
                    BlockRegistry.registerBlock(wood.infectedName(ingredient),
                            properties -> new BlockInfectedLog(ingredient, wood, properties),
                            () -> copyOf(wood.log()).randomTicks());
                }
            }
            default -> {
            }
        }
    }

    public static Block part(Ingredients ingredient, String suffix) {
        return BlockRegistry.BLOCK_REGISTRY.get(ingredient.getRegistryName() + suffix).get();
    }

    private static BlockBehaviour.Properties copyOf(Block block) {
#if MC_VER >= 12100
        return BlockBehaviour.Properties.ofFullCopy(block);
#else
        return BlockBehaviour.Properties.copy(block);
#endif
    }

    private static BlockBehaviour.Properties aquaticPart() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WATER)
#if MC_VER >= 26102
                .noCollision()
#else
                .noCollission()
#endif
                .instabreak()
                .sound(SoundType.WET_GRASS)
#if MC_VER >= 26300
                .pushReaction(PushReaction.POPPED);
#else
                .pushReaction(PushReaction.DESTROY);
#endif
    }
}
