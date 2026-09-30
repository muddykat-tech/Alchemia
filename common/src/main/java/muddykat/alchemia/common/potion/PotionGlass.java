package muddykat.alchemia.common.potion;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum PotionGlass implements StringRepresentable {
    NORMAL_POTION(10),
    POTION_PHIAL(12),
    SQUARE_PHIAL(9);

    public static final Codec<PotionGlass> CODEC = StringRepresentable.fromEnum(PotionGlass::values);

    private final int height;

    PotionGlass(int height) {
        this.height = height;
    }

    public int height() {
        return height;
    }

    public boolean hasEmptyItem() {
        return this != NORMAL_POTION;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PotionGlass byName(String name) {
        for (PotionGlass glass : values()) {
            if (glass.getSerializedName().equals(name)) return glass;
        }
        return NORMAL_POTION;
    }
}
