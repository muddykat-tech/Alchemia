package muddykat.alchemia.common.potion;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum PotionGlass implements StringRepresentable {
    NORMAL_POTION(10, 1.6F),
    POTION_PHIAL(12, 0.8F),
    SQUARE_PHIAL(9, 0.8F);

    public static final Codec<PotionGlass> CODEC = StringRepresentable.fromEnum(PotionGlass::values);

    private final int height;
    private final float drinkSeconds;

    PotionGlass(int height, float drinkSeconds) {
        this.height = height;
        this.drinkSeconds = drinkSeconds;
    }

    public int height() {
        return height;
    }

    public float drinkSeconds() {
        return drinkSeconds;
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
