package muddykat.alchemia.common;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.registration.registers.BlockRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

public class WorldEventHandler {

    @SubscribeEvent
    public static void cauldronCreation(BlockEvent.EntityPlaceEvent event){
        if(event.getEntity() instanceof Player){
            BlockState block = event.getPlacedBlock();
            if(block.getBlock() instanceof CauldronBlock){
                BlockState blockBelow = event.getPlacedAgainst();
                if(blockBelow.is(Blocks.CAMPFIRE)) {
                    BlockPos pos = event.getPos();
                    event.getWorld().setBlock(pos, Blocks.AIR.defaultBlockState(), 1 | 8);
                    event.getWorld().setBlock(pos.below(), BlockRegister.BLOCK_REGISTRY.get("alchemical_cauldron").get().defaultBlockState(), 1);
                }
            }
        }
    }

}
