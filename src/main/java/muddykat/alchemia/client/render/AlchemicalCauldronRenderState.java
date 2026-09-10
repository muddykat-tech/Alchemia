package muddykat.alchemia.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

public class AlchemicalCauldronRenderState extends BlockEntityRenderState {
    public int waterLevel;
    public int potionColor;
    public @Nullable TextureAtlasSprite fluidSprite;
}
