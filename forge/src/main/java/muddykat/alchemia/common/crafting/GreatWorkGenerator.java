package muddykat.alchemia.common.crafting;

import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.WorldEffects;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import static muddykat.alchemia.common.potion.PotionEnum.*;

public final class GreatWorkGenerator {

    public static final int VERSION = 3;
    private static final long SALT = 0x6A09E667F3BCC909L;
    private static final int ATTEMPTS = 600;
    private static final double OPPOSED = 0.7;
    private static final double SIDE_BY_SIDE = 0.25;
    private static final int TOP_LEVEL = 3;

    public enum Opposition {
        ALIGNED,
        UNOPPOSED,
        ONE_OPPOSED,
        EACH_OPPOSED
    }

    public record Template(int count, int composites, int minEffects, int maxEffects, int potency, Opposition opposition, boolean waited,
                           double floor, double ceiling) {
    }

    public record Flask(Map<PotionEnum, Integer> levels) {

        public int potency() {
            return levels.values().stream().mapToInt(Integer::intValue).sum();
        }

        public PotionEffectIngredient ingredient() {
            List<PotionEffectIngredient.Requirement> required = new ArrayList<>();
            levels.forEach((potion, level) -> required.add(PotionEffectIngredient.need(potion.getEffect(), level)));
            return new PotionEffectIngredient(List.copyOf(required));
        }
    }

    private static final List<PotionEnum> EVERYTHING = List.of(PotionEnum.values());

    private static final List<List<PotionEnum>> THEMES = List.of(
            List.of(Slow, Poison, Weakness, Blindness, Dig_Slowdown, Hunger, Damage_Resistance, Damage_Boost),
            List.of(Invisibility, Speed, Night_Vision, Dig_Speed, Levitation, Slow_Falling, Dolphins_Grace, Jump, Water_Breathing),
            List.of(Glowing, Fire_Protection, Harm, Damage_Boost, Dig_Speed, Conduit_Power, Bad_Omen, Saturation, Speed, Night_Vision,
                    Luck),
            List.of(Regeneration, Health_Boost, Absorption, Saturation, Healing, Damage_Boost, Wither, Damage_Resistance, Fire_Protection,
                    Speed, Jump, Night_Vision, Luck),
            EVERYTHING);

    private static final List<List<PotionEnum>> FOILS = List.of(
            List.of(Speed, Healing, Night_Vision, Dig_Speed),
            List.of(Slow, Darkness, Weakness, Dig_Slowdown, Fire_Protection),
            List.of(Water_Breathing, Slow, Dolphins_Grace, Absorption, Slow_Falling, Weakness, Dig_Slowdown, Hunger, Darkness),
            List.of(Poison, Hunger, Weakness, Harm, Blindness, Darkness, Slow, Dig_Slowdown, Confusion, Glowing),
            EVERYTHING);

    public static final List<Template> TEMPLATES = List.of(
            new Template(5, 1, 2, 2, 3, Opposition.ALIGNED, false, 0, 28),
            new Template(7, 2, 2, 2, 4, Opposition.UNOPPOSED, false, 36, 52),
            new Template(9, 3, 2, 3, 5, Opposition.ONE_OPPOSED, false, 56, 80),
            new Template(11, 4, 3, 3, 5, Opposition.EACH_OPPOSED, false, 85, 108),
            new Template(12, 4, 3, 5, 5, Opposition.EACH_OPPOSED, true, 112, 160));

    private GreatWorkGenerator() {
    }

    public static List<MagnumOpus.Stage> generate(long seed) {
        List<MagnumOpus.Stage> stages = new ArrayList<>();
        double floor = -1;
        for (int index = 0; index < MagnumOpus.CLASSIC.size(); index++) {
            MagnumOpus.Stage classic = MagnumOpus.CLASSIC.get(index);
            List<Flask> flasks = null;
            for (int attempt = 0; attempt < ATTEMPTS && flasks == null; attempt++) {
                Random random = new Random(mix(seed, index, attempt));
                List<Flask> candidate = stage(index, TEMPLATES.get(index).count(), random);
                if (candidate != null && fits(TEMPLATES.get(index), score(candidate), floor, attempt)) flasks = candidate;
            }
            if (flasks == null) {
                stages.add(classic);
                continue;
            }
            floor = score(flasks);
            List<PotionEffectIngredient> potions = new ArrayList<>();
            for (Flask flask : flasks) potions.add(flask.ingredient());
            stages.add(new MagnumOpus.Stage(classic.result(), classic.previous(), List.copyOf(potions)));
        }
        return List.copyOf(stages);
    }

    private static boolean fits(Template template, double score, double previous, int attempt) {
        if (score <= previous) return false;
        if (attempt < ATTEMPTS / 2) return score >= template.floor() && score <= template.ceiling();
        return score <= template.ceiling();
    }

