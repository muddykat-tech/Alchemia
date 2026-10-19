package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class Lettering {

    public static final FontDescription ALAGARD = new FontDescription.Resource(Identifier.fromNamespaceAndPath(Alchemia.MODID, "alagard"));
    public static final int RISE = 4;
    public static final int HEIGHT = 14;

    private Lettering() {
    }

    public static MutableComponent of(String text) {
        return Component.literal(text).withStyle(style -> style.withFont(ALAGARD));
    }

    public static MutableComponent of(Component text) {
        return text.copy().withStyle(style -> style.withFont(ALAGARD));
    }
}
