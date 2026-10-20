package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class WorldEffects {

    private static final String BIOME_PREFIX = "biome:";
    private static final String ABOVE_PREFIX = "above:";
    private static final String BELOW_PREFIX = "below:";
    private static final String MOONLIGHT_PREFIX = "moonlight:";
    private static final char LEVEL_MARK = '@';
    private static final String HINT_PREFIX = "alchemia.world_effect.";
    private static final String GUIDE_PREFIX = "alchemia.guide.world_effect.";
    private static final String NOUN_PREFIX = "alchemia.world_effect.noun.";

    public enum Rule {
        RAIN,
        THUNDER,
        CLEAR,
        DAY,
        NIGHT,
        FULL_MOON,
        NEW_MOON,
        MOONLIGHT,
        ABOVE,
        BELOW,
        BIOME
    }

    public record Imbued(List<Holder<Biome>> biomes, boolean high, boolean low) {
        public static final Imbued NONE = new Imbued(List.of(), false, false);
    }

    public record Condition(Rule rule, List<ResourceKey<Biome>> biomes, List<TagKey<Biome>> tags, double value) {

        static Condition of(Rule rule) {
            return new Condition(rule, List.of(), List.of(), 0.0);
        }

        static Condition of(Rule rule, double value) {
            return new Condition(rule, List.of(), List.of(), value);
        }

        public boolean holds(Level level, BlockPos pos) {
            return holds(level, pos, Imbued.NONE);
        }

        public boolean holds(Level level, BlockPos pos, Imbued attuned) {
            return switch (rule) {
                case RAIN -> level.isRaining();
                case THUNDER -> level.isThundering();
                case CLEAR -> !level.isRaining();
                case DAY -> level.isBrightOutside();
                case NIGHT -> level.isDarkOutside();
                case FULL_MOON -> level.isDarkOutside() && moon(level, pos) == MoonPhase.FULL_MOON;
                case NEW_MOON -> level.isDarkOutside() && moon(level, pos) == MoonPhase.NEW_MOON;
                case MOONLIGHT -> level.isDarkOutside()
                        && DimensionType.MOON_BRIGHTNESS_PER_PHASE[moon(level, pos).index()] >= value - 1.0E-4;
                case ABOVE -> pos.getY() >= value || attuned.high();
                case BELOW -> pos.getY() <= value || attuned.low();
                case BIOME -> inBiome(level.getBiome(pos)) || attuned.biomes().stream().anyMatch(this::inBiome);
            };
        }

        private boolean inBiome(Holder<Biome> biome) {
            for (TagKey<Biome> tag : tags) {
                if (biome.is(tag)) return true;
            }
            for (ResourceKey<Biome> key : biomes) {
                if (biome.is(key)) return true;
            }
            return false;
        }

        public Component guide() {
            return phrase(GUIDE_PREFIX);
        }

        public String noun() {
            return switch (rule) {
                case FULL_MOON, NEW_MOON, MOONLIGHT -> "moon";
                default -> rule.name().toLowerCase(Locale.ROOT);
            };
        }

        public Component hint() {
            return phrase(HINT_PREFIX);
        }

        private Component phrase(String prefix) {
            if (rule == Rule.ABOVE || rule == Rule.BELOW) {
                return Component.translatable(prefix + rule.name().toLowerCase(Locale.ROOT), (int) value);
            }
            if (rule == Rule.MOONLIGHT) {
                if (value >= 1.0) return Component.translatable(prefix + "full_moon");
                if (Math.abs(value - 0.5) < 1.0E-4) return Component.translatable(prefix + "half_moon");
                return Component.translatable(prefix + "moonlight", (int) Math.round(value * 100));
            }
            if (rule != Rule.BIOME) return Component.translatable(prefix + rule.name().toLowerCase(Locale.ROOT));

            List<Component> names = new ArrayList<>();
            for (TagKey<Biome> tag : tags) names.add(tagName(tag.location()));
            for (ResourceKey<Biome> key : biomes) {
                names.add(Component.translatable("biome." + key.identifier().getNamespace() + "." + key.identifier().getPath()));
            }

            Component joined = names.isEmpty() ? Component.literal("?") : names.get(0);
            for (int index = 1; index < names.size(); index++) {
                joined = Component.translatable("alchemia.world_effect.either", joined, names.get(index));
            }
            return Component.translatable(prefix + "biome", joined);
        }
    }

    public record Requirement(List<Condition> conditions) {

        public boolean holds(Level level, BlockPos pos) {
            return holds(level, pos, Imbued.NONE);
        }

        public boolean holds(Level level, BlockPos pos, Imbued attuned) {
            for (Condition condition : conditions) {
                if (!condition.holds(level, pos, attuned)) return false;
            }
            return true;
        }

        public Component hint() {
            Component joined = conditions.get(0).hint();
            for (int index = 1; index < conditions.size(); index++) {
                joined = Component.translatable("alchemia.world_effect.and", joined, conditions.get(index).hint());
            }
            return joined;
        }

        public Component guide() {
            Component joined = conditions.get(0).guide();
            for (int index = 1; index < conditions.size(); index++) {
                joined = Component.translatable(GUIDE_PREFIX + "and", joined, conditions.get(index).guide());
            }
            return joined;
        }
    }

    public record Rules(Requirement wake, TreeMap<Integer, Requirement> levels) {

        public int cap(Level level, BlockPos pos, Imbued attuned) {
            for (Map.Entry<Integer, Requirement> tier : levels.entrySet()) {
                if (!tier.getValue().holds(level, pos, attuned)) return tier.getKey() - 1;
            }
            return Integer.MAX_VALUE;
        }
    }

    private static List<? extends String> parsedFrom;
    private static int parsedGeneration = -1;
    private static Map<AlchemiaEffect, Rules> rules = Map.of();

    private WorldEffects() {
    }

    private static MoonPhase moon(Level level, BlockPos pos) {
        return level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, Vec3.atCenterOf(pos));
    }

    private static Component tagName(Identifier id) {
        String path = id.getPath();
        String plain = (path.startsWith("is_") ? path.substring(3) : path).replace('_', ' ').replace('/', ' ');
        return Component.translatableWithFallback("tag.worldgen.biome." + id.getNamespace() + "." + path.replace('/', '.'), plain);
    }

    private static Map<AlchemiaEffect, Rules> rules() {
        List<? extends String> lines = Configuration.worldEffects();
        int generation = AlchemiaEffects.generation();
        if (lines != parsedFrom || generation != parsedGeneration) {
            rules = parse(lines);
            parsedFrom = lines;
            parsedGeneration = generation;
        }
        return rules;
    }

    private static @Nullable AlchemiaEffect effectNamed(String name) {
        if (name.indexOf(':') >= 0) return AlchemiaEffects.byEffectId(Identifier.tryParse(name));
        return AlchemiaEffects.byId(name);
    }

    private static Condition condition(String text) {
        String lowered = text.toLowerCase(Locale.ROOT);
        if (lowered.startsWith(ABOVE_PREFIX)) return numbered(Rule.ABOVE, text.substring(ABOVE_PREFIX.length()));
        if (lowered.startsWith(BELOW_PREFIX)) return numbered(Rule.BELOW, text.substring(BELOW_PREFIX.length()));
        if (lowered.startsWith(MOONLIGHT_PREFIX)) return numbered(Rule.MOONLIGHT, text.substring(MOONLIGHT_PREFIX.length()));

        if (!lowered.startsWith(BIOME_PREFIX)) {
            try {
                Rule rule = Rule.valueOf(lowered.toUpperCase(Locale.ROOT));
                return switch (rule) {
                    case BIOME, ABOVE, BELOW, MOONLIGHT -> null;
                    default -> Condition.of(rule);
                };
            } catch (IllegalArgumentException error) {
                return null;
            }
        }

        List<ResourceKey<Biome>> biomes = new ArrayList<>();
        List<TagKey<Biome>> tags = new ArrayList<>();
        for (String entry : text.substring(BIOME_PREFIX.length()).split("\\|")) {
            String name = entry.trim();
            boolean tag = name.startsWith("#");
            Identifier id = Identifier.tryParse(tag ? name.substring(1) : name);
            if (id == null) return null;

            if (tag) {
                tags.add(TagKey.create(Registries.BIOME, id));
            } else {
                biomes.add(ResourceKey.create(Registries.BIOME, id));
            }
        }
        return biomes.isEmpty() && tags.isEmpty() ? null : new Condition(Rule.BIOME, List.copyOf(biomes), List.copyOf(tags), 0.0);
    }

    private static Condition numbered(Rule rule, String number) {
        try {
            return Condition.of(rule, Double.parseDouble(number.trim()));
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private static Map<AlchemiaEffect, Rules> parse(List<? extends String> lines) {
        Map<AlchemiaEffect, Requirement> wakes = new HashMap<>();
        Map<AlchemiaEffect, TreeMap<Integer, Requirement>> tiers = new HashMap<>();

        for (String line : lines) {
            int split = line.indexOf('=');
            if (split < 0) {
                if (!line.isBlank()) Alchemia.LOGGER.warn("Ignoring world effect '{}': expected 'Effect = condition'", line);
                continue;
            }

            String key = line.substring(0, split).trim();
            int tier = 1;
            int mark = key.lastIndexOf(LEVEL_MARK);
            if (mark >= 0) {
                try {
                    tier = Integer.parseInt(key.substring(mark + 1).trim());
                } catch (NumberFormatException error) {
                    Alchemia.LOGGER.warn("Ignoring world effect '{}': level after '@' must be a number", line);
                    continue;
                }
                key = key.substring(0, mark).trim();
            }

            AlchemiaEffect recipe = effectNamed(key);
            if (recipe == null) {
                Alchemia.LOGGER.warn("Ignoring world effect '{}': unknown effect", line);
                continue;
            }

            List<Condition> conditions = new ArrayList<>();
            for (String part : line.substring(split + 1).split(",")) {
                if (part.isBlank()) continue;

                Condition condition = condition(part.trim());
                if (condition == null) {
                    Alchemia.LOGGER.warn("Ignoring condition '{}' for world effect {}", part.trim(), recipe.id());
                    continue;
                }
                conditions.add(condition);
            }
            if (conditions.isEmpty()) continue;

            Requirement requirement = new Requirement(List.copyOf(conditions));
            if (tier <= 1) {
                wakes.put(recipe, requirement);
            } else {
                tiers.computeIfAbsent(recipe, ignored -> new TreeMap<>()).put(tier, requirement);
            }
        }

        Map<AlchemiaEffect, Rules> parsed = new LinkedHashMap<>();
        for (AlchemiaEffect recipe : AlchemiaEffects.values()) {
            Requirement wake = wakes.get(recipe);
            TreeMap<Integer, Requirement> levels = tiers.getOrDefault(recipe, new TreeMap<>());
            if (wake != null || !levels.isEmpty()) parsed.put(recipe, new Rules(wake, levels));
        }
        return parsed;
    }

    public static Rules rules(AlchemiaEffect recipe) {
        return recipe == null ? null : rules().get(recipe);
    }

    public static boolean conditional(AlchemiaEffect recipe) {
        return rules(recipe) != null;
    }

    public static Requirement requirement(AlchemiaEffect recipe) {
        Rules found = rules(recipe);
        return found == null ? null : found.wake();
    }

    public static boolean dormant(AlchemiaEffect recipe, Level level, BlockPos pos) {
        return dormant(recipe, level, pos, Imbued.NONE);
    }

    public static boolean dormant(AlchemiaEffect recipe, Level level, BlockPos pos, Imbued attuned) {
        Requirement requirement = requirement(recipe);
        return requirement != null && level != null && !requirement.holds(level, pos, attuned);
    }

    public static int levelCap(AlchemiaEffect recipe, Level level, BlockPos pos) {
        return levelCap(recipe, level, pos, Imbued.NONE);
    }

    public static int levelCap(AlchemiaEffect recipe, Level level, BlockPos pos, Imbued attuned) {
        Rules found = rules(recipe);
        return found == null || level == null ? Integer.MAX_VALUE : found.cap(level, pos, attuned);
    }

    private static final List<Rule> SHIFT_PRIORITY = List.of(Rule.FULL_MOON, Rule.NEW_MOON, Rule.MOONLIGHT,
            Rule.THUNDER, Rule.RAIN, Rule.CLEAR, Rule.NIGHT, Rule.DAY);

    public static Set<Condition> holding(AlchemiaEffect recipe, Level level, BlockPos pos) {
        return holding(recipe, level, pos, Imbued.NONE);
    }

    public static Set<Condition> holding(AlchemiaEffect recipe, Level level, BlockPos pos, Imbued attuned) {
        Set<Condition> holding = new HashSet<>();
        Rules found = rules(recipe);
        if (found == null || level == null) return holding;

        List<Requirement> requirements = new ArrayList<>(found.levels().values());
        if (found.wake() != null) requirements.add(found.wake());
        for (Requirement requirement : requirements) {
            for (Condition condition : requirement.conditions()) {
                if (condition.holds(level, pos, attuned)) holding.add(condition);
            }
        }
        return holding;
    }

    public static @Nullable Rule shifted(Set<Condition> before, Set<Condition> after) {
        Set<Rule> flipped = EnumSet.noneOf(Rule.class);
        for (Condition condition : before) {
            if (!after.contains(condition)) flipped.add(condition.rule());
        }
        for (Condition condition : after) {
            if (!before.contains(condition)) flipped.add(condition.rule());
        }

        for (Rule rule : SHIFT_PRIORITY) {
            if (flipped.contains(rule)) return rule;
        }
        return flipped.isEmpty() ? null : flipped.iterator().next();
    }

    public static Component levelHint(AlchemiaEffect recipe, int tier) {
        Rules found = rules(recipe);
        Requirement requirement = found == null ? null : found.levels().get(tier);
        if (requirement == null) return null;
        return Component.translatable("alchemia.world_effect.level", Component.translatable("enchantment.level." + tier),
                requirement.hint());
    }

    public static List<Component> levelHints(AlchemiaEffect recipe) {
        Rules found = rules(recipe);
        List<Component> hints = new ArrayList<>();
        if (found == null) return hints;

        for (int tier : found.levels().keySet()) hints.add(levelHint(recipe, tier));
        return hints;
    }

    public static Set<String> notesIn(ItemStack guide) {
        return new HashSet<>(guide.getOrDefault(DataComponentRegistry.NOTED_EFFECTS.get(), List.<String>of()));
    }

    public static Set<String> notesOf(Player player) {
        Set<String> notes = new HashSet<>();
        if (player == null) return notes;

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof ItemAlchemiaGuide) notes.addAll(notesIn(stack));
        }
        return notes;
    }

    private static String tierMark(AlchemiaEffect recipe, int tier) {
        return recipe.id() + LEVEL_MARK + tier;
    }

    public static boolean wakeVisible(Set<String> notes, AlchemiaEffect recipe) {
        return recipe != null && requirement(recipe) != null && notes.contains(recipe.id());
    }

    public static boolean tierVisible(Set<String> notes, AlchemiaEffect recipe, int tier) {
        return recipe != null && notes.contains(tierMark(recipe, tier));
    }

    public static boolean anyVisible(Set<String> notes, AlchemiaEffect recipe) {
        if (wakeVisible(notes, recipe)) return true;

        Rules found = rules(recipe);
        if (found == null) return false;
        for (int tier : found.levels().keySet()) {
            if (tierVisible(notes, recipe, tier)) return true;
        }
        return false;
    }

    public static List<Component> guideLines(Set<String> notes, AlchemiaEffect recipe) {
        List<Component> lines = new ArrayList<>();
        Rules found = rules(recipe);
        if (found == null) return lines;

        if (wakeVisible(notes, recipe)) {
            lines.add(Component.translatable(GUIDE_PREFIX + "wake", found.wake().guide()));
        }
        for (Map.Entry<Integer, Requirement> tier : found.levels().entrySet()) {
            if (!tierVisible(notes, recipe, tier.getKey())) continue;
            lines.add(Component.translatable(GUIDE_PREFIX + "tier",
                    Component.translatable("enchantment.level." + tier.getKey()), tier.getValue().guide()));
        }
        return lines;
    }

    public static @Nullable Component newsOf(Set<String> before, Set<String> after) {
        Set<String> nouns = new java.util.LinkedHashSet<>();
        for (String mark : after) {
            if (before.contains(mark)) continue;

            Requirement requirement = requirementFor(mark);
            if (requirement == null) continue;
            for (Condition condition : requirement.conditions()) nouns.add(condition.noun());
        }
        if (nouns.isEmpty()) return null;

        Component joined = null;
        for (String noun : nouns) {
            Component part = Component.translatable(NOUN_PREFIX + noun);
            joined = joined == null ? part : Component.translatable(GUIDE_PREFIX + "and", joined, part);
        }
        return joined;
    }

    public static Set<String> allMarks() {
        Set<String> marks = new HashSet<>();
        for (Map.Entry<AlchemiaEffect, Rules> entry : rules().entrySet()) {
            if (entry.getValue().wake() != null) marks.add(entry.getKey().id());
            for (int tier : entry.getValue().levels().keySet()) marks.add(tierMark(entry.getKey(), tier));
        }
        return marks;
    }

    public static @Nullable AlchemiaEffect recipeOfMark(String mark) {
        int split = mark.lastIndexOf(LEVEL_MARK);
        return requirementFor(mark) == null ? null : RecipeDiscovery.byName(split < 0 ? mark : mark.substring(0, split));
    }

    private static @Nullable Requirement requirementFor(String mark) {
        int split = mark.lastIndexOf(LEVEL_MARK);
        AlchemiaEffect recipe = RecipeDiscovery.byName(split < 0 ? mark : mark.substring(0, split));
        Rules found = rules(recipe);
        if (found == null) return null;
        if (split < 0) return found.wake();

        try {
            return found.levels().get(Integer.parseInt(mark.substring(split + 1)));
        } catch (NumberFormatException error) {
            return null;
        }
    }

    public static boolean note(Player player, AlchemiaEffect recipe) {
        return player != null && note(RecipeDiscovery.guidesOf(player), recipe);
    }

    public static boolean note(Iterable<ItemStack> guides, AlchemiaEffect recipe) {
        if (requirement(recipe) == null) return false;
        return addNotes(guides, List.of(recipe.id()));
    }

    public static boolean reveal(Player player, AlchemiaEffect recipe, int brewedLevel) {
        return player != null && reveal(RecipeDiscovery.guidesOf(player), recipe, brewedLevel);
    }

    public static boolean reveal(Iterable<ItemStack> guides, AlchemiaEffect recipe, int brewedLevel) {
        Rules found = rules(recipe);
        if (found == null) return false;

        List<String> marks = new ArrayList<>();
        for (int tier : found.levels().keySet()) {
            if (tier <= brewedLevel + 1) marks.add(tierMark(recipe, tier));
        }
        return addNotes(guides, marks);
    }

    public static boolean learnNotes(ItemStack guide, Collection<String> marks) {
        List<String> valid = new ArrayList<>();
        for (String mark : marks) {
            if (requirementFor(mark) != null) valid.add(mark);
        }
        return addNotes(List.of(guide), valid);
    }

    private static boolean addNotes(Iterable<ItemStack> guides, List<String> marks) {
        if (marks.isEmpty()) return false;

        boolean changed = false;
        for (ItemStack stack : guides) {
            List<String> names = new ArrayList<>(stack.getOrDefault(DataComponentRegistry.NOTED_EFFECTS.get(), List.<String>of()));
            boolean grew = false;
            for (String mark : marks) {
                if (names.contains(mark)) continue;
                names.add(mark);
                grew = true;
            }
            if (!grew) continue;

            names.sort(null);
            stack.set(DataComponentRegistry.NOTED_EFFECTS.get(), List.copyOf(names));
            changed = true;
        }
        return changed;
    }
}
