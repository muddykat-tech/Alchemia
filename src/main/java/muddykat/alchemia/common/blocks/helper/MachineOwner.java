package muddykat.alchemia.common.blocks.helper;

import net.minecraft.util.StringRepresentable;

import java.util.List;

public enum MachineOwner implements StringRepresentable {
    CORE("core"),
    RIGHT("right"),
    LEFT("left"),
    TOP("top");

    private final String id;

    MachineOwner(String id) {
        this.id = id;
    }

    public List<AlchemyMachineShapes.Cell> cells() {
        return switch (this) {
            case CORE -> AlchemyMachineShapes.CORE;
            case RIGHT -> AlchemyMachineShapes.RIGHT;
            case LEFT -> AlchemyMachineShapes.LEFT;
            case TOP -> AlchemyMachineShapes.TOP;
        };
    }

    public String registryName() {
        return this == CORE ? "alchemy_machine_core" : "alchemy_machine_" + id + "_upgrade";
    }

    public static MachineOwner of(UpgradeSide side) {
        return switch (side) {
            case RIGHT -> RIGHT;
            case LEFT -> LEFT;
            case TOP -> TOP;
        };
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
