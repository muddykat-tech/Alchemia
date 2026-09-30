package muddykat.alchemia.client.render;

import muddykat.alchemia.common.blocks.helper.PotionCluster;
import muddykat.alchemia.common.potion.PotionGlass;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class PlacedPotionsRenderState extends BlockEntityRenderState {
    public int count;
    public final PotionGlass[] glasses = new PotionGlass[PotionCluster.MAX_POTIONS];
    public final boolean[] filled = new boolean[PotionCluster.MAX_POTIONS];
    public final int[] colors = new int[PotionCluster.MAX_POTIONS];
}
