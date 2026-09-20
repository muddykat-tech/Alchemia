package muddykat.alchemia.client.render;

import muddykat.alchemia.common.potion.BrewBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public final class BrewBaseTextures {

    public static final ResourceLocation WATER_SPRITE = new ResourceLocation("block/water_still");

    private BrewBaseTextures() {
    }

    public static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(texture);
    }

    public static TextureAtlasSprite stillSprite(BrewBase base) {
        if (base.texture().isPresent()) {
            return sprite(base.texture().get());
        }

        Fluid fluid = base.fluidValue();
        if (fluid != Fluids.EMPTY) {
            ResourceLocation still = IClientFluidTypeExtensions.of(fluid).getStillTexture();
            if (still != null) return sprite(still);
        }

        return sprite(WATER_SPRITE);
    }
}
