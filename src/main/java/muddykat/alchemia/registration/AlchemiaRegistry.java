package muddykat.alchemia.registration;

import muddykat.alchemia.common.items.helper.Ingredients;

public class AlchemiaRegistry {

    public static void initialize() {
        for (Ingredients ingredient : Ingredients.values()) {
            ingredient.register();
        }
    }
}
