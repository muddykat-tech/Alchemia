package muddykat.alchemia.client.render;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemIngredient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class CrushProperty implements ClampedItemPropertyFunction {

    public static final CrushProperty INSTANCE = new CrushProperty();
    public static final ResourceLocation ID = new ResourceLocation(Alchemia.MODID, "crush");

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return (float) ItemIngredient.crushLevel(stack);
    }
}
