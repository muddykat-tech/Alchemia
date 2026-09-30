package muddykat.alchemia.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum GeodeVariant implements StringRepresentable {
    STANDARD("standard", ""),
    MINIATURE("miniature", "_mini"),
    GIANT("giant", "_giant"),
    SUPERMASSIVE("supermassive", "_supermassive"),
    SURFACE("surface", "_surface");

    public static final Codec<GeodeVariant> CODEC = StringRepresentable.fromEnum(GeodeVariant::values);

    private final String name;
    private final String suffix;

    GeodeVariant(String name, String suffix) {
        this.name = name;
        this.suffix = suffix;
    }

    public String suffix() {
        return suffix;
    }

    public boolean isVanillaShaped() {
        return this == STANDARD;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
