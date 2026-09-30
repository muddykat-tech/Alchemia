package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
#if MC_VER < 26102
import net.minecraft.world.InteractionHand;
#endif

public class BlockIngredientBerry extends BlockIngredientTall {

    public static final BooleanProperty FRUITING = BooleanProperty.create("fruiting");
    public static final int FRUITING_CHANCE = 6;
    public static final int MIN_PICKED = 1;
    public static final int MAX_PICKED = 3;

    public BlockIngredientBerry(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FRUITING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRUITING);
    }

    public boolean isFruiting(BlockState state) {
        return state.getValue(FRUITING);
    }

    @Override
    protected BlockState shareGrowth(BlockState from, BlockState to) {
        return super.shareGrowth(from, to).setValue(FRUITING, from.getValue(FRUITING));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return isLower(state) && !(isMaxAge(state) && isFruiting(state));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isLower(state)) return;
        if (!isMaxAge(state)) {
            super.randomTick(state, level, pos, random);
            return;
        }

        if (!isFruiting(state) && random.nextInt(FRUITING_CHANCE) == 0 && level.getRawBrightness(pos.above(), 0) >= 9) {
            level.setBlock(pos, state.setValue(FRUITING, true), 2);
        }
    }

    private InteractionResult pick(BlockState state, Level level, BlockPos pos, Player player) {
        if (!isFruiting(state)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            int count = MIN_PICKED + level.getRandom().nextInt(MAX_PICKED - MIN_PICKED + 1);
            popResource(level, pos, new ItemStack(ItemRegistry.getItemFromRegistry(ingredientData.getRegistryName()), count));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                    1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);

            BlockState picked = state.setValue(FRUITING, false);
            level.setBlock(pos, picked, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        }

#if MC_VER >= 26102
        return InteractionResult.SUCCESS;
#else
        return InteractionResult.sidedSuccess(level.isClientSide());
#endif
    }

#if MC_VER >= 26102
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = pick(state, level, pos, player);
        return result == InteractionResult.PASS ? super.useWithoutItem(state, level, pos, player, hitResult) : result;
    }
#else
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = pick(state, level, pos, player);
        return result == InteractionResult.PASS ? super.use(state, level, pos, player, hand, hitResult) : result;
    }
#endif

    private boolean canRipen(BlockState state) {
        return isMaxAge(state) && !isFruiting(state);
    }

    private void ripen(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(FRUITING, true), 2);
    }

#if MC_VER >= 26300
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return canRipen(state) || super.isValidBonemealTarget(level, pos, state, source);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        if (isMaxAge(state)) ripen(level, pos, state);
        else super.performBonemeal(level, random, pos, state, source);
    }
#elif MC_VER >= 26102
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return canRipen(state) || super.isValidBonemealTarget(level, pos, state);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isMaxAge(state)) ripen(level, pos, state);
        else super.performBonemeal(level, random, pos, state);
    }
#else
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return canRipen(state) || super.isValidBonemealTarget(level, pos, state, isClient);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isMaxAge(state)) ripen(level, pos, state);
        else super.performBonemeal(level, random, pos, state);
    }
#endif

    @Override
    protected BlockState wildState(BlockState plant, int age, RandomSource random) {
        BlockState state = super.wildState(plant, age, random);
        return state.setValue(FRUITING, age == getMaxAge() && random.nextBoolean());
    }
}
