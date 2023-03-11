package muddykat.alchemia.common.blocks.blockentity;

import com.blamejared.crafttweaker.api.util.AttributeUtil;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.helper.ScreenHelper;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
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
        ItemStack heldStack = player.getItemInHand(hand);
        Item heldItem = heldStack.getItem();

        if(heldItem instanceof BucketItem) {
            boolean isValid = heldItem.equals(Items.WATER_BUCKET) ;
            if(isValid) {
                BlockEntity entity = level.getBlockEntity(pos);
                if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                    if (cauldron.canFill()) {
                        Item item = heldStack.getItem();
                        player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(Items.BUCKET)));
                        player.awardStat(Stats.ITEM_USED.get(item));
                        cauldron.setFullWater();
                        level.setBlockAndUpdate(pos, state);

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
                cauldron.addIngredient(ingredient.getIngredient());
                heldStack.shrink(1);
                player.setItemInHand(hand, heldStack);
                return InteractionResult.CONSUME;
            }
        }

        if(heldItem.equals(Items.GLASS_BOTTLE)){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof TileEntityAlchemyCauldron cauldron) {
                if(cauldron.filledPotion()) { // this check updates the water level in the cauldron.
                    ItemStack potion = new ItemStack(Items.POTION);
                    int[] alignment = new int[2];
                    alignment[0] = cauldron.getXAlignment();
                    alignment[1] = cauldron.getYAlignment();
                    PotionMap.PotionEffectPosition potionEffectPos = PotionMap.INSTANCE.getEffectPotion(alignment);
                    MobEffect effect = potionEffectPos.getEffect();
                    int effectDuration = potionEffectPos.getDuration();
                    int effectStrength = potionEffectPos.getStrength();

                    cauldron.addEffect(new MobEffectInstance(effect, effectDuration));
                    cauldron.ensureStrength(effect, effectStrength);

                    PotionUtils.setCustomEffects(potion, cauldron.getEffectList());

                    potion.setHoverName(new TextComponent("Crafted Potion"));

                    heldStack.shrink(1);
                    player.addItem(potion);

                    if(cauldron.getWaterLevel() <= 0){
                        cauldron.resetEffectList();
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
