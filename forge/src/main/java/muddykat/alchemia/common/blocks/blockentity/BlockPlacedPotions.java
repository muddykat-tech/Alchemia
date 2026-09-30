package muddykat.alchemia.common.blocks.blockentity;

import muddykat.alchemia.common.blocks.helper.PotionCluster;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedPotions;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPlacedPotions extends EntityBlockGeneric {

    public static final String ID = "placed_potions";

    public static final IntegerProperty POTIONS = IntegerProperty.create("potions", 1, PotionCluster.MAX_POTIONS);
    public static final List<EnumProperty<PotionGlass>> SLOTS = List.of(
            EnumProperty.create("slot_0", PotionGlass.class),
            EnumProperty.create("slot_1", PotionGlass.class),
            EnumProperty.create("slot_2", PotionGlass.class),
            EnumProperty.create("slot_3", PotionGlass.class));

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public BlockPlacedPotions(Properties properties) {
        super(properties);
        BlockState initial = this.stateDefinition.any().setValue(POTIONS, 1);
        for (EnumProperty<PotionGlass> slot : SLOTS) initial = initial.setValue(slot, PotionGlass.NORMAL_POTION);
        this.registerDefaultState(initial);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            shapes.put(state, PotionCluster.shape(glasses(state)));
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeRegistry.PLACED_POTIONS.get().create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POTIONS);
        for (EnumProperty<PotionGlass> slot : SLOTS) builder.add(slot);
    }

    public static List<PotionGlass> glasses(BlockState state) {
        List<PotionGlass> glasses = new ArrayList<>();
        for (int slot = 0; slot < state.getValue(POTIONS); slot++) glasses.add(state.getValue(SLOTS.get(slot)));
        return glasses;
    }

    public BlockState stateFor(List<ItemStack> contents) {
        BlockState state = defaultBlockState().setValue(POTIONS, contents.size());
        for (int slot = 0; slot < contents.size(); slot++) {
            state = state.setValue(SLOTS.get(slot), PotionGlasses.of(contents.get(slot)));
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileEntityPlacedPotions potions) {
            potions.dropContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof TileEntityPlacedPotions potions)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            List<ItemStack> remaining = potions.contents();
            if (remaining.isEmpty()) {
                level.removeBlock(pos, false);
                return InteractionResult.SUCCESS;
            }
            ItemStack taken = remaining.remove(remaining.size() - 1);
            potions.setContents(remaining);
            if (!taken.isEmpty()) player.getInventory().placeItemBackInInventory(taken);

            if (remaining.isEmpty()) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, stateFor(remaining), Block.UPDATE_ALL);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static InteractionResult place(Level level, BlockPos clicked, Direction face, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!PotionGlasses.isVessel(held)) return InteractionResult.PASS;

        BlockPos target = level.getBlockState(clicked).getBlock() instanceof BlockPlacedPotions ? clicked : clicked.relative(face);
        if (!player.mayUseItemAt(target, face, held)) return InteractionResult.PASS;

        BlockState existing = level.getBlockState(target);
        BlockPlacedPotions block;
        if (existing.getBlock() instanceof BlockPlacedPotions potions) {
            if (existing.getValue(POTIONS) >= PotionCluster.MAX_POTIONS) return InteractionResult.PASS;
            block = potions;
        } else {
            if (!existing.canBeReplaced()) return InteractionResult.PASS;
            block = (BlockPlacedPotions) BlockRegistry.BLOCK_REGISTRY.get(ID).get();
        }
        if (!block.defaultBlockState().canSurvive(level, target)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            List<ItemStack> contents = level.getBlockEntity(target) instanceof TileEntityPlacedPotions potions
                    && existing.getBlock() == block ? potions.contents() : new ArrayList<>();
            contents.add(held.copyWithCount(1));
            level.setBlock(target, block.stateFor(contents), Block.UPDATE_ALL);
            if (level.getBlockEntity(target) instanceof TileEntityPlacedPotions potions) potions.setContents(contents);
            if (!player.getAbilities().instabuild) held.shrink(1);
            level.playSound(null, target, SoundType.GLASS.getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.3F);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, target);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
