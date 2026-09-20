package muddykat.alchemia.client.render;

import muddykat.alchemia.common.potion.BrewBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class BrewBaseTextures {

    public static final SpriteId WATER_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS,
            Identifier.withDefaultNamespace("block/water_still"));

    private BrewBaseTextures() {
    }

    public static TextureAtlasSprite stillSprite(BrewBase base, SpriteGetter sprites) {
        if (base.texture().isPresent()) {
            return sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, base.texture().get()));
        }

        Fluid fluid = base.fluidValue();
        if (fluid != Fluids.EMPTY) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                    .get(fluid.defaultFluidState()).stillMaterial().sprite();
            if (sprite != null) return sprite;
        }

        return sprites.get(WATER_SPRITE);
    }
}
