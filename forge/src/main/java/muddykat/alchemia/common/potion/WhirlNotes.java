package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.utility.StackData;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WhirlNotes {

    private WhirlNotes() {
    }

    public static Set<String> exitsIn(ItemStack guide) {
        return new HashSet<>(StackData.WHIRL_EXITS.getOrDefault(guide, List.<String>of()));
    }

    public static boolean reveal(Iterable<ItemStack> guides, String mark) {
        return learn(guides, List.of(mark));
    }

    public static boolean learn(ItemStack guide, Collection<String> marks) {
        return learn(List.of(guide), marks);
    }

    private static boolean learn(Iterable<ItemStack> guides, Collection<String> marks) {
        if (marks.isEmpty()) return false;

        boolean changed = false;
        for (ItemStack stack : guides) {
            List<String> known = new ArrayList<>(StackData.WHIRL_EXITS.getOrDefault(stack, List.<String>of()));
            boolean grew = false;
            for (String mark : marks) {
                if (known.contains(mark)) continue;
                known.add(mark);
                grew = true;
            }
            if (!grew) continue;

            known.sort(null);
            StackData.WHIRL_EXITS.set(stack, List.copyOf(known));
            changed = true;
        }
        return changed;
    }
}
