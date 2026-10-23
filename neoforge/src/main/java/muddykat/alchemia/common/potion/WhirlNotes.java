package muddykat.alchemia.common.potion;

import muddykat.alchemia.registration.registers.DataComponentRegistry;
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
        return new HashSet<>(guide.getOrDefault(DataComponentRegistry.WHIRL_EXITS.get(), List.<String>of()));
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
            List<String> known = new ArrayList<>(stack.getOrDefault(DataComponentRegistry.WHIRL_EXITS.get(), List.<String>of()));
            boolean grew = false;
            for (String mark : marks) {
                if (known.contains(mark)) continue;
                known.add(mark);
                grew = true;
            }
            if (!grew) continue;

            known.sort(null);
            stack.set(DataComponentRegistry.WHIRL_EXITS.get(), List.copyOf(known));
            changed = true;
        }
        return changed;
    }
}
