package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.common.potion.PotionGlass;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class PotionLiquid {

    public record Corner(float x, float y, float z, float u, float v) {}

    public record Face(List<Corner> corners, float nx, float ny, float nz) {}

    private static final float INSET = 0.1F;

    private static final Map<PotionGlass, List<Face>> FACES = new EnumMap<>(PotionGlass.class);

    static {
        FACES.put(PotionGlass.NORMAL_POTION, mesh(4.9F,
                box(6, 0, 6, 10, 6, 10),
                box(6, 1, 5, 10, 5, 6),
                box(6, 1, 10, 10, 5, 11),
                box(5, 1, 6, 6, 5, 10),
                box(10, 1, 6, 11, 5, 10)));

        FACES.put(PotionGlass.POTION_PHIAL, mesh(6.9F,
                box(6, 2, 6, 10, 8, 10),
                box(7, 0, 7, 9, 1, 9),
                box(6, 3, 5, 10, 7, 6),
                box(6, 3, 10, 10, 7, 11),
                box(7, 2, 10, 9, 3, 11),
                box(7, 1, 9, 9, 2, 10),
                box(7, 1, 6, 9, 2, 7),
                box(7, 2, 5, 9, 3, 6),
                box(5, 3, 6, 6, 7, 10),
                box(10, 2, 7, 11, 3, 9),
                box(6, 1, 7, 10, 2, 9),
                box(5, 2, 7, 6, 3, 9),
                box(10, 3, 6, 11, 7, 10)));

        FACES.put(PotionGlass.SQUARE_PHIAL, mesh(4.9F,
                box(6, 0, 6, 10, 6, 10)));
    }

    private PotionLiquid() {
    }

    public static List<Face> faces(PotionGlass glass) {
        return FACES.get(glass);
    }

    private static float[] box(float x0, float y0, float z0, float x1, float y1, float z1) {
        return new float[]{x0, y0, z0, x1, y1, z1};
    }

    private static List<Face> mesh(float surface, float[]... boxes) {
        float[][] edges = {edges(boxes, 0, Float.NaN), edges(boxes, 1, surface), edges(boxes, 2, Float.NaN)};
        int nx = edges[0].length - 1, ny = edges[1].length - 1, nz = edges[2].length - 1;

        boolean[][][] filled = new boolean[nx][ny][nz];
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    float cx = (edges[0][i] + edges[0][i + 1]) / 2;
                    float cy = (edges[1][j] + edges[1][j + 1]) / 2;
                    float cz = (edges[2][k] + edges[2][k + 1]) / 2;
                    filled[i][j][k] = cy < surface && eroded(boxes, cx, cy, cz);
                }
            }
        }

        List<Face> faces = new ArrayList<>();
        int[] cell = new int[3];
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    if (!filled[i][j][k]) continue;
                    cell[0] = i;
                    cell[1] = j;
                    cell[2] = k;
                    for (int axis = 0; axis < 3; axis++) {
                        for (int sign = -1; sign <= 1; sign += 2) {
                            int[] neighbour = cell.clone();
                            neighbour[axis] += sign;
                            if (open(filled, neighbour)) faces.add(face(edges, cell, axis, sign));
                        }
                    }
                }
            }
        }
        return List.copyOf(faces);
    }

    private static float[] edges(float[][] boxes, int axis, float extra) {
        TreeSet<Float> values = new TreeSet<>();
        for (float[] box : boxes) {
            values.add(box[axis] + INSET);
            values.add(box[axis] - INSET);
            values.add(box[axis + 3] + INSET);
            values.add(box[axis + 3] - INSET);
        }
        if (!Float.isNaN(extra)) values.add(extra);
        float[] edges = new float[values.size()];
        int index = 0;
        for (float value : values) edges[index++] = value;
        return edges;
    }

    private static boolean eroded(float[][] boxes, float x, float y, float z) {
        float reach = INSET * 0.999F;
        for (int corner = 0; corner < 8; corner++) {
            float px = x + ((corner & 1) == 0 ? -reach : reach);
            float py = y + ((corner & 2) == 0 ? -reach : reach);
            float pz = z + ((corner & 4) == 0 ? -reach : reach);
            if (!inside(boxes, px, py, pz)) return false;
        }
        return true;
    }

    private static boolean inside(float[][] boxes, float x, float y, float z) {
        for (float[] box : boxes) {
            if (x >= box[0] && x <= box[3] && y >= box[1] && y <= box[4] && z >= box[2] && z <= box[5]) return true;
        }
        return false;
    }

    private static boolean open(boolean[][][] filled, int[] cell) {
        if (cell[0] < 0 || cell[1] < 0 || cell[2] < 0) return true;
        if (cell[0] >= filled.length || cell[1] >= filled[0].length || cell[2] >= filled[0][0].length) return true;
        return !filled[cell[0]][cell[1]][cell[2]];
    }

    private static Face face(float[][] edges, int[] cell, int axis, int sign) {
        int uAxis = (axis + 1) % 3;
        int vAxis = (axis + 2) % 3;
        float plane = edges[axis][cell[axis] + (sign > 0 ? 1 : 0)];
        float u0 = edges[uAxis][cell[uAxis]], u1 = edges[uAxis][cell[uAxis] + 1];
        float v0 = edges[vAxis][cell[vAxis]], v1 = edges[vAxis][cell[vAxis] + 1];

        float[][] square = sign > 0
                ? new float[][]{{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}}
                : new float[][]{{u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}};

        List<Corner> corners = new ArrayList<>(4);
        for (float[] point : square) {
            float[] position = new float[3];
            position[axis] = plane;
            position[uAxis] = point[0];
            position[vAxis] = point[1];
            corners.add(corner(position, axis));
        }

        float[] normal = new float[3];
        normal[axis] = sign;
        return new Face(List.copyOf(corners), normal[0], normal[1], normal[2]);
    }

    private static Corner corner(float[] pixels, int axis) {
        float x = pixels[0] / 16, y = pixels[1] / 16, z = pixels[2] / 16;
        return switch (axis) {
            case 0 -> new Corner(x, y, z, z, 1 - y);
            case 1 -> new Corner(x, y, z, x, z);
            default -> new Corner(x, y, z, x, 1 - y);
        };
    }
}
