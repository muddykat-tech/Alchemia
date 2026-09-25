package muddykat.alchemia.common.items.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IngredientTool {

    int ticksPerStep();

    boolean canApply(ItemStack target);

    void applyStep(ItemStack target, ItemStack tool, ServerLevel level, @Nullable LivingEntity user);

    void playStep(Level level, BlockPos pos);

    String actionKey();

    float progress(ItemStack target);

    static @Nullable IngredientTool of(ItemStack stack) {
        return stack.getItem() instanceof IngredientTool tool ? tool : null;
    }
}
