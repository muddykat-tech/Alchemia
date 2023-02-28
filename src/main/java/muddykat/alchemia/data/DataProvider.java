package muddykat.alchemia.data;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.data.generators.AlchemiaBlockStateProvider;
import muddykat.alchemia.data.generators.AlchemiaItemModelProvider;
import muddykat.alchemia.data.generators.loot.AlchemiaLootProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = Alchemia.MODID, bus = Bus.MOD)
public class DataProvider {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        ExistingFileHelper helper = event.getExistingFileHelper();
        DataGenerator gen = event.getGenerator();
        if(event.includeServer()){

        }

        if(event.includeClient()){
            gen.addProvider(new AlchemiaItemModelProvider(gen, helper));
            gen.addProvider(new AlchemiaBlockStateProvider(gen, helper));
            gen.addProvider(new AlchemiaLootProvider(gen));
        }
    }
}
