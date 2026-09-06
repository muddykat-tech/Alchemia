package muddykat.alchemia.compat.jei;

import muddykat.alchemia.Alchemia;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

@JeiPlugin
public class AlchemiaJeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "jei");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerFromDataComponentTypes(Items.POTION, DataComponents.POTION_CONTENTS);
        registration.registerFromDataComponentTypes(Items.SPLASH_POTION, DataComponents.POTION_CONTENTS);
        registration.registerFromDataComponentTypes(Items.LINGERING_POTION, DataComponents.POTION_CONTENTS);
    }
}
