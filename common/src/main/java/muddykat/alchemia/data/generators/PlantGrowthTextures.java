package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.Ingredients;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class PlantGrowthTextures {

    private static final Map<Ingredients, Integer> PLACEHOLDERS = new LinkedHashMap<>();

    private PlantGrowthTextures() {
    }

    public static String plantFolder(Ingredients ingredient) {
        return ingredient.getType().name().toLowerCase(Locale.ROOT) + "s/"
                + ingredient.name().toLowerCase(Locale.ROOT);
    }

    public static String ownStage(Ingredients ingredient, int age) {
        return plantFolder(ingredient) + "/" + ingredient.name().toLowerCase(Locale.ROOT) + "_growth_" + age;
    }

    public static String ownWild(Ingredients ingredient) {
        return plantFolder(ingredient) + "/" + ingredient.name().toLowerCase(Locale.ROOT) + "_wild";
    }

    public static String sharedStage(Ingredients ingredient, int age) {
        return "growth/" + ingredient.getType().name().toLowerCase(Locale.ROOT) + "/growth_stage_" + age;
    }

    public static String maturePath(Ingredients ingredient) {
        return ingredient.getType().name().toLowerCase(Locale.ROOT) + "s/"
                + ingredient.name().toLowerCase(Locale.ROOT);
    }

    public static boolean hasOwnStage(Ingredients ingredient, int age) {
        return texture(ownStage(ingredient, age));
    }

    public static Sprite sprite(Ingredients ingredient, int age, int maxAge, boolean wild) {
        String name = ingredient.name().toLowerCase(Locale.ROOT);
        if (age < maxAge) {
            String path = stage(ingredient, age);
            return new Sprite(path, path);
        }
        if (wild && texture(ownWild(ingredient))) return new Sprite(name + "_wild", ownWild(ingredient));
        if (hasOwnStage(ingredient, age)) return new Sprite(name, ownStage(ingredient, age));
        return new Sprite(name, maturePath(ingredient));
    }

    public record Sprite(String model, String texture) {
    }

    public static Sprite part(Ingredients ingredient, String part) {
        String name = ingredient.name().toLowerCase(Locale.ROOT) + "_" + part;
        String own = plantFolder(ingredient) + "/" + name;
        return new Sprite(name, texture(own) ? own : maturePath(ingredient));
    }

    public static String ownTexture(Ingredients ingredient, String name) {
        String own = plantFolder(ingredient) + "/" + name;
        return texture(own) ? own : null;
    }

    public static String stage(Ingredients ingredient, int age) {
        if (hasOwnStage(ingredient, age)) return ownStage(ingredient, age);

        PLACEHOLDERS.merge(ingredient, 1, Integer::sum);
        return sharedStage(ingredient, age);
    }

    public static void logPlaceholders() {
        if (PLACEHOLDERS.isEmpty()) {
            Alchemia.LOGGER.info("Every plant has its own growth textures");
            return;
        }

        StringBuilder names = new StringBuilder();
        for (Map.Entry<Ingredients, Integer> entry : PLACEHOLDERS.entrySet()) {
            if (!names.isEmpty()) names.append(", ");
            names.append(entry.getKey().name().toLowerCase(Locale.ROOT)).append(" (").append(entry.getValue()).append(")");
        }

        Alchemia.LOGGER.info("{} plants still grow with the shared placeholder textures, missing stages in brackets: {}",
                PLACEHOLDERS.size(), names);
        PLACEHOLDERS.clear();
    }

    private static boolean texture(String path) {
        return PlantGrowthTextures.class.getClassLoader()
                .getResource("assets/" + Alchemia.MODID + "/textures/block/" + path + ".png") != null;
    }
}
