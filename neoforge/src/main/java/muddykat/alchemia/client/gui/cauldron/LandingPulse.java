package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class LandingPulse {

    private static final long MS = 700L;

    private @Nullable Object key;
    private long at = Long.MIN_VALUE;
    private PathPoint point = PathPoint.ORIGIN;
    private int rgb;

    public void trigger(BrewContext context) {
        PotionMap.PotionEffectPosition found = context.cauldron().effectAt(context.brewAt());
        if (found.getEffect() == null || !found.isPerfect() || found.getRecipe() == null) return;
        PathPoint target = context.map().positionOf(found.getRecipe());
        if (target == null) return;

        List<Object> next = List.of(found.getRecipe(), found.getStrength());
        if (next.equals(key) && Util.getMillis() - at < MS * 3) return;
        key = next;
        at = Util.getMillis();
        point = target;
        rgb = context.knowledge().knows(found.getRecipe()) ? found.getEffect().value().getColor() : MapInk.UNKNOWN_RGB;
    }

    public double flash(long now) {
        return at == Long.MIN_VALUE ? 1.0 : Math.min(1.0, (now - at) / (double) MS);
    }

    public void draw(GuiGraphicsExtractor graphics, MapView view) {
        if (at == Long.MIN_VALUE) return;
        double t = (Util.getMillis() - at) / (double) MS;
        if (t >= 1.0) return;

        PathPoint centre = view.toScreen(point);
        double eased = PathGeometry.ease(t);
        double radius = (PotionMap.EFFECT_CORE + (PotionMap.EFFECT_RADIUS * 1.4 - PotionMap.EFFECT_CORE) * eased) * view.zoom();
        int alpha = (int) Math.round(220 * (1.0 - t));
        if (radius >= 2.0) {
            PathRenderer.ring(graphics, centre.x(), centre.y(), radius, ARGB.color(alpha, rgb), false);
            PathRenderer.ring(graphics, centre.x(), centre.y(), radius * 0.7, ARGB.color(alpha / 2, rgb), false);
        }
    }
}
