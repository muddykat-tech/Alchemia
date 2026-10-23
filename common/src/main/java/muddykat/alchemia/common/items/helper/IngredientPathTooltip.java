package muddykat.alchemia.common.items.helper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record IngredientPathTooltip(List<PathPoint> points, double taken, boolean teleport,
                                    Component label, int crush) implements TooltipComponent {

    public List<PathPoint> reached() {
        return PathGeometry.truncate(points, taken);
    }

    public double span() {
        return PathGeometry.length(points);
    }
}
