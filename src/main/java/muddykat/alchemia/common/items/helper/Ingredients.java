package muddykat.alchemia.common.items.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum Ingredients {
    Firebell(IngredientType.Flower, IngredientAlignment.Fire),
    Waterbloom(IngredientType.Flower, IngredientAlignment.Water),
    Windbloom(IngredientType.Flower, IngredientAlignment.Air),
    Terraria(IngredientType.Root, IngredientAlignment.Earth),
    Bloodthorn(IngredientType.Herb, IngredientAlignment.Fire, IngredientAlignment.Air),
    Lifeleaf(IngredientType.Herb, IngredientAlignment.Earth, IngredientAlignment.Water),
    Goldthorn(IngredientType.Herb, IngredientAlignment.Earth),
    Icefruit(IngredientType.Herb, IngredientAlignment.Water),
    Lava_Root(IngredientType.Root, IngredientAlignment.Fire),
    Tangleweed(IngredientType.Herb, IngredientAlignment.Water),
    Goodberry(IngredientType.Herb, IngredientAlignment.Earth, IngredientAlignment.Water),
    Thunder_Thistle(IngredientType.Herb, IngredientAlignment.Water, IngredientAlignment.Air),
    Flameweed(IngredientType.Herb, IngredientAlignment.Fire),
    Mageberry(IngredientType.Herb, IngredientAlignment.Water, IngredientAlignment.Air),
    Terrorbud(IngredientType.Herb, IngredientAlignment.Air, IngredientAlignment.Fire),

    Dreambeet(IngredientType.Root, IngredientAlignment.Air, IngredientAlignment.Water),

    Marshroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire),
    Sulphur_Shelf(IngredientType.Mushroom, IngredientAlignment.Fire),
    Witch_Mushroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire),
    Magma_Morel(IngredientType.Mushroom, IngredientAlignment.Fire),
    Rainbow_Cap(IngredientType.Mushroom, IngredientAlignment.Air, IngredientAlignment.Earth),

    Cloud_Crystal(IngredientType.Mineral, IngredientAlignment.Air),
    Earth_Pyrite(IngredientType.Mineral, IngredientAlignment.Earth),
    Frost_Sapphire(IngredientType.Mineral, IngredientAlignment.Water),
    Fire_Citrine(IngredientType.Mineral, IngredientAlignment.Fire),
    Arcane_Crystal(IngredientType.Mineral, IngredientAlignment.Earth, IngredientAlignment.Air),
    Fable_Bismuth(IngredientType.Mineral, IngredientAlignment.Water, IngredientAlignment.Air);

    final IngredientType type;
    final IngredientAlignment primaryAlignment;

    IngredientAlignment secondaryAlignment;

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment){
        this.type = type;
        this.primaryAlignment = primaryAlignment;
    }

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment){
        this(type, primaryAlignment);
        this.secondaryAlignment = secondaryAlignment;
    }

    public void register(){
        String rName = type.name().toLowerCase() +"_"+ name().toLowerCase();
        BlockRegister.registerBlock(rName, () -> new BlockIngredient(this, type));
        ItemRegister.registerItem(rName, () -> new ItemIngredient(BlockRegister.BLOCK_REGISTRY.get(rName).get(), this, type, primaryAlignment));
    }
}
