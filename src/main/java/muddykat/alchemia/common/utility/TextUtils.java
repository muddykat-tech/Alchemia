package muddykat.alchemia.common.utility;

import muddykat.alchemia.Alchemia;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextUtils {
    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable(Alchemia.MODID + "." + key, args);
    }
}
