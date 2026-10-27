package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class CauldronFilling {

    private CauldronFilling() {
    }

    public static @Nullable InteractionResult tryFill(TileEntityAlchemyCauldron cauldron, Player player, InteractionHand hand,
                                                      ItemStack heldStack, BlockPos pos, Level level) {
        Item heldItem = heldStack.getItem();
        BrewBases.Match match = BrewBases.forItem(heldStack);
        if (match == null) return tryFluidFill(cauldron, player, hand, pos, level);

        if (!cauldron.canAcceptFill(match.base(), match.fill().levels())) return refuseFill(cauldron, player, match.base());

        Item remainder = match.fill().remainderValue();
        if (!player.isCreative()) {
            if (remainder != null) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(remainder)));
            } else {
                heldStack.shrink(1);
                player.setItemInHand(hand, heldStack);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(heldItem));
        cauldron.addFill(match.base(), match.fill().levels());
        announce(level, pos, match.fill().levels());
        return InteractionResult.SUCCESS;
    }

    public static void announce(Level level, BlockPos pos, int levels) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, levels >= BrewBase.MAX_FILL_LEVEL ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_EMPTY,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
    }

    private static @Nullable InteractionResult tryFluidFill(TileEntityAlchemyCauldron cauldron, Player player, InteractionHand hand,
                                                            BlockPos pos, Level level) {
        ResourceHandler<FluidResource> held = ItemAccess.forPlayerInteraction(player, hand).oneByOne()
                .getCapability(Capabilities.Fluid.ITEM);
        if (held == null) return null;

        for (int index = 0; index < held.size(); index++) {
            FluidResource resource = held.getResource(index);
            if (resource.isEmpty()) continue;

            BrewBases.Match match = BrewBases.forFluid(resource);
            if (match == null) continue;
            if (!cauldron.canAcceptFill(match.base(), match.fill().levels())) return refuseFill(cauldron, player, match.base());

            int amount = match.fill().fluidAmount();
            try (Transaction transaction = Transaction.openRoot()) {
                if (held.extract(index, resource, amount, transaction) < amount) continue;
                transaction.commit();
            }

            cauldron.addFill(match.base(), match.fill().levels());
            announce(level, pos, match.fill().levels());
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    private static InteractionResult refuseFill(TileEntityAlchemyCauldron cauldron, Player player, BrewBase filled) {
        if (cauldron.getWaterLevel() > 0 && !filled.equals(cauldron.getBase())) {
            TileEntityAlchemyCauldron.message(player, "alchemia.brew.wrong_base");
        }
        return InteractionResult.CONSUME;
    }
}
