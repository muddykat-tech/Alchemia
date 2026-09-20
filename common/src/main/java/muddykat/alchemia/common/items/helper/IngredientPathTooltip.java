package muddykat.alchemia.common.items.helper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record IngredientPathTooltip(List<int[]> cells, int taken, boolean teleport,
                                    Component label) implements TooltipComponent {
}
