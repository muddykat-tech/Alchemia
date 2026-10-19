package muddykat.alchemia.common;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.BrewBaseConfig;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static muddykat.alchemia.Alchemia.MOD_NAME;

public class CommonSetup {

    public static void init(final FMLCommonSetupEvent event) {
        Alchemia.LOGGER.info(MOD_NAME + " Common Setup");
        event.enqueueWork(() -> {
            BrewBaseConfig.load();
            PotionMap.scramble(0L);

            BrewDispenseBehavior.registerBottling(Items.GLASS_BOTTLE);
            for (PotionGlass glass : PotionGlass.values()) {
                if (glass.hasEmptyItem()) BrewDispenseBehavior.registerBottling(PotionGlasses.emptyItem(glass));
            }
            BrewDispenseBehavior.registerFilling(Items.WATER_BUCKET);
            AncientOakFlammability.register();
        });
    }
}
