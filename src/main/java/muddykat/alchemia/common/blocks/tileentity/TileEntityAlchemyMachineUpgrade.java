package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public class TileEntityAlchemyMachineUpgrade extends SyncedBlockEntity {

    private final UpgradeSide side;
    private final ItemStacksResourceHandler potions;

    public TileEntityAlchemyMachineUpgrade(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get(), pos, state);
        this.side = state.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade ? upgrade.getSide() : UpgradeSide.RIGHT;
        this.potions = new ItemStacksResourceHandler(this.side.potionSlots()) {
            @Override
            protected int getCapacity(int index, ItemResource resource) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int index, ItemStack previousContents) {
                setChanged();
                sync();
            }
        };
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Potions").ifPresent(potions::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        potions.serialize(output.child("Potions"));
    }

    public UpgradeSide getSide() {
        return side;
    }

    public ItemStacksResourceHandler getPotions() {
        return potions;
    }

    public int slotCount() {
        return potions.size();
    }

    public Direction facing() {
        return getBlockState().getValue(BlockAlchemyMachineUpgrade.FACING);
    }

    public ItemStack getStack(int slot) {
        return potions.getResource(slot).toStack(potions.getAmountAsInt(slot));
    }

    public void clearSlot(int slot) {
        potions.set(slot, ItemResource.EMPTY, 0);
    }

    public @Nullable TileEntityAlchemyMachineCore getCore() {
        if (level == null) return null;
        Direction facing = facing();
        BlockPos corePos = worldPosition.relative(side.directionFrom(facing).getOpposite());
        return level.getBlockEntity(corePos) instanceof TileEntityAlchemyMachineCore core
                && core.getBlockState().getValue(BlockAlchemyMachineCore.FACING) == facing ? core : null;
    }

    public static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    public int slotForHit(BlockHitResult hit) {
        for (int slot = 0; slot < potions.size(); slot++) {
            if (potions.getAmountAsInt(slot) == 0) return slot;
        }
        return -1;
    }

    public InteractionResult insertPotion(Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!isPotion(held)) return InteractionResult.PASS;

        int slot = slotForHit(hit);
        if (slot < 0) return InteractionResult.PASS;

        if (level != null && !level.isClientSide()) {
            potions.set(slot, ItemResource.of(held), 1);
            if (!player.getAbilities().instabuild) held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) Containers.dropContents(level, pos, potions.copyToList());
    }

    @Override
    public InteractionResult onActivated(BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
}
