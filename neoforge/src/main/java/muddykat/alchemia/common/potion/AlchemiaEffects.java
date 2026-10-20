package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Rarity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static muddykat.alchemia.common.items.helper.IngredientAlignment.Air;
import static muddykat.alchemia.common.items.helper.IngredientAlignment.Earth;
import static muddykat.alchemia.common.items.helper.IngredientAlignment.Fire;
import static muddykat.alchemia.common.items.helper.IngredientAlignment.Void;
import static muddykat.alchemia.common.items.helper.IngredientAlignment.Water;
import static net.minecraft.world.item.Rarity.COMMON;
import static net.minecraft.world.item.Rarity.EPIC;
import static net.minecraft.world.item.Rarity.RARE;
import static net.minecraft.world.item.Rarity.UNCOMMON;

public final class AlchemiaEffects {

    public static final Comparator<AlchemiaEffect> ORDER = Comparator.comparingInt(AlchemiaEffects::indexOf);

    public static final int DEFAULTS_VERSION = 2;

    private static volatile List<AlchemiaEffect> EFFECTS = List.of();
    private static volatile Map<String, AlchemiaEffect> BY_ID = Map.of();
    private static volatile Map<Identifier, AlchemiaEffect> BY_EFFECT = Map.of();
    private static volatile Map<AlchemiaEffect, Integer> INDEX = Map.of();
    private static volatile int generation;

    private AlchemiaEffects() {
    }

    static {
        install(defaults());
    }

    public static List<AlchemiaEffect> defaults() {
        List<AlchemiaEffect> defaults = new ArrayList<>(vanilla());
        defaults.addAll(alchemical());
        return defaults;
    }

    public static List<AlchemiaEffect> introducedAfter(int version) {
        return version < 2 ? alchemical() : List.of();
    }

    private static List<AlchemiaEffect> alchemical() {
        return List.of(
                alchemia("Returning", "returning", Water, Earth, EPIC, 3, 1, "water"),
                alchemia("Teleportation", "teleportation", Air, Water, EPIC, 3, 1, "honey"),
                alchemia("Attraction", "attraction", Earth, Water, UNCOMMON, 3, 180, "honey"),
                alchemia("Treasure_Vision", "treasure_vision", Earth, Fire, RARE, 3, 90, "water"),
                alchemia("Dangersense", "dangersense", Fire, Void, UNCOMMON, 2, 120, "water"),
                alchemia("Flames", "flames", Fire, Earth, UNCOMMON, 3, 1, "honey"),
                alchemia("Thorns", "thorns", Earth, Void, RARE, 4, 120, "honey"));
    }

    private static AlchemiaEffect alchemia(String id, String path, IngredientAlignment primary, IngredientAlignment secondary,
                                           Rarity rarity, int maxLevel, int duration, String base) {
        return new AlchemiaEffect(id, Identifier.fromNamespaceAndPath(Alchemia.MODID, path), maxLevel, rarity, primary, secondary,
                duration, Optional.empty(), Optional.of("alchemia.effect." + path + ".description"), List.of(base));
    }

    private static List<AlchemiaEffect> vanilla() {
        return List.of(
                AlchemiaEffect.builtIn("Healing", "instant_health", Water, Air, RARE),
                AlchemiaEffect.builtIn("Harm", "instant_damage", Fire, Void, RARE),
                AlchemiaEffect.builtIn("Fire_Protection", "fire_resistance", Water, Void, UNCOMMON),
                AlchemiaEffect.builtIn("Regeneration", "regeneration", Earth, Water, UNCOMMON),
                AlchemiaEffect.builtIn("Luck", "luck", Air, Void, EPIC),
                AlchemiaEffect.builtIn("Speed", "speed", Air, Fire, COMMON),
                AlchemiaEffect.builtIn("Slow", "slowness", Earth, Void, COMMON),
                AlchemiaEffect.builtIn("Dig_Speed", "haste", Fire, Earth, UNCOMMON),
                AlchemiaEffect.builtIn("Dig_Slowdown", "mining_fatigue", Water, Void, UNCOMMON),
                AlchemiaEffect.builtIn("Damage_Boost", "strength", Fire, Air, RARE),
                AlchemiaEffect.builtIn("Jump", "jump_boost", Fire, Void, EPIC),
                AlchemiaEffect.builtIn("Confusion", "nausea", Void, Water, EPIC),
                AlchemiaEffect.builtIn("Damage_Resistance", "resistance", Earth, Void, EPIC),
                AlchemiaEffect.builtIn("Water_Breathing", "water_breathing", Air, Water, RARE),
                AlchemiaEffect.builtIn("Invisibility", "invisibility", Void, Air, EPIC),
                AlchemiaEffect.builtIn("Blindness", "blindness", Void, Earth, UNCOMMON),
                AlchemiaEffect.builtIn("Night_Vision", "night_vision", Air, Fire, RARE),
                AlchemiaEffect.builtIn("Hunger", "hunger", Void, Air, COMMON),
                AlchemiaEffect.builtIn("Weakness", "weakness", Fire, Earth, UNCOMMON),
                AlchemiaEffect.builtIn("Poison", "poison", Fire, Water, COMMON),
                AlchemiaEffect.builtIn("Wither", "wither", Void, Fire, EPIC),
                AlchemiaEffect.builtIn("Health_Boost", "health_boost", Earth, Water, EPIC),
                AlchemiaEffect.builtIn("Absorption", "absorption", Water, Void, RARE),
                AlchemiaEffect.builtIn("Saturation", "saturation", Earth, Void, UNCOMMON),
                AlchemiaEffect.builtIn("Glowing", "glowing", Air, Fire, RARE),
                AlchemiaEffect.builtIn("Levitation", "levitation", Air, Void, UNCOMMON),
                AlchemiaEffect.builtIn("Unluck", "unluck", Earth, Void, EPIC),
                AlchemiaEffect.builtIn("Slow_Falling", "slow_falling", Water, Void, RARE),
                AlchemiaEffect.builtIn("Conduit_Power", "conduit_power", Water, Void, EPIC),
                AlchemiaEffect.builtIn("Dolphins_Grace", "dolphins_grace", Water, Air, RARE),
                AlchemiaEffect.builtIn("Bad_Omen", "bad_omen", Fire, Void, EPIC),
                AlchemiaEffect.builtIn("Wind_Charged", "wind_charged", Air, Void, UNCOMMON),
                AlchemiaEffect.builtIn("Weaving", "weaving", Earth, Void, UNCOMMON),
                AlchemiaEffect.builtIn("Oozing", "oozing", Water, Earth, UNCOMMON),
                AlchemiaEffect.builtIn("Infested", "infested", Earth, Fire, UNCOMMON),
                AlchemiaEffect.builtIn("Darkness", "darkness", Void, Air, RARE),
                AlchemiaEffect.builtIn("Breath_Of_The_Nautilus", "breath_of_the_nautilus", Water, Void, RARE),
                AlchemiaEffect.builtIn("Hero_Of_The_Village", "hero_of_the_village", Earth, Air, EPIC),
                AlchemiaEffect.builtIn("Raid_Omen", "raid_omen", Fire, Earth, EPIC),
                AlchemiaEffect.builtIn("Trial_Omen", "trial_omen", Fire, Air, EPIC));
    }

