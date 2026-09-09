package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.config.Configuration;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public enum BrewBase implements StringRepresentable {
    WATER("water", 0x3F76E4, 1, Items.WATER_BUCKET, Items.BUCKET, null, null),
    HONEY("honey", 0xD9871F, 2, Items.HONEY_BLOCK, null, Items.HONEY_BOTTLE, Items.GLASS_BOTTLE);

    private final String id;
    private final int fluidColor;
    private final int instabilityCap;
    private final Item fullFill;
    private final @Nullable Item fullFillRemainder;
    private final @Nullable Item portionFill;
    private final @Nullable Item portionRemainder;

    BrewBase(String id, int fluidColor, int instabilityCap, Item fullFill, @Nullable Item fullFillRemainder,
             @Nullable Item portionFill, @Nullable Item portionRemainder) {
        this.id = id;
        this.fluidColor = fluidColor;
        this.instabilityCap = instabilityCap;
        this.fullFill = fullFill;
        this.fullFillRemainder = fullFillRemainder;
        this.portionFill = portionFill;
        this.portionRemainder = portionRemainder;
    }

    public int fluidColor() {
        return fluidColor;
    }

    public boolean usesBiomeColor() {
        return this == WATER;
    }

    public int instabilityCap() {
        return instabilityCap;
    }

    public Item fullFill() {
        return fullFill;
    }

    public @Nullable Item fullFillRemainder() {
        return fullFillRemainder;
    }

    public @Nullable Item portionFill() {
        return portionFill;
    }

    public @Nullable Item portionRemainder() {
        return portionRemainder;
    }

    public boolean allows(@Nullable PotionEnum recipe) {
        return recipe == null || Configuration.baseAllowsEffect(this, recipe);
    }

    public ItemStack unbrewedResult() {
        return this == HONEY ? new ItemStack(Items.HONEY_BOTTLE) : ItemStack.EMPTY;
    }

    public String translationKey() {
        return "alchemia.base." + id;
    }

    public static @Nullable BrewBase byFullFill(ItemStack stack) {
        for (BrewBase base : values()) {
            if (stack.is(base.fullFill)) return base;
        }
        return null;
    }

    public static @Nullable BrewBase byPortionFill(ItemStack stack) {
        for (BrewBase base : values()) {
            if (base.portionFill != null && stack.is(base.portionFill)) return base;
        }
        return null;
    }

    public static BrewBase byName(String name) {
        for (BrewBase base : values()) {
            if (base.id.equals(name)) return base;
        }
        return WATER;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
