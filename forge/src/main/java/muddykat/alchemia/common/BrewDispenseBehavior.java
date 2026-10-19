package muddykat.alchemia.common;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public final class BrewDispenseBehavior implements DispenseItemBehavior {

    private static final Method DISPENSE_METHOD =
            ObfuscationReflectionHelper.findMethod(DispenserBlock.class, "m_7216_", ItemStack.class);

    private final DispenseItemBehavior fallback;
    private final BiPredicate<TileEntityAlchemyCauldron, ItemStack> applies;

    private final DefaultDispenseItemBehavior acting;

    private BrewDispenseBehavior(DispenseItemBehavior fallback, BiPredicate<TileEntityAlchemyCauldron, ItemStack> applies,
                                 BiFunction<TileEntityAlchemyCauldron, ItemStack, ItemStack> action) {
        this.fallback = fallback;
        this.applies = applies;
        this.acting = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior overflow = new DefaultDispenseItemBehavior();

            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                TileEntityAlchemyCauldron cauldron = cauldronAhead(source, stack);
                if (cauldron == null) return stack;

                ItemStack remainder = action.apply(cauldron, stack);
                stack.shrink(1);
                if (stack.isEmpty()) return remainder;
                if (remainder.isEmpty()) return stack;

                DispenserBlockEntity dispenser = source.getEntity();
                if (dispenser.addItem(remainder) < 0) overflow.dispense(source, remainder);
                return stack;
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
        DispenseItemBehavior previous;
        try {
            previous = (DispenseItemBehavior) DISPENSE_METHOD.invoke(Blocks.DISPENSER, new ItemStack(item));
        } catch (ReflectiveOperationException error) {
            previous = new DefaultDispenseItemBehavior();
        }
        DispenserBlock.registerBehavior(item, new BrewDispenseBehavior(previous, applies, action));
    }

    private TileEntityAlchemyCauldron cauldronAhead(BlockSource source, ItemStack stack) {
        BlockPos target = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        return source.getLevel().getBlockEntity(target) instanceof TileEntityAlchemyCauldron cauldron
                && applies.test(cauldron, stack) ? cauldron : null;
    }

    @Override
    public ItemStack dispense(BlockSource source, ItemStack stack) {
        return cauldronAhead(source, stack) != null ? acting.dispense(source, stack) : fallback.dispense(source, stack);
    }
}