    static long mix(long seed, int index, int attempt) {
        long mixed = seed ^ SALT ^ ((long) index << 40) ^ ((long) attempt * 0x9E3779B97F4A7C15L);
        mixed ^= mixed >>> 33;
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= mixed >>> 33;
        mixed *= 0xC4CEB9FE1A85EC53L;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    private static @Nullable List<Flask> stage(int index, int count, Random random) {
        Template template = TEMPLATES.get(index);
        int budget = Math.min(template.potency(), Configuration.maxPotency());
        int composites = Math.min(template.composites(), count);
        List<PotionEnum> theme = usable(THEMES.get(index), 1);
        List<PotionEnum> pool = new ArrayList<>(theme);
        for (PotionEnum foil : usable(FOILS.get(index), 1)) {
            if (!pool.contains(foil)) pool.add(foil);
        }

        List<Flask> flasks = new ArrayList<>();
        Set<PotionEnum> taken = EnumSet.noneOf(PotionEnum.class);
        boolean opposedSomewhere = false;
        for (int made = 0; made < composites; made++) {
            int effects = template.minEffects() + random.nextInt(template.maxEffects() - template.minEffects() + 1);
            effects = Math.min(effects, budget);
            if (effects < 2) return null;
            Flask flask = composite(free(theme, taken), free(pool, taken), effects, budget, random);
            if (flask == null) return null;

            double opposition = strongestOpposition(flask.levels().keySet());
            switch (template.opposition()) {
                case ALIGNED -> {
                    if (opposition > SIDE_BY_SIDE) return null;
                }
                case UNOPPOSED -> {
                    if (opposition >= OPPOSED) return null;
                }
                case EACH_OPPOSED -> {
                    if (opposition < OPPOSED) return null;
                }
                case ONE_OPPOSED -> {
                }
            }
            if (opposition >= OPPOSED) opposedSomewhere = true;
            flasks.add(flask);
            taken.addAll(flask.levels().keySet());
        }
        if (template.opposition() == Opposition.ONE_OPPOSED && !opposedSomewhere) return null;

        List<PotionEnum> singles = free(usable(theme, TOP_LEVEL), taken);
        java.util.Collections.shuffle(singles, random);
        List<PotionEnum> spare = free(usable(FOILS.get(index), TOP_LEVEL), taken);
        spare.removeAll(singles);
        java.util.Collections.shuffle(spare, random);
        singles.addAll(spare);
        for (PotionEnum potion : singles) {
            if (flasks.size() >= count) break;
            int level = Math.min(TOP_LEVEL, Math.min(potion.getMaxStrength() + 1, Configuration.maxPotency()));
            Map<PotionEnum, Integer> levels = new TreeMap<>();
            levels.put(potion, level);
            Flask flask = new Flask(levels);
            if (!taken.add(potion) || !valid(flask)) continue;
            flasks.add(flask);
        }
        if (flasks.size() != count) return null;
        if (template.waited() && !waits(flasks)) return null;
        return flasks;
    }

    private static @Nullable Flask composite(List<PotionEnum> theme, List<PotionEnum> pool, int effects, int budget, Random random) {
        if (theme.isEmpty()) return null;
        Set<PotionEnum> chosen = new LinkedHashSet<>();
        chosen.add(theme.get(random.nextInt(theme.size())));
        List<PotionEnum> open = new ArrayList<>(pool);
        open.removeAll(chosen);
        while (chosen.size() < effects && !open.isEmpty()) chosen.add(open.remove(random.nextInt(open.size())));
        if (chosen.size() < effects) return null;

        Map<PotionEnum, Integer> levels = new TreeMap<>();
        for (PotionEnum potion : chosen) levels.put(potion, 1);
        int spare = budget - effects;
        List<PotionEnum> order = new ArrayList<>(chosen);
        while (spare > 0) {
            List<PotionEnum> room = new ArrayList<>();
            for (PotionEnum potion : order) {
                if (levels.get(potion) < Math.min(TOP_LEVEL, potion.getMaxStrength() + 1)) room.add(potion);
            }
            if (room.isEmpty()) break;
            PotionEnum raised = room.get(random.nextInt(room.size()));
            levels.put(raised, levels.get(raised) + 1);
            spare--;
        }
        Flask flask = new Flask(levels);
        return valid(flask) ? flask : null;
    }

    private static List<PotionEnum> free(List<PotionEnum> potions, Set<PotionEnum> taken) {
        List<PotionEnum> free = new ArrayList<>(potions);
        free.removeAll(taken);
        return free;
    }

    private static List<PotionEnum> usable(List<PotionEnum> potions, int level) {
        List<PotionEnum> usable = new ArrayList<>();
        for (PotionEnum potion : potions) {
            if (potion.getEffect() == null || potion.getMaxStrength() + 1 < level) continue;
            if (!brewable(List.of(potion))) continue;
            usable.add(potion);
        }
        return usable;
    }

    public static boolean valid(Flask flask) {
        if (flask.levels().isEmpty() || flask.potency() > Configuration.maxPotency()) return false;
        for (Map.Entry<PotionEnum, Integer> entry : flask.levels().entrySet()) {
            if (entry.getValue() < 1 || entry.getValue() > entry.getKey().getMaxStrength() + 1) return false;
        }
        if (!brewable(flask.levels().keySet())) return false;

        Set<List<?>> places = new HashSet<>();
        boolean high = false;
        boolean low = false;
        for (Map.Entry<PotionEnum, Integer> entry : flask.levels().entrySet()) {
            for (WorldEffects.Condition condition : conditions(entry.getKey(), entry.getValue())) {
                switch (condition.rule()) {
                    case BIOME -> places.add(List.of(condition.biomes(), condition.tags()));
                    case ABOVE -> high = true;
                    case BELOW -> low = true;
                    default -> {
                    }
                }
            }
        }
        return places.size() <= 1 && !(high && low);
    }

    private static boolean brewable(Collection<PotionEnum> potions) {
        for (BrewBase base : BrewBases.values()) {
            boolean all = true;
            for (PotionEnum potion : potions) {
                if (!base.allows(potion)) {
                    all = false;
                    break;
                }
            }
            if (all) return true;
        }
        return false;
    }

    public static List<WorldEffects.Condition> conditions(PotionEnum potion, int level) {
        List<WorldEffects.Condition> conditions = new ArrayList<>();
        WorldEffects.Rules rules = WorldEffects.rules(potion);
        if (rules == null) return conditions;
        if (rules.wake() != null) conditions.addAll(rules.wake().conditions());
        rules.levels().forEach((tier, requirement) -> {
            if (tier <= level) conditions.addAll(requirement.conditions());
        });
        return conditions;
    }

    private static boolean waited(WorldEffects.Condition condition) {
        return switch (condition.rule()) {
            case ABOVE, BELOW, BIOME -> false;
            default -> true;
        };
    }

    private static boolean waits(List<Flask> flasks) {
        for (Flask flask : flasks) {
            for (Map.Entry<PotionEnum, Integer> entry : flask.levels().entrySet()) {
                for (WorldEffects.Condition condition : conditions(entry.getKey(), entry.getValue())) {
                    if (waited(condition)) return true;
                }
            }
        }
        return false;
    }

    public static double opposition(PotionEnum a, PotionEnum b) {
        int ax = a.getPrimaryAlignment().getX() + a.getSecondaryAlignment().getX();
        int ay = a.getPrimaryAlignment().getY() + a.getSecondaryAlignment().getY();
        int bx = b.getPrimaryAlignment().getX() + b.getSecondaryAlignment().getX();
        int by = b.getPrimaryAlignment().getY() + b.getSecondaryAlignment().getY();
        double lengths = Math.sqrt((double) ax * ax + ay * ay) * Math.sqrt((double) bx * bx + by * by);
        if (lengths == 0) return 0;
        return -(ax * bx + ay * by) / lengths;
    }

    private static double strongestOpposition(Collection<PotionEnum> potions) {
        List<PotionEnum> list = new ArrayList<>(potions);
        double strongest = -1;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) strongest = Math.max(strongest, opposition(list.get(i), list.get(j)));
        }
        return strongest;
    }

    public static double score(Flask flask) {
        List<PotionEnum> potions = new ArrayList<>(flask.levels().keySet());
        double score = flask.potency() + 2.0 * (potions.size() - 1);
        for (int i = 0; i < potions.size(); i++) {
            for (int j = i + 1; j < potions.size(); j++) score += 3.0 * Math.max(0, opposition(potions.get(i), potions.get(j)));
        }
        for (Map.Entry<PotionEnum, Integer> entry : flask.levels().entrySet()) {
            score += 0.5 * entry.getKey().getRarity().ordinal();
            for (WorldEffects.Condition condition : conditions(entry.getKey(), entry.getValue())) score += waited(condition) ? 3 : 1;
        }
        return score;
    }

    public static double score(List<Flask> flasks) {
        double total = 0;
        for (Flask flask : flasks) total += score(flask);
        return total;
    }

    public static List<Flask> flasks(MagnumOpus.Stage stage) {
        List<Flask> flasks = new ArrayList<>();
        for (PotionEffectIngredient potion : stage.potions()) {
            Map<PotionEnum, Integer> levels = new TreeMap<>();
            for (PotionEffectIngredient.Requirement requirement : potion.required()) {
                PotionEnum recipe = muddykat.alchemia.common.potion.RecipeDiscovery.byEffect(requirement.effect());
                if (recipe != null) levels.put(recipe, requirement.amplifier() + 1);
            }
            flasks.add(new Flask(levels));
        }
        return flasks;
    }
}
