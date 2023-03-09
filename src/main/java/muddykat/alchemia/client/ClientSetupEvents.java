package muddykat.alchemia.client;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetupEvents {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), AlchemicalCauldronRenderer::new);
    }
}
