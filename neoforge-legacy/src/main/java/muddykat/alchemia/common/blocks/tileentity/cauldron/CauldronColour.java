package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewBase;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CauldronColour {

    private int rgb = 0xffffff;

    public int rgb() {
        return rgb;
    }

    public void update(TileEntityAlchemyCauldron cauldron) {
        if (cauldron.isSpoiled()) {
            rgb = TileEntityAlchemyCauldron.SPOILED_COLOR;
            return;
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        List<MobEffectInstance> effects = cauldron.getEffectList().stream().toList();

        if (!effects.isEmpty()) {
            int count = Math.min(effects.size(), 3);

            for (int i = 0; i < count; i++) {
                int color = effects.get(i).getEffect().value().getColor();
                red += (color >> 16) & 255;
                green += (color >> 8) & 255;
                blue += color & 255;
            }

            red = Math.max(0, Math.min(255, red / count));
            green = Math.max(0, Math.min(255, green / count));
            blue = Math.max(0, Math.min(255, blue / count));
            rgb = (red << 16) | (green << 8) | blue;
        } else {
            refreshBase(cauldron);
        }
    }

    public void refreshBase(TileEntityAlchemyCauldron cauldron) {
        Level level = cauldron.getLevel();
        if (level == null || !level.isClientSide()) return;
        BrewBase base = cauldron.getBase();
        rgb = base.biomeTint()
                ? BiomeColors.getAverageWaterColor((BlockAndTintGetter) level, cauldron.getBlockPos())
                : base.tintOrDefault();
    }
}
