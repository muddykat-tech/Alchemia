package muddykat.alchemia.common;

import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class WorldEventHandler {

    @SubscribeEvent
    public static void cauldronCreation(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        BlockState block = event.getPlacedBlock();
        if (!(block.getBlock() instanceof CauldronBlock)) return;

        BlockState blockBelow = event.getPlacedAgainst();
        if (blockBelow.is(Blocks.CAMPFIRE)) {
            BlockPos pos = event.getPos();
            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 1 | 8);
            event.getLevel().setBlock(pos.below(), BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get().defaultBlockState(), 1);
        }
    }

    @SubscribeEvent
    public static void onRecipeUpdate(PlayerEvent.ItemCraftedEvent event) {
        boolean shouldReturnShears = false;
        ItemStack shears = new ItemStack(Items.SHEARS);
        Container items = event.getInventory();
        for (int i = 0; i < items.getContainerSize(); i++) {
            ItemStack foundItem = items.getItem(i);
            if (foundItem.getItem().equals(Items.SHEARS)) {
                shears = foundItem;
                shouldReturnShears = true;
            }
        }

        if (shouldReturnShears) {
            shears.setDamageValue(shears.getDamageValue() + 1);
            event.getEntity().getInventory().add(shears);
        }
    }
}
