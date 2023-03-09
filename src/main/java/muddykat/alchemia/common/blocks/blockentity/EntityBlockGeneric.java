package muddykat.alchemia.common.blocks.blockentity;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.material.Material;

public abstract class EntityBlockGeneric extends BaseEntityBlock {
    protected EntityBlockGeneric() {
        super(Properties.of(Material.METAL));
    }
}
