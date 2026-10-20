package muddykat.alchemia.common.blocks.blockentity;

import muddykat.alchemia.common.utility.Handover;
import muddykat.alchemia.client.ClientHooks;
import muddykat.alchemia.common.blocks.tileentity.TileEntityResearchDesk;
import muddykat.alchemia.common.guide.GuideResearch;
import muddykat.alchemia.common.guide.GuideSource;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class BlockResearchDesk extends EntityBlockGeneric {

    public static final String ID = "research_desk";

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_GUIDE = BooleanProperty.create("has_guide");

    private static final Map<Direction, VoxelShape> SHAPES = GuideBlockShapes.rotations(new double[][]{
            {1, 0, 1, 3, 12, 3},
            {13, 0, 1, 15, 12, 3},
            {1, 0, 13, 3, 12, 15},
            {13, 0, 13, 15, 12, 15},
            {1, 9, 1, 15, 12, 15},
            {0, 12, 0, 16, 14, 16},
            {2, 14, 7, 14, 17, 15}
    });

    public BlockResearchDesk(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_GUIDE, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeRegistry.RESEARCH_DESK.get().create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HAS_GUIDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityResearchDesk desk)) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof ItemAlchemiaGuide) {
            if (!desk.hasGuide()) return insert(desk, stack, level, pos, player);
            return share(desk, stack, level, pos, player);
        }

        if (desk.hasGuide() && ItemAlchemiaGuide.canBind(stack)) {
            if (!level.isClientSide()) {
                ItemAlchemiaGuide.Binding binding = ItemAlchemiaGuide.bind(desk.guide(), stack, player, false);
                if (binding.bound()) {
                    desk.guideChanged();
                    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                notify(player, binding.message(desk.guide()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return useEmpty(level, pos, player, desk);
    }

    private static InteractionResult useEmpty(Level level, BlockPos pos, Player player, TileEntityResearchDesk desk) {
        if (!desk.hasGuide()) {
            if (!level.isClientSide()) notify(player, Component.translatable("alchemia.desk.empty"));
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                ItemStack taken = desk.takeGuide();
                Handover.give(player, taken);
                level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.2F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (level.isClientSide()) ClientHooks.openGuide(GuideSource.block(pos));
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static InteractionResult insert(TileEntityResearchDesk desk, ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            desk.setGuide(stack);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            notify(player, Component.translatable("alchemia.desk.placed"));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static InteractionResult share(TileEntityResearchDesk desk, ItemStack held, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return InteractionResult.sidedSuccess(level.isClientSide());

        GuideResearch.Shared shared = GuideResearch.exchange(desk.guide(), held);
        if (!shared.any()) {
            notify(player, Component.translatable(shared.skipped() > 0 ? "alchemia.desk.no_room" : "alchemia.desk.nothing_new"));
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        desk.guideChanged();
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 0.9F);
        level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 0.6F, 1.2F);
        Component message = Component.translatable("alchemia.desk.shared", shared.discoveries(), shared.recipes());
        if (shared.skipped() > 0) message = Component.translatable("alchemia.desk.shared_partly", message, shared.skipped());
        notify(player, message);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void notify(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.displayClientMessage(message, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileEntityResearchDesk desk) desk.dropGuide();
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!state.getValue(HAS_GUIDE) || random.nextInt(6) != 0) return;

        level.addParticle(ParticleTypes.ENCHANT,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                random.nextFloat() * 2.0 - 1.0, random.nextFloat() * 0.6 + 0.2, random.nextFloat() * 2.0 - 1.0);
    }
}
