package muddykat.alchemia.common.crafting;

import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MachineBlueprints {

    public static final String CORE_ID = "alchemy_machine_core";

    public record Blueprint(String result, List<String> rows, Map<Character, Supplier<ItemLike>> keys) {

        public ItemStack resultStack() {
            return new ItemStack(ItemRegistry.getItemFromRegistry(result));
        }

        public ItemStack at(int row, int column) {
            Supplier<ItemLike> item = keys.get(rows.get(row).charAt(column));
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
        }
    }

    public static final Map<UpgradeSide, Ingredients> UPGRADE_MINERALS = Map.of(
            UpgradeSide.TOP, Ingredients.Cloud_Crystal,
            UpgradeSide.LEFT, Ingredients.Earth_Pyrite,
            UpgradeSide.RIGHT, Ingredients.Fire_Citrine);

    public static final Blueprint CORE = new Blueprint(CORE_ID, List.of("ISI", "GCG", "III"), keys("ISGC", List.of(
            () -> Items.IRON_INGOT,
            () -> Items.BREWING_STAND,
            () -> Items.GLASS,
            () -> Items.CAULDRON)));

    private MachineBlueprints() {
    }

    public static Blueprint upgrade(UpgradeSide side) {
        Ingredients mineral = UPGRADE_MINERALS.get(side);
        return new Blueprint(side.registryName(), List.of("GBG", "CMC", "CCC"), keys("GBCM", List.of(
                () -> Items.GLASS,
                () -> Items.GLASS_BOTTLE,
                () -> Items.COPPER_INGOT,
                () -> ItemRegistry.getItemFromRegistry(mineral.getRegistryName()))));
    }

    private static Map<Character, Supplier<ItemLike>> keys(String symbols, List<Supplier<ItemLike>> items) {
        Map<Character, Supplier<ItemLike>> keys = new LinkedHashMap<>();
        for (int index = 0; index < symbols.length(); index++) keys.put(symbols.charAt(index), items.get(index));
        return keys;
    }
}
