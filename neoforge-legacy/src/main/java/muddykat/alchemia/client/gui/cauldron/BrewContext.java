package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

public interface BrewContext {

    TileEntityAlchemyCauldron cauldron();

    PotionMap map();

    BrewCourse course();

    PathPoint brewAt();

    CauldronKnowledge knowledge();

    MapView view();

    ItemStack pendingIngredient();

    Font getFont();
}