    public static List<AlchemiaEffect> present(List<AlchemiaEffect> effects) {
        List<AlchemiaEffect> kept = new ArrayList<>();
        for (AlchemiaEffect effect : effects) {
            if (effect.resolved()) kept.add(effect);
        }
        return kept;
    }

    public static synchronized void install(List<AlchemiaEffect> effects) {
        Map<String, AlchemiaEffect> byId = new LinkedHashMap<>();
        Map<Identifier, AlchemiaEffect> byEffect = new HashMap<>();
        for (AlchemiaEffect effect : effects) {
            String key = effect.id().toLowerCase(Locale.ROOT);
            if (!effect.resolved()) {
                Alchemia.LOGGER.warn("Ignoring alchemia effect '{}': no mob effect named {}", effect.id(), effect.effectId());
                continue;
            }
            if (byId.containsKey(key)) {
                Alchemia.LOGGER.warn("Ignoring duplicate alchemia effect '{}'", effect.id());
                continue;
            }
            AlchemiaEffect sharing = byEffect.get(effect.effectId());
            if (sharing != null) {
                Alchemia.LOGGER.warn("Ignoring alchemia effect '{}': {} is already brewed as '{}'",
                        effect.id(), effect.effectId(), sharing.id());
                continue;
            }
            byId.put(key, effect);
            byEffect.put(effect.effectId(), effect);
        }

        if (byId.isEmpty()) {
            Alchemia.LOGGER.warn("No usable alchemia effects were defined, falling back to the built in ones");
            for (AlchemiaEffect effect : present(defaults())) {
                if (byEffect.containsKey(effect.effectId())) continue;
                byId.put(effect.id().toLowerCase(Locale.ROOT), effect);
                byEffect.put(effect.effectId(), effect);
            }
        }

        List<AlchemiaEffect> ordered = List.copyOf(byId.values());
        Map<AlchemiaEffect, Integer> index = new HashMap<>();
        for (int position = 0; position < ordered.size(); position++) index.put(ordered.get(position), position);

        EFFECTS = ordered;
        BY_ID = Collections.unmodifiableMap(byId);
        BY_EFFECT = Collections.unmodifiableMap(byEffect);
        INDEX = Collections.unmodifiableMap(index);
        generation++;
    }

    public static int generation() {
        return generation;
    }

    public static List<AlchemiaEffect> values() {
        return EFFECTS;
    }

    public static List<AlchemiaEffect> definitions() {
        return new ArrayList<>(EFFECTS);
    }

    public static int count() {
        return EFFECTS.size();
    }

    public static int indexOf(AlchemiaEffect effect) {
        return INDEX.getOrDefault(effect, Integer.MAX_VALUE);
    }

    public static @Nullable AlchemiaEffect byId(@Nullable String id) {
        if (id == null) return null;
        return BY_ID.get(id.toLowerCase(Locale.ROOT));
    }

    public static @Nullable AlchemiaEffect byEffect(@Nullable Holder<MobEffect> effect) {
        if (effect == null) return null;
        return effect.unwrapKey().map(key -> BY_EFFECT.get(key.identifier())).orElse(null);
    }

    public static @Nullable AlchemiaEffect byEffectId(@Nullable Identifier id) {
        return id == null ? null : BY_EFFECT.get(id);
    }

    public static List<String> ids() {
        List<String> ids = new ArrayList<>();
        for (AlchemiaEffect effect : EFFECTS) ids.add(effect.id());
        return ids;
    }

    public static Identifier sprite(Holder<MobEffect> effect) {
        AlchemiaEffect known = byEffect(effect);
        if (known != null) return known.sprite();
        return effect.unwrapKey().map(key -> AlchemiaEffect.defaultSprite(key.identifier()))
                .orElseGet(() -> Identifier.withDefaultNamespace("missingno"));
    }
}
