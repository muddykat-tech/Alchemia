package muddykat.alchemia.client.render;

import muddykat.alchemia.common.blocks.helper.AlchemyMachineFlasks;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

import java.util.List;

public class AlchemyMachineUpgradeRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public List<AlchemyMachineFlasks.Flask> flasks = List.of();
    public int[] potionColors = new int[0];
}
