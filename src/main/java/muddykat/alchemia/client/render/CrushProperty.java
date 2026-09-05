package muddykat.alchemia.client.render;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.common.items.ItemIngredient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record CrushProperty() implements RangeSelectItemModelProperty {

    public static final CrushProperty INSTANCE = new CrushProperty();
    public static final MapCodec<CrushProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return (float) ItemIngredient.crushLevel(itemStack);
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
