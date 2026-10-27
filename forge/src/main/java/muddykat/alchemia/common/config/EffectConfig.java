package muddykat.alchemia.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EffectConfig {

    public static final String FILE_NAME = "alchemia-effects.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String[] HEADER = {
            "Effects that can be brewed in the alchemical cauldron.",
            "Every entry is placed on each brew base's potion map, gets a page in the guide, and is announced with a toast",
            "when it is first brewed. Remove an entry to take an effect out of the game, or add one to brew any registered",
            "mob effect, including effects added by other mods. Entries whose mob effect does not exist are skipped, so one",
            "file can be shared across Minecraft versions and mod packs.",
            "",
            "id: name the effect is saved under in guides, recipes and the other Alchemia configs. Letters, digits and",
            "  underscores only. Renaming an id makes every guide forget it.",
            "effect: mob effect id that is brewed, e.g. \"minecraft:speed\". Each mob effect may appear once.",
            "max_level: highest level the potion map offers, 1 to " + AlchemiaEffect.MAX_LEVEL + ". Defaults to " + AlchemiaEffect.DEFAULT_LEVEL + ".",
            "rarity: common, uncommon, rare or epic. Rarer effects sit further from the centre of the map.",
            "primary, secondary: the elements the effect leans towards, fire, water, earth, air or void.",
            "  Together they pick the side of the map it settles on. secondary defaults to void.",
            "duration: seconds the brewed effect lasts before modifiers. Defaults to " + AlchemiaEffect.DEFAULT_DURATION + ".",
            "icon: optional GUI sprite drawn for the effect, e.g. \"minecraft:mob_effect/speed\". Defaults to the effect's own icon.",
            "description: optional translation key or plain text shown on the effect's guide page once it is known.",
            "bases: optional brew base ids that can reach this effect even if their \"effects\" list in",
            "  " + BrewBaseConfig.FILE_NAME + " leaves it out. Bases without an \"effects\" list reach every effect.",
            "",
            "Conditions that hold an effect dormant are set by id under [world_effects] in alchemia-server.toml."
    };

    private EffectConfig() {
    }

    public static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    public static void load() {
        Path file = path();
        if (!Files.exists(file)) {
            List<AlchemiaEffect> defaults = AlchemiaEffects.present(AlchemiaEffects.defaults());
            AlchemiaEffects.install(defaults);
            write(file, defaults);
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement entries = root.isJsonObject() ? root.getAsJsonObject().get("effects") : root;
            if (entries == null || !entries.isJsonArray()) {
                Alchemia.LOGGER.error("{} has no \"effects\" list, using the built in effects", FILE_NAME);
                AlchemiaEffects.install(AlchemiaEffects.present(AlchemiaEffects.defaults()));
                return;
            }

            int version = root.isJsonObject() && root.getAsJsonObject().has("version")
                    ? root.getAsJsonObject().get("version").getAsInt() : 1;
            List<AlchemiaEffect> parsed = new ArrayList<>();
            boolean clean = true;
            for (JsonElement entry : entries.getAsJsonArray()) {
                DataResult<AlchemiaEffect> result = AlchemiaEffect.CODEC.parse(JsonOps.INSTANCE, entry);
                if (result.result().isPresent()) {
                    parsed.add(result.result().get());
                } else {
                    clean = false;
                    Alchemia.LOGGER.error("Skipping an effect in {}: {}", FILE_NAME,
                            result.error().map(error -> error.message()).orElse("unknown problem"));
                }
            }

            boolean grew = false;
            if (version < AlchemiaEffects.DEFAULTS_VERSION) {
                for (AlchemiaEffect effect : AlchemiaEffects.introducedAfter(version)) {
                    if (!effect.resolved() || parsed.stream().anyMatch(known -> known.id().equalsIgnoreCase(effect.id())
                            || known.effectId().equals(effect.effectId()))) continue;
                    parsed.add(effect);
                    grew = true;
                    Alchemia.LOGGER.info("Added the new {} effect to {}", effect.id(), FILE_NAME);
                }
            }
            AlchemiaEffects.install(parsed);
            if (version < AlchemiaEffects.DEFAULTS_VERSION && clean) {
                write(file, parsed);
            } else if (grew) {
                Alchemia.LOGGER.warn("{} has entries that could not be read, so the new effects were added for this run only", FILE_NAME);
            }
        } catch (IOException | RuntimeException error) {
            Alchemia.LOGGER.error("Could not read {}, using the built in effects", FILE_NAME, error);
            AlchemiaEffects.install(AlchemiaEffects.present(AlchemiaEffects.defaults()));
        }
    }

    private static void write(Path file, List<AlchemiaEffect> effects) {
        AlchemiaEffect.LIST_CODEC.encodeStart(JsonOps.INSTANCE, effects)
                .resultOrPartial(error -> Alchemia.LOGGER.error("Could not build {}: {}", FILE_NAME, error))
                .ifPresent(encoded -> {
                    JsonObject root = new JsonObject();
                    JsonArray header = new JsonArray();
                    for (String line : HEADER) header.add(line);
                    root.add("__comment", header);
                    root.addProperty("version", AlchemiaEffects.DEFAULTS_VERSION);
                    root.add("effects", encoded);

                    try {
                        Files.createDirectories(file.getParent());
                        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                            GSON.toJson(root, writer);
                        }
                    } catch (IOException error) {
                        Alchemia.LOGGER.error("Could not write {}", FILE_NAME, error);
                    }
                });
    }
}
