package muddykat.alchemia.common.blocks.blockentity;

import muddykat.alchemia.common.utility.Handover;
import muddykat.alchemia.client.ClientHooks;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedGuide;
import muddykat.alchemia.common.guide.GuideResearch;
import muddykat.alchemia.common.guide.GuideSource;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockPlacedGuide extends EntityBlockGeneric {

    public static final String ID = "placed_guide";

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty THICKNESS = IntegerProperty.create("thickness", 0, GuideResearch.MAX_THICKNESS);
    public static final IntegerProperty TABS = IntegerProperty.create("tabs", 0, GuideResearch.MAX_TABS);

    private static final int[] HEIGHTS = {5, 6, 8, 9, 10};
    private static final List<Map<Direction, VoxelShape>> SHAPES = buildShapes();

    public BlockPlacedGuide(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(THICKNESS, 0).setValue(TABS, 0));
    }

    public static int height(int thickness) {
        return HEIGHTS[Mth.clamp(thickness, 0, HEIGHTS.length - 1)];
    }

    private static List<Map<Direction, VoxelShape>> buildShapes() {
        List<Map<Direction, VoxelShape>> shapes = new ArrayList<>();
        for (int thickness = 0; thickness <= GuideResearch.MAX_THICKNESS; thickness++) {
            shapes.add(GuideBlockShapes.rotations(new double[][]{{2.5, 0, 2, 13.5, height(thickness), 14}}));
        }
        return shapes;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeRegistry.PLACED_GUIDE.get().create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, THICKNESS, TABS);
    }

    public static BlockState sizedFor(BlockState state, ItemStack guide) {
        return state.setValue(THICKNESS, GuideResearch.thickness(guide)).setValue(TABS, GuideResearch.tabs(guide));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(THICKNESS)).get(state.getValue(FACING));
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
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileEntityPlacedGuide tome) tome.dropGuide();
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return muddykat.alchemia.common.utility.Interactions.item(interact(state, level, pos, player, hand, hitResult));
    }

    private InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityPlacedGuide tome)) return InteractionResult.PASS;

        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                ItemStack taken = tome.takeGuide();
                level.removeBlock(pos, false);
                Handover.give(player, taken);
                level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.2F);
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (level.isClientSide()) ClientHooks.openGuide(GuideSource.block(pos));
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static InteractionResult place(Level level, BlockPos clicked, Direction face, Player player, ItemStack guide) {
        BlockPos target = level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(face);
        if (!player.mayUseItemAt(target, face, guide)) return InteractionResult.PASS;
        if (!level.getBlockState(target).canBeReplaced()) return InteractionResult.PASS;

        BlockPlacedGuide block = (BlockPlacedGuide) BlockRegistry.BLOCK_REGISTRY.get(ID).get();
        BlockState state = sizedFor(block.defaultBlockState().setValue(FACING, player.getDirection().getOpposite()), guide);
        if (!state.canSurvive(level, target)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            level.setBlock(target, state, Block.UPDATE_ALL);
            if (level.getBlockEntity(target) instanceof TileEntityPlacedGuide tome) tome.setGuide(guide);
            guide.shrink(1);
            level.playSound(null, target, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 0.9F);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, target);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
