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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
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

    public record Condition(Rule rule, List<ResourceKey<Biome>> biomes, List<TagKey<Biome>> tags, double value) {

        static Condition of(Rule rule) {
            return new Condition(rule, List.of(), List.of(), 0.0);
        }

        static Condition of(Rule rule, double value) {
            return new Condition(rule, List.of(), List.of(), value);
        }

        public boolean holds(Level level, BlockPos pos) {
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
                case ABOVE -> pos.getY() >= value;
                case BELOW -> pos.getY() <= value;
                case BIOME -> inBiome(level, pos);
            };
        }

        private boolean inBiome(Level level, BlockPos pos) {
            Holder<Biome> biome = level.getBiome(pos);
            for (TagKey<Biome> tag : tags) {
                if (biome.is(tag)) return true;
            }
            for (ResourceKey<Biome> key : biomes) {
                if (biome.is(key)) return true;
            }
            return false;
        }

        public Component hint() {
            if (rule == Rule.ABOVE || rule == Rule.BELOW) {
                return Component.translatable("alchemia.world_effect." + rule.name().toLowerCase(Locale.ROOT), (int) value);
            }
            if (rule == Rule.MOONLIGHT) {
                if (value >= 1.0) return Component.translatable("alchemia.world_effect.full_moon");
                if (Math.abs(value - 0.5) < 1.0E-4) return Component.translatable("alchemia.world_effect.half_moon");
                return Component.translatable("alchemia.world_effect.moonlight", (int) Math.round(value * 100));
            }
            if (rule != Rule.BIOME) return Component.translatable("alchemia.world_effect." + rule.name().toLowerCase(Locale.ROOT));

            List<Component> names = new ArrayList<>();
            for (TagKey<Biome> tag : tags) names.add(tagName(tag.location()));
            for (ResourceKey<Biome> key : biomes) {
                names.add(Component.translatable("biome." + key.identifier().getNamespace() + "." + key.identifier().getPath()));
            }

            Component joined = names.isEmpty() ? Component.literal("?") : names.get(0);
            for (int index = 1; index < names.size(); index++) {
                joined = Component.translatable("alchemia.world_effect.either", joined, names.get(index));
            }
            return Component.translatable("alchemia.world_effect.biome", joined);
        }
    }

    public record Requirement(List<Condition> conditions) {

        public boolean holds(Level level, BlockPos pos) {
            for (Condition condition : conditions) {
                if (!condition.holds(level, pos)) return false;
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
    }

    public record Rules(Requirement wake, TreeMap<Integer, Requirement> levels) {

        public int cap(Level level, BlockPos pos) {
            for (Map.Entry<Integer, Requirement> tier : levels.entrySet()) {
                if (!tier.getValue().holds(level, pos)) return tier.getKey() - 1;
            }
            return Integer.MAX_VALUE;
        }
    }

    private static List<? extends String> parsedFrom;
    private static Map<PotionEnum, Rules> rules = Map.of();

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

    private static Map<PotionEnum, Rules> rules() {
        List<? extends String> lines = Configuration.worldEffects();
        if (lines != parsedFrom) {
            rules = parse(lines);
            parsedFrom = lines;
        }
        return rules;
    }

    private static PotionEnum effectNamed(String name) {
        if (name.indexOf(':') >= 0) {
            Identifier id = Identifier.tryParse(name);
            for (PotionEnum recipe : PotionEnum.values()) {
                if (recipe.getEffect().unwrapKey().map(key -> key.identifier().equals(id)).orElse(false)) return recipe;
            }
            return null;
        }

        for (PotionEnum recipe : PotionEnum.values()) {
            if (recipe.name().equalsIgnoreCase(name)) return recipe;
        }
        return null;
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

    private static Map<PotionEnum, Rules> parse(List<? extends String> lines) {
        Map<PotionEnum, Requirement> wakes = new EnumMap<>(PotionEnum.class);
        Map<PotionEnum, TreeMap<Integer, Requirement>> tiers = new EnumMap<>(PotionEnum.class);

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

            PotionEnum recipe = effectNamed(key);
            if (recipe == null) {
                Alchemia.LOGGER.warn("Ignoring world effect '{}': unknown effect", line);
                continue;
            }

            List<Condition> conditions = new ArrayList<>();
            for (String part : line.substring(split + 1).split(",")) {
                if (part.isBlank()) continue;

                Condition condition = condition(part.trim());
                if (condition == null) {
                    Alchemia.LOGGER.warn("Ignoring condition '{}' for world effect {}", part.trim(), recipe.name());
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

        Map<PotionEnum, Rules> parsed = new EnumMap<>(PotionEnum.class);
        for (PotionEnum recipe : PotionEnum.values()) {
            Requirement wake = wakes.get(recipe);
            TreeMap<Integer, Requirement> levels = tiers.getOrDefault(recipe, new TreeMap<>());
            if (wake != null || !levels.isEmpty()) parsed.put(recipe, new Rules(wake, levels));
        }
        return parsed;
    }

    public static Rules rules(PotionEnum recipe) {
        return recipe == null ? null : rules().get(recipe);
    }

    public static boolean conditional(PotionEnum recipe) {
        return rules(recipe) != null;
    }

    public static Requirement requirement(PotionEnum recipe) {
        Rules found = rules(recipe);
        return found == null ? null : found.wake();
    }

    public static boolean dormant(PotionEnum recipe, Level level, BlockPos pos) {
        Requirement requirement = requirement(recipe);
        return requirement != null && level != null && !requirement.holds(level, pos);
    }

    public static int levelCap(PotionEnum recipe, Level level, BlockPos pos) {
        Rules found = rules(recipe);
        return found == null || level == null ? Integer.MAX_VALUE : found.cap(level, pos);
    }

    public static Component levelHint(PotionEnum recipe, int tier) {
        Rules found = rules(recipe);
        Requirement requirement = found == null ? null : found.levels().get(tier);
        if (requirement == null) return null;
        return Component.translatable("alchemia.world_effect.level", Component.translatable("enchantment.level." + tier),
                requirement.hint());
    }

    public static List<Component> levelHints(PotionEnum recipe) {
        Rules found = rules(recipe);
        List<Component> hints = new ArrayList<>();
        if (found == null) return hints;

        for (int tier : found.levels().keySet()) hints.add(levelHint(recipe, tier));
        return hints;
    }

    public static Set<PotionEnum> notedIn(ItemStack guide) {
        Set<PotionEnum> noted = EnumSet.noneOf(PotionEnum.class);
        for (String name : guide.getOrDefault(DataComponentRegistry.NOTED_EFFECTS.get(), List.<String>of())) {
            PotionEnum recipe = RecipeDiscovery.byName(name);
            if (recipe != null) noted.add(recipe);
        }
        return noted;
    }

    public static Set<PotionEnum> notedBy(Player player) {
        Set<PotionEnum> noted = EnumSet.noneOf(PotionEnum.class);
        if (player == null) return noted;

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof ItemAlchemiaGuide) noted.addAll(notedIn(stack));
        }
        return noted;
    }

    public static boolean note(Player player, PotionEnum recipe) {
        if (player == null || !conditional(recipe)) return false;

        boolean changed = false;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof ItemAlchemiaGuide)) continue;

            List<String> names = new ArrayList<>(stack.getOrDefault(DataComponentRegistry.NOTED_EFFECTS.get(), List.<String>of()));
            if (names.contains(recipe.name())) continue;

            names.add(recipe.name());
            names.sort(null);
            stack.set(DataComponentRegistry.NOTED_EFFECTS.get(), List.copyOf(names));
            changed = true;
        }
        return changed;
    }
}
