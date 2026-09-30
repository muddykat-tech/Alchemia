package muddykat.alchemia.data.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedPotions;
import muddykat.alchemia.common.blocks.helper.PotionCluster;
import muddykat.alchemia.common.potion.PotionGlass;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlacedPotionModelProvider implements DataProvider {

    private final Path assets;
    private final String renderType;

    public PlacedPotionModelProvider(PackOutput output) {
        this(output, null);
    }

    public PlacedPotionModelProvider(PackOutput output, String renderType) {
        this.assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(Alchemia.MODID);
        this.renderType = renderType;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        JsonArray parts = new JsonArray();

        for (PotionGlass glass : PotionGlass.values()) {
            JsonObject source = sourceModel(glass);
            for (int count = 1; count <= PotionCluster.MAX_POTIONS; count++) {
                for (int slot = 0; slot < count; slot++) {
                    String name = glass.getSerializedName() + "_" + count + "_" + slot;
                    int[] offset = PotionCluster.offset(count, slot);
                    writes.add(DataProvider.saveStable(cache, shifted(source, offset),
                            assets.resolve("models/block/potion_blocks/" + name + ".json")));
                    parts.add(part(glass, count, slot, name));
                }
            }
        }

        JsonObject blockstate = new JsonObject();
        blockstate.add("multipart", parts);
        writes.add(DataProvider.saveStable(cache, blockstate, assets.resolve("blockstates/" + BlockPlacedPotions.ID + ".json")));

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private static JsonObject part(PotionGlass glass, int count, int slot, String model) {
        JsonObject when = new JsonObject();
        when.addProperty(BlockPlacedPotions.POTIONS.getName(), String.valueOf(count));
        when.addProperty(BlockPlacedPotions.SLOTS.get(slot).getName(), glass.getSerializedName());

        JsonObject apply = new JsonObject();
        apply.addProperty("model", Alchemia.MODID + ":block/potion_blocks/" + model);

        JsonObject part = new JsonObject();
        part.add("when", when);
        part.add("apply", apply);
        return part;
    }

    private JsonObject shifted(JsonObject source, int[] offset) {
        JsonObject model = new JsonObject();
        if (source.has("texture_size")) model.add("texture_size", source.get("texture_size"));
        model.add("textures", source.get("textures"));
        if (renderType != null) model.addProperty("render_type", renderType);

        JsonArray elements = new JsonArray();
        for (JsonElement entry : source.getAsJsonArray("elements")) {
            JsonObject element = entry.getAsJsonObject().deepCopy();
            element.remove("name");
            shift(element.getAsJsonArray("from"), offset);
            shift(element.getAsJsonArray("to"), offset);
            if (element.has("rotation")) shift(element.getAsJsonObject("rotation").getAsJsonArray("origin"), offset);
            elements.add(element);
        }
        model.add("elements", elements);
        return model;
    }

    private static void shift(JsonArray position, int[] offset) {
        position.set(0, new JsonPrimitive(position.get(0).getAsDouble() + offset[0]));
        position.set(2, new JsonPrimitive(position.get(2).getAsDouble() + offset[1]));
    }

    private static JsonObject sourceModel(PotionGlass glass) {
        String path = "assets/" + Alchemia.MODID + "/models/block/" + glass.getSerializedName() + ".json";
        try (InputStream stream = PlacedPotionModelProvider.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing placed potion model " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getName() {
        return "Alchemia placed potion models";
    }
}
