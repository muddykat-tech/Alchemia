package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Alchemia.MODID);

    public static final RegistryObject<SoundEvent> GUIDE_STAMP = SOUNDS.register("guide.stamp",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Alchemia.MODID, "guide.stamp")));
}
