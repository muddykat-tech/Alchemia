package muddykat.alchemia.common.blocks.helper;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum UpgradeSide implements StringRepresentable {
    RIGHT("right", 5);

    private final String id;
    private final int potionSlots;

    UpgradeSide(String id, int potionSlots) {
        this.id = id;
        this.potionSlots = potionSlots;
    }

    public String registryName() {
        return "alchemy_machine_" + id + "_upgrade";
    }

    public int potionSlots() {
        return potionSlots;
    }

    public Direction directionFrom(Direction facing) {
        return switch (this) {
            case RIGHT -> facing.getClockWise();
        };
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
