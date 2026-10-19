package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.common.blocks.AncientOak;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class InfectableWood {

    public record Wood(String name, Supplier<Block> logSupplier, Supplier<List<Block>> leavesSupplier,
                       String sideTexture, String endTexture) {

        public Wood(String name, Block log, List<Block> leaves) {
            this(name, () -> log, () -> leaves, "minecraft:block/" + name + "_log", "minecraft:block/" + name + "_log_top");
        }

        public Block log() {
            return logSupplier.get();
        }

        public List<Block> leaves() {
            return leavesSupplier.get();
        }

        public String infectedName(Ingredients fungus) {
            return fungus.name().toLowerCase(Locale.ROOT) + "_infected_" + name + "_log";
        }

        public boolean isTrunk(net.minecraft.world.level.block.state.BlockState state) {
            return state.is(log()) || (state.getBlock() instanceof BlockInfectedLog infected && infected.getWood() == this);
        }

        public BlockInfectedLog infected(Ingredients fungus) {
            var holder = BlockRegistry.BLOCK_REGISTRY.get(infectedName(fungus));
            return holder == null ? null : (BlockInfectedLog) holder.get();
        }

        public boolean hasLeavesNear(BlockGetter level, BlockPos pos, int reach) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dy = -reach; dy <= reach; dy++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        cursor.setWithOffset(pos, dx, dy, dz);
                        if (leaves().contains(level.getBlockState(cursor).getBlock())) return true;
                    }
                }
            }
            return false;
        }
    }

    public static final List<Wood> WOODS;

    static {
        List<Wood> woods = new ArrayList<>();
        woods.add(new Wood("oak", Blocks.OAK_LOG, List.of(Blocks.OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES)));
        woods.add(new Wood("spruce", Blocks.SPRUCE_LOG, List.of(Blocks.SPRUCE_LEAVES)));
        woods.add(new Wood("birch", Blocks.BIRCH_LOG, List.of(Blocks.BIRCH_LEAVES)));
        woods.add(new Wood("jungle", Blocks.JUNGLE_LOG, List.of(Blocks.JUNGLE_LEAVES)));
        woods.add(new Wood("acacia", Blocks.ACACIA_LOG, List.of(Blocks.ACACIA_LEAVES)));
        woods.add(new Wood("cherry", Blocks.CHERRY_LOG, List.of(Blocks.CHERRY_LEAVES)));
        woods.add(new Wood("dark_oak", Blocks.DARK_OAK_LOG, List.of(Blocks.DARK_OAK_LEAVES)));
        woods.add(new Wood("mangrove", Blocks.MANGROVE_LOG, List.of(Blocks.MANGROVE_LEAVES)));
#if MC_VER >= 26102
        woods.add(new Wood("pale_oak", Blocks.PALE_OAK_LOG, List.of(Blocks.PALE_OAK_LEAVES)));
#endif
#if MC_VER >= 26300
        woods.add(new Wood("poplar", Blocks.POPLAR_LOG, List.of(Blocks.RED_POPLAR_LEAVES, Blocks.ORANGE_POPLAR_LEAVES, Blocks.YELLOW_POPLAR_LEAVES)));
#endif
        woods.add(new Wood(AncientOak.WOOD_NAME, () -> AncientOak.block(AncientOak.LOG),
                () -> List.of(AncientOak.block(AncientOak.LEAVES)),
                AncientOak.TEXTURE_ROOT + "log_side", AncientOak.TEXTURE_ROOT + "log_top"));
        WOODS = Collections.unmodifiableList(woods);
    }

    private InfectableWood() {
    }

    public static Wood of(Block log) {
        for (Wood wood : WOODS) {
            if (wood.log() == log) return wood;
        }
        return null;
    }
}
