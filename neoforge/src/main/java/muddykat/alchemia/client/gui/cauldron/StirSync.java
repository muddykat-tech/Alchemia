package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.PotionMap;

import java.util.ArrayDeque;
import java.util.function.IntConsumer;

public final class StirSync {

    private static final int MAX_PENDING = 40;

    private final ArrayDeque<Integer> pending = new ArrayDeque<>();
    private int base;
    private int sent;
    private BrewCourse shown = new BrewCourse(0.0, 0.0);
    private PathPoint brewAt = PathPoint.ORIGIN;

    public BrewCourse course() {
        return shown;
    }

    public PathPoint brewAt() {
        return brewAt;
    }

    public void reset(int stirCount) {
        base = stirCount;
        sent = 0;
        pending.clear();
    }

    public void send(int units, boolean fast, IntConsumer button) {
        if (pending.size() >= MAX_PENDING || units <= 0) return;

        int count = Math.min(AlchemicalCauldronMenu.MAX_STIRS_PER_CLICK, units * (fast ? TileEntityAlchemyCauldron.FAST_STIRS : 1));
        button.accept(count == 1 ? AlchemicalCauldronMenu.BUTTON_STIR : AlchemicalCauldronMenu.BUTTON_STIR_MANY + count);
        pending.addLast(count);
        sent++;
    }

    public void reconcile(TileEntityAlchemyCauldron cauldron) {
        int confirmed = cauldron.getStirCount() - base;

        if (confirmed < 0) {
            reset(cauldron.getStirCount());
            confirmed = 0;
        } else if (confirmed > sent) {
            base += confirmed - sent;
            confirmed = sent;
        }

        while (pending.size() > sent - confirmed) pending.removeFirst();

        if (!PotionMap.isReady()) {
            shown = cauldron.getCourse().copy();
            brewAt = new PathPoint(cauldron.getXAlignment(), cauldron.getYAlignment());
            return;
        }

        PotionMap map = PotionMap.get(cauldron.getBase());
        cauldron.getCourse().legs(map);
        shown = cauldron.getCourse().copy();
        for (int stirs : pending) {
            for (int count = 0; count < stirs && !shown.teleporting(map); count++) {
                shown.stir(map, TileEntityAlchemyCauldron.STIR_STEP, arrival -> true);
            }
        }
        brewAt = shown.position(map);
    }
}
