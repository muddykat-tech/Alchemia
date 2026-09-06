package muddykat.alchemia.common.utility;

import muddykat.alchemia.Alchemia;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextUtils {

    public static final int PARCHMENT = 0xC9A96E;

    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable(Alchemia.MODID + "." + key, args);
    }

    public static MutableComponent brewingSource() {
        return Component.translatable("alchemia.tooltip.source")
                .withStyle(style -> style.withColor(PARCHMENT));
    }
}
