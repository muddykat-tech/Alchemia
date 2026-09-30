package muddykat.alchemia.common.items;

import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.helper.InfectableWood;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemIngredientSpores extends ItemIngredientSeed {

    public ItemIngredientSpores(Block block, Ingredients ingredient, IngredientType type, IngredientAlignment alignment, Properties properties) {
        super(block, ingredient, type, alignment, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState log = level.getBlockState(pos);
        InfectableWood.Wood wood = InfectableWood.of(log.getBlock());
        BlockInfectedLog infected = wood == null ? null : wood.infected(getIngredient());

        if (infected == null || !log.hasProperty(RotatedPillarBlock.AXIS)) return super.useOn(context);

        if (!level.isClientSide()) {
            BlockState state = infected.infect(log);
            level.setBlock(pos, state, 3);
            level.playSound(null, pos, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), state));

            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        }

#if MC_VER >= 26102
        return InteractionResult.SUCCESS;
#else
        return InteractionResult.sidedSuccess(level.isClientSide());
#endif
    }
}
