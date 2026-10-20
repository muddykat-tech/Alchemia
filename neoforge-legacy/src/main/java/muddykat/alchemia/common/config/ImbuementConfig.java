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
import muddykat.alchemia.common.potion.Imbuements;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ImbuementConfig {

    public static final String FILE_NAME = "alchemia-imbuements.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String[] HEADER = {
            "Imbuements: items that lend a brew a sense of place when added to the cauldron.",
            "An imbued brew counts as being somewhere it is not, waking effects that only stir there.",
            "A brew holds one imbuement of each kind: one for its land and one for its altitude.",
            "Tree bark is always available and imbues the brew with the exact biome it was stripped in.",
            "",
            "item: item id that imbues the brew, e.g. \"minecraft:kelp\". Each item may appear once.",
            "name: translation key or plain text naming the imbuement, shown in the guide and tooltips.",
            "biomes: biome ids or biome tags (prefixed with #) it covers, e.g. \"#c:is_taiga\" or \"minecraft:deep_dark\".",
            "  Unknown ids and empty tags are ignored, so one file can be shared across Minecraft versions.",
            "altitude: \"high\" to satisfy every height requirement, \"low\" to satisfy every depth requirement."
    };

    private ImbuementConfig() {
    }

    public static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    public static void load() {
        Path file = path();
        if (!Files.exists(file)) {
            List<Imbuements.Category> defaults = present(Imbuements.defaults());
            Imbuements.install(defaults);
            write(file, defaults);
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement entries = root.isJsonObject() ? root.getAsJsonObject().get("modifiers") : root;
            if (entries == null || !entries.isJsonArray()) {
                Alchemia.LOGGER.error("{} has no \"modifiers\" list, using the built in imbuements", FILE_NAME);
                Imbuements.install(Imbuements.defaults());
                return;
            }

            List<Imbuements.Category> parsed = new ArrayList<>();
            for (JsonElement entry : entries.getAsJsonArray()) {
                DataResult<Imbuements.Category> result = Imbuements.Category.CODEC.parse(JsonOps.INSTANCE, entry);
                result.result().ifPresentOrElse(category -> {
                    if (BuiltInRegistries.ITEM.containsKey(category.item())) {
                        parsed.add(category);
                    } else {
                        Alchemia.LOGGER.warn("Skipping imbuement {} in {}: no such item", category.item(), FILE_NAME);
                    }
                }, () -> Alchemia.LOGGER.error("Skipping an imbuement in {}: {}",
                        FILE_NAME, result.error().map(DataResult.Error::message).orElse("unknown problem")));
            }
            Imbuements.install(parsed);
        } catch (IOException | RuntimeException error) {
            Alchemia.LOGGER.error("Could not read {}, using the built in imbuements", FILE_NAME, error);
            Imbuements.install(Imbuements.defaults());
        }
    }

    private static List<Imbuements.Category> present(List<Imbuements.Category> categories) {
        List<Imbuements.Category> kept = new ArrayList<>();
        for (Imbuements.Category category : categories) {
            if (BuiltInRegistries.ITEM.containsKey(category.item())) kept.add(category);
        }
        return kept;
    }

    private static void write(Path file, List<Imbuements.Category> categories) {
        Imbuements.Category.LIST_CODEC.encodeStart(JsonOps.INSTANCE, categories)
                .resultOrPartial(error -> Alchemia.LOGGER.error("Could not build {}: {}", FILE_NAME, error))
                .ifPresent(encoded -> {
                    JsonObject root = new JsonObject();
                    JsonArray header = new JsonArray();
                    for (String line : HEADER) header.add(line);
                    root.add("__comment", header);
                    root.add("modifiers", encoded);

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
