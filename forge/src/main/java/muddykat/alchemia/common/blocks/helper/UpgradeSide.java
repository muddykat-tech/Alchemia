package muddykat.alchemia.common.blocks.helper;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import javax.annotation.Nullable;

public enum UpgradeSide implements StringRepresentable {
    RIGHT("right", 5),
    LEFT("left", 4),
    TOP("top", 3);

    private final String id;
    private final int potionSlots;

    UpgradeSide(String id, int potionSlots) {
        this.id = id;
        this.potionSlots = potionSlots;
    }

    public String registryName() {
        return "alchemy_machine_" + id + "_upgrade";
    }

    public @Nullable UpgradeSide requires() {
        return this == TOP ? LEFT : null;
    }

    public int potionSlots() {
        return potionSlots;
    }

    public Direction directionFrom(Direction facing) {
        return switch (this) {
            case RIGHT -> facing.getCounterClockWise();
            case LEFT -> facing.getClockWise();
            case TOP -> Direction.UP;
        };
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
