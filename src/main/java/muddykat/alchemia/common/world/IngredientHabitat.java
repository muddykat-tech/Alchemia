package muddykat.alchemia.common.world;

import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.Ingredients;

import java.util.EnumMap;
import java.util.Map;

public enum IngredientHabitat {
    SURFACE,
    SAND,
    AQUATIC,
    UNDERGROUND,
    SULFUR,
    CEILING,
    GEODE;

    private static final Map<Ingredients, IngredientHabitat> OVERRIDES = new EnumMap<>(Ingredients.class);

    static {
        OVERRIDES.put(Ingredients.Flameweed, AQUATIC);
        OVERRIDES.put(Ingredients.Tangleweed, AQUATIC);
        OVERRIDES.put(Ingredients.Druids_Rosemary, SURFACE);
        OVERRIDES.put(Ingredients.Firebell, SURFACE);
        OVERRIDES.put(Ingredients.Marshroom, SURFACE);
        OVERRIDES.put(Ingredients.Sulphur_Shelf, SULFUR);
    }

    public static IngredientHabitat of(Ingredients ingredient) {
        IngredientHabitat override = OVERRIDES.get(ingredient);
        if (override != null) return override;

        return switch (ingredient.getType()) {
            case Flower, Herb -> ingredient.getPrimaryAlignment() == IngredientAlignment.Fire ? SAND : SURFACE;
            case Root -> CEILING;
            case Mushroom -> UNDERGROUND;
            case Mineral -> GEODE;
        };
    }

    public boolean isUnderground() {
        return this == UNDERGROUND || this == SULFUR || this == CEILING || this == GEODE;
    }
}
