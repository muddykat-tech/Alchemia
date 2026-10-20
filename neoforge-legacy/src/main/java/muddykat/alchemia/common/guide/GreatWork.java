package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.utility.StackData;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GreatWork {

    private GreatWork() {
    }

    public static List<String> stored(ItemStack guide) {
        return StackData.GREAT_WORK.getOrDefault(guide, List.of());
    }

    public static boolean knows(ItemStack guide, MagnumOpus.Stage stage) {
        return stored(guide).contains(stage.result());
    }

    public static int known(ItemStack guide) {
        int count = 0;
        for (MagnumOpus.Stage stage : MagnumOpus.stages()) {
            if (knows(guide, stage)) count++;
        }
        return count;
    }

    public static int total() {
        return MagnumOpus.stages().size();
    }

    public static @Nullable MagnumOpus.Stage next(ItemStack guide) {
        for (MagnumOpus.Stage stage : MagnumOpus.stages()) {
            if (!knows(guide, stage)) return stage;
        }
        return null;
    }

    public static @Nullable MagnumOpus.Stage byName(String name) {
        for (MagnumOpus.Stage stage : MagnumOpus.stages()) {
            if (stage.result().equals(name)) return stage;
        }
        return null;
    }

    public static boolean learn(ItemStack guide, Collection<String> names) {
        List<String> known = new ArrayList<>(stored(guide));
        boolean grew = false;
        for (String name : names) {
            if (byName(name) != null && !known.contains(name)) {
                known.add(name);
                grew = true;
            }
        }
        if (!grew) return false;

        List<String> ordered = new ArrayList<>();
        for (MagnumOpus.Stage stage : MagnumOpus.stages()) {
            if (known.contains(stage.result())) ordered.add(stage.result());
        }
        StackData.GREAT_WORK.set(guide, List.copyOf(ordered));
        return true;
    }

    public static ItemStack result(MagnumOpus.Stage stage) {
        return new ItemStack(ItemRegistry.getItemFromRegistry(stage.result()));
    }

    public static ItemStack input(MagnumOpus.Stage stage) {
        return stage.previous() == null ? ItemStack.EMPTY : new ItemStack(ItemRegistry.getItemFromRegistry(stage.previous()));
    }
}
