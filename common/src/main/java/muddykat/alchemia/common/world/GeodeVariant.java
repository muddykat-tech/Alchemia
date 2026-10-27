package muddykat.alchemia.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public enum GeodeVariant implements StringRepresentable {
    STANDARD("standard", ""),
    MINIATURE("miniature", "_mini"),
    GIANT("giant", "_giant"),
    SUPERMASSIVE("supermassive", "_supermassive"),
    SURFACE("surface", "_surface"),
    UNDERSIDE("underside", "_underside");

    public static final Codec<GeodeVariant> CODEC = StringRepresentable.fromEnum(GeodeVariant::values);
    public static final List<GeodeVariant> PER_MINERAL = List.of(STANDARD, MINIATURE, GIANT, SUPERMASSIVE, SURFACE);

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
