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
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BrewBaseConfig {

    public static final String FILE_NAME = "alchemia-brew-bases.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String[] HEADER = {
            "Brew bases the alchemical cauldron can be filled with.",
            "Every base rolls its own potion map, so effects and dead space sit in different places on each one.",
            "",
            "id: lower case name of the base. Used for saves, recipes and the alchemia.base.<id> translation key.",
            "fills: the sources that pour this base into an empty or matching cauldron. At least one is required.",
            "  item: item id consumed to fill, e.g. \"minecraft:honey_bottle\".",
            "  fluid: fluid id drained from any fluid holding item, e.g. \"minecraft:water\". Use item or fluid, not both.",
            "  levels: how much of the cauldron this source fills, 1 to 4. 4 is a full cauldron.",
            "  remainder: item handed back after an item fill, e.g. \"minecraft:glass_bottle\". Item fills only.",
            "  amount: millibuckets drained per fluid fill. Fluid fills only, defaults to 250 per level.",
            "instability_cap: how many dead space landings this base survives before the brew is ruined.",
            "tint: default cauldron fluid colour as hex, e.g. \"#3F76E4\". Effects override it once brewing starts.",
            "biome_tint: true to tint the fluid with the biome water colour instead of the flat tint.",
            "texture: block atlas sprite drawn in the cauldron, e.g. \"minecraft:block/water_still\".",
            "fluid: fluid this base is made of. Its still texture is used when no texture is set above.",
            "unbrewed_result: item handed out when the base is bottled without any effects. Leave out for a water bottle.",
            "effects: effects this base can reach on its potion map. Leave the field out to allow every effect."
    };

    private BrewBaseConfig() {
    }

    public static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    public static void load() {
        Path file = path();
        if (!Files.exists(file)) {
            List<BrewBase> defaults = BrewBases.defaults();
            BrewBases.install(defaults);
            write(file, defaults);
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement bases = root.isJsonObject() ? root.getAsJsonObject().get("bases") : root;
            if (bases == null || !bases.isJsonArray()) {
                Alchemia.LOGGER.error("{} has no \"bases\" list, using the built in brew bases", FILE_NAME);
                BrewBases.install(BrewBases.defaults());
                return;
            }

            List<BrewBase> parsed = new ArrayList<>();
            for (JsonElement entry : bases.getAsJsonArray()) {
                DataResult<BrewBase> result = BrewBase.CODEC.parse(JsonOps.INSTANCE, entry);
                result.result().ifPresentOrElse(parsed::add, () -> Alchemia.LOGGER.error("Skipping a brew base in {}: {}",
                        FILE_NAME, result.error().map(DataResult.Error::message).orElse("unknown problem")));
            }
            BrewBases.install(parsed);
        } catch (IOException | RuntimeException error) {
            Alchemia.LOGGER.error("Could not read {}, using the built in brew bases", FILE_NAME, error);
            BrewBases.install(BrewBases.defaults());
        }
    }

    private static void write(Path file, List<BrewBase> bases) {
        BrewBase.LIST_CODEC.encodeStart(JsonOps.INSTANCE, bases)
                .resultOrPartial(error -> Alchemia.LOGGER.error("Could not build {}: {}", FILE_NAME, error))
                .ifPresent(encoded -> {
                    JsonObject root = new JsonObject();
                    JsonArray header = new JsonArray();
                    for (String line : HEADER) header.add(line);
                    root.add("__comment", header);
                    root.add("bases", encoded);

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
