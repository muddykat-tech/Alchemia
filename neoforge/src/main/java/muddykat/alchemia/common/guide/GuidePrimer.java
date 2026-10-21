package muddykat.alchemia.common.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GuidePrimer {

    public static final Identifier BUBBLE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "cauldron");

    public static final int PHOTO_W = 128;
    public static final int PHOTO_H = 72;
    public static final int PHOTO_SCALE = 2;

    public static final String INTRO = "intro";

    public sealed interface CraftSource permits Craft, CraftRecipe {
    }

    public record Craft(List<String> pattern, Map<Character, Identifier> key, Identifier result, int count) implements CraftSource {
    }

    public record CraftRecipe(Identifier id) implements CraftSource {
    }

    public record Chapter(String key, List<String> sections) {
    }

    public static final List<Chapter> CHAPTERS = List.of(
            new Chapter("cauldron", List.of("cauldron", "facing")),
            new Chapter("bases", List.of("bases", "filling")),
            new Chapter("ingredients", List.of("ingredients", "grinding")),
            new Chapter("screen", List.of("screen", "map")),
            new Chapter("stirring", List.of("stirring", "brewing")),
            new Chapter("effects", List.of("effects", "dead_space")),
            new Chapter("bottling", List.of("bottling", "glasses")),
            new Chapter("modifiers", List.of("modifiers", "lures", "imbuements", "bark", "finishing")),
            new Chapter("recording", List.of("recording", "desk")),
            new Chapter("beyond", List.of("beyond")));

    private static volatile Set<Identifier> earned = Set.of();
    private static volatile Set<String> chapters = Set.of();
    private static volatile boolean synced;
    private static volatile int generation;

    private GuidePrimer() {
    }

    public static List<List<Identifier>> live(String token) {
        if (token.equals("bases")) {
            List<List<Identifier>> slots = new ArrayList<>();
            for (BrewBase base : BrewBases.values()) {
                List<Identifier> items = new ArrayList<>();
                for (BrewBase.Fill fill : base.fills()) fill.item().ifPresent(items::add);
                if (!items.isEmpty()) slots.add(items);
            }
            return slots;
        }
        return List.of();
    }

    public static void install(Collection<Identifier> advancements, Collection<String> unlocked) {
        earned = Set.copyOf(advancements);
        chapters = Set.copyOf(unlocked);
        synced = true;
        generation++;
    }

    public static void latch(String chapter) {
        Set<String> next = new HashSet<>(chapters);
        if (!next.add(chapter)) return;
        chapters = Set.copyOf(next);
        generation++;
    }

    public static void reset() {
        earned = Set.of();
        chapters = Set.of();
        synced = false;
        generation++;
    }

    public static boolean synced() {
        return synced;
    }

    public static boolean earned(Identifier advancement) {
        return earned.contains(advancement);
    }

    public static boolean latched(String chapter) {
        return chapters.contains(chapter);
    }

    public static int generation() {
        return generation;
    }

    public static @Nullable Chapter chapter(String key) {
        for (Chapter chapter : CHAPTERS) {
            if (chapter.key().equals(key)) return chapter;
        }
        return null;
    }

    public static Identifier photo(String name) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/primer/" + name + ".png");
    }

    public static Identifier frame() {
        return photo("photo_frame");
    }

    public static Identifier craftFrame() {
        return photo("craft_frame");
    }

    public static Identifier arrows() {
        return photo("photo_arrows");
    }
}
