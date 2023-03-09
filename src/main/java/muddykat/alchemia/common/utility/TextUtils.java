package muddykat.alchemia.common.utility;

import muddykat.alchemia.Alchemia;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TextUtils {

    public static MutableComponent getTranslation(String key, Object... args) {
        return new TranslatableComponent(Alchemia.MODID + "." + key, args);
    }


}
