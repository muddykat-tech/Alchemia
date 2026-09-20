package muddykat.alchemia.common.blocks.helper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class AlchemyMachineShapes {

    public record Cell(int x, int y, int z, VoxelShape shape) {}

    private AlchemyMachineShapes() {
    }

    public static final List<Cell> CORE = List.of(
        new Cell(-1, 0, 0, Shapes.or(
            Block.box(8, 0, 0, 16, 16, 16))),
        new Cell(0, 0, -1, Shapes.or(
            Block.box(0, 0, 12, 16, 4, 16))),
        new Cell(0, 0, 0, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(0, 0, 1, Shapes.or(
            Block.box(0, 0, 0, 16, 12, 4))),
        new Cell(1, 0, 0, Shapes.or(
            Block.box(0, 0, 0, 4, 12, 16))),
        new Cell(-1, 1, 0, Shapes.or(
            Block.box(8, 0, 0, 16, 8, 8))),
        new Cell(0, 1, 0, Shapes.or(
            Block.box(0, 0, 0, 16, 12, 16))),
        new Cell(1, 1, 0, Shapes.or(
            Block.box(0, 0, 0, 8, 12, 8)))
    );

    public static final List<Cell> RIGHT = List.of(
        new Cell(-1, 0, 0, Shapes.or(
            Block.box(12, 0, 0, 16, 16, 16))),
        new Cell(-1, 0, 1, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(0, 0, 0, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(0, 0, 1, Shapes.or(
            Block.box(0, 0, 4, 12, 16, 16))),
        new Cell(-1, 1, 0, Shapes.or(
            Block.box(12, 0, 4, 16, 8, 12))),
        new Cell(-1, 1, 1, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(0, 1, 0, Shapes.or(
            Block.box(0, 0, 4, 16, 16, 12))),
        new Cell(0, 1, 1, Shapes.or(
            Block.box(0, 0, 4, 16, 16, 16))),
        new Cell(1, 1, 0, Shapes.or(
            Block.box(0, 8, 4, 12, 16, 16))),
        new Cell(1, 1, 1, Shapes.or(
            Block.box(0, 12, 0, 12, 16, 12))),
        new Cell(-1, 2, 1, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 12))),
        new Cell(0, 2, 1, Shapes.or(
            Block.box(0, 0, 4, 12, 16, 16))),
        new Cell(-1, 3, 1, Shapes.or(
            Block.box(4, 0, 4, 12, 8, 12)))
    );

    public static final List<Cell> LEFT = List.of(
        new Cell(-1, 0, 0, Shapes.or(
            Block.box(12, 8, 8, 16, 16, 16))),
        new Cell(0, 0, 0, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(0, 0, 1, Shapes.or(
            Block.box(0, 8, 0, 16, 16, 4))),
        new Cell(1, 0, 0, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(1, 0, 1, Shapes.or(
            Block.box(0, 0, 0, 12, 16, 8))),
        new Cell(2, 0, 0, Shapes.or(
            Block.box(8, 0, 0, 12, 16, 16))),
        new Cell(0, 1, 0, Shapes.or(
            Block.box(4, 0, 0, 16, 12, 16))),
        new Cell(1, 1, 0, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16))),
        new Cell(1, 1, 1, Shapes.or(
            Block.box(0, 0, 0, 12, 16, 8))),
        new Cell(2, 1, 0, Shapes.or(
            Block.box(0, 0, 0, 12, 16, 16))),
        new Cell(1, 2, 1, Shapes.or(
            Block.box(0, 0, 0, 12, 8, 8)))
    );

    public static final List<Cell> TOP = List.of(
        new Cell(0, -1, 1, Shapes.or(
            Block.box(4, 0, 0, 16, 16, 8))),
        new Cell(0, 0, 0, Shapes.or(
            Block.box(8, 8, 8, 16, 16, 12))),
        new Cell(0, 0, 1, Shapes.or(
            Block.box(8, 0, 0, 12, 16, 4))),
        new Cell(1, 0, 0, Shapes.or(
            Block.box(8, 8, 4, 16, 16, 12))),
        new Cell(-1, 1, 0, Shapes.or(
            Block.box(8, 0, 4, 16, 16, 16))),
        new Cell(0, 1, 0, Shapes.or(
            Block.box(0, 0, 4, 16, 16, 16))),
        new Cell(0, 1, 1, Shapes.or(
            Block.box(8, 0, 0, 16, 16, 4))),
        new Cell(1, 1, 0, Shapes.or(
            Block.box(0, 0, 4, 16, 16, 16))),
        new Cell(-1, 2, 0, Shapes.or(
            Block.box(8, 0, 4, 16, 12, 16))),
        new Cell(0, 2, 0, Shapes.or(
            Block.box(0, 0, 4, 12, 8, 16))),
        new Cell(0, 2, 1, Shapes.or(
            Block.box(0, 0, 0, 12, 12, 4))),
        new Cell(1, 2, 0, Shapes.or(
            Block.box(0, 0, 8, 8, 4, 16)))
    );

}
