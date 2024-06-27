package muddykat.alchemia.common.blocks.blockentity;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import static net.minecraft.world.InteractionResult.PASS;
import static net.minecraft.world.InteractionResult.SUCCESS;

public class BlockAlchemyCauldron extends EntityBlockGeneric {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public BlockAlchemyCauldron() {
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get().create(pPos, pState);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        level.setBlockAndUpdate(pos, state);
        ItemStack heldStack = player.getItemInHand(hand);
        Item heldItem = heldStack.getItem();

        if(heldStack.isEmpty() && player.isShiftKeyDown())
        {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                if (!cauldron.canFill()) {
                    if (!level.isClientSide) {
                        level.playSound((Player) null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    cauldron.empty();
                    return SUCCESS;
                }
            }
        }


        if(heldItem.equals(Items.GUNPOWDER)) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                if(cauldron.getPotion().getItem().equals(Items.POTION)) {
                    if (!level.isClientSide) {
                        level.playSound((Player) null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
                    }
                    heldStack.shrink(1);
                    player.setItemInHand(hand, heldStack);

                    cauldron.setSplashResult();
                }
            }

        }

        if(heldItem instanceof BucketItem) {
            boolean isValid = heldItem.equals(Items.WATER_BUCKET) ;
            if(isValid) {
                BlockEntity entity = level.getBlockEntity(pos);
                if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                    if (cauldron.canFill()) {
                        Item item = heldStack.getItem();
                        if(!player.isCreative()) player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(Items.BUCKET)));
                        player.awardStat(Stats.ITEM_USED.get(item));
                        cauldron.setFullWater();

                        if (!level.isClientSide) {
                            level.playSound((Player) null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            level.gameEvent((Entity) null, GameEvent.FLUID_PLACE, pos);
                        }

                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
        }

        if(heldItem instanceof ItemIngredient ingredient) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                if(cauldron.getWaterLevel() > 0) {
                    cauldron.addIngredient(ingredient);
                    heldStack.shrink(1);
                    player.setItemInHand(hand, heldStack);

                    if (!level.isClientSide) {
                        level.playSound((Player) null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent((Entity) null, GameEvent.SPLASH, pos);
                    }

                    return InteractionResult.CONSUME;
                }
                return InteractionResult.FAIL;
            }
        }

        if(heldItem.equals(Items.GLASS_BOTTLE)){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                if(cauldron.getWaterLevel() > 0) { // this check updates the water level in the cauldron.
                    cauldron.takeWaterPortion();
                    ItemStack potion = cauldron.getPotion();
                    heldStack.shrink(1);
                    player.addItem(potion);

                    if (!level.isClientSide) {
                        level.playSound((Player) null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent((Entity) null, GameEvent.SPLASH, pos);
                    }

                    if(cauldron.getWaterLevel() < 1){
                        cauldron.resetEffectList();
                        if (!level.isClientSide) {
                            level.playSound((Player) null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            level.gameEvent((Entity) null, GameEvent.SPLASH, pos);
                        }
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }

        ItemStack itemstack = player.getItemInHand(hand);
        if(!level.isClientSide()){
            if(itemstack.hasTag() && itemstack.getItem() instanceof ItemAlchemiaGuide) {
                BlockEntity entity = level.getBlockEntity(pos);
                if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                    NetworkHooks.openGui((ServerPlayer) player, cauldron, pos);
                }
            } else {
                player.displayClientMessage(new TranslatableComponent("alchemia.guide.unbound.message"), true);
            }
            return InteractionResult.SUCCESS;
        }

        return SUCCESS;
    }
    protected static VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }
}
