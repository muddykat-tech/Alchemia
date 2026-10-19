package muddykat.alchemia.common;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public final class BrewDispenseBehavior implements DispenseItemBehavior {

    private final DispenseItemBehavior fallback;
    private final BiPredicate<TileEntityAlchemyCauldron, ItemStack> applies;

    private final DefaultDispenseItemBehavior acting;

    private BrewDispenseBehavior(DispenseItemBehavior fallback, BiPredicate<TileEntityAlchemyCauldron, ItemStack> applies,
                                 BiFunction<TileEntityAlchemyCauldron, ItemStack, ItemStack> action) {
        this.fallback = fallback;
        this.applies = applies;
        this.acting = new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                TileEntityAlchemyCauldron cauldron = cauldronAhead(source, stack);
                if (cauldron == null) return stack;
                return consumeWithRemainder(source, stack, action.apply(cauldron, stack));
            }
        };
    }

    public static void registerBottling(ItemLike vessel) {
        register(vessel, (cauldron, stack) -> cauldron.holdsFinishedBrew(), TileEntityAlchemyCauldron::bottle);
    }

    public static void registerFilling(ItemLike container) {
        register(container, TileEntityAlchemyCauldron::canFillFrom, TileEntityAlchemyCauldron::fillFrom);
    }

    private static void register(ItemLike item, BiPredicate<TileEntityAlchemyCauldron, ItemStack> applies,
                                 BiFunction<TileEntityAlchemyCauldron, ItemStack, ItemStack> action) {
        DispenseItemBehavior previous = DispenserBlock.DISPENSER_REGISTRY.get(item.asItem());
        DispenserBlock.registerBehavior(item, new BrewDispenseBehavior(
                previous == null ? new DefaultDispenseItemBehavior() : previous, applies, action));
    }

    private TileEntityAlchemyCauldron cauldronAhead(BlockSource source, ItemStack stack) {
        BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        return source.level().getBlockEntity(target) instanceof TileEntityAlchemyCauldron cauldron
                && applies.test(cauldron, stack) ? cauldron : null;
    }

    @Override
    public ItemStack dispense(BlockSource source, ItemStack stack) {
        return cauldronAhead(source, stack) != null ? acting.dispense(source, stack) : fallback.dispense(source, stack);
    }
}
