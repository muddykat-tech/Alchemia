package muddykat.alchemia.common.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.data.AlchemiaItemTags;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Imbuements {

    public static final String HIGH = "high";
    public static final String LOW = "low";

    private static final char FIXED_MARK = '@';

    public record Category(Identifier item, String name, List<String> biomes, Optional<String> altitude) {
        public static final Codec<Category> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("item").forGetter(Category::item),
                Codec.STRING.fieldOf("name").forGetter(Category::name),
                Codec.STRING.listOf().optionalFieldOf("biomes", List.of()).forGetter(Category::biomes),
                Codec.STRING.optionalFieldOf("altitude").forGetter(Category::altitude)
        ).apply(instance, Category::new));

        Category(Identifier item, String name, List<String> biomes) {
            this(item, name, biomes, Optional.empty());
        }

        Category(Identifier item, String name, String altitude) {
            this(item, name, List.of(), Optional.of(altitude));
        }

        public boolean high() {
            return altitude.filter(HIGH::equalsIgnoreCase).isPresent();
        }

        public boolean low() {
            return altitude.filter(LOW::equalsIgnoreCase).isPresent();
        }

        public static final Codec<List<Category>> LIST_CODEC = CODEC.listOf();

        public Component displayName() {
            return Component.translatableWithFallback(name, name);
        }

        public List<Holder<Biome>> resolve(Registry<Biome> registry) {
            Set<Holder<Biome>> found = new LinkedHashSet<>();
            for (String selector : biomes) {
                boolean tag = selector.startsWith("#");
                Identifier id = Identifier.tryParse(tag ? selector.substring(1) : selector);
                if (id == null) continue;
                if (tag) {
                    for (Holder<Biome> biome : registry.getTagOrEmpty(TagKey.create(Registries.BIOME, id))) found.add(biome);
                } else {
                    registry.get(ResourceKey.create(Registries.BIOME, id)).ifPresent(found::add);
                }
            }
            return List.copyOf(found);
        }
    }

    private static volatile Map<Item, Category> categories = Map.of();
    private static volatile List<Category> definitions = List.of();

    public record Attuner(Item item, @Nullable ResourceKey<Biome> biome) {

        public @Nullable Category category() {
            return biome != null ? null : categories.get(item);
        }

        public boolean placesBiome() {
            if (biome != null) return true;
            Category category = category();
            return category != null && !category.biomes().isEmpty();
        }

        public boolean placesAltitude() {
            Category category = category();
            return category != null && (category.high() || category.low());
        }

        public boolean blank() {
            return !placesBiome() && !placesAltitude();
        }

        public boolean clashes(Attuner other) {
            return (placesBiome() && other.placesBiome()) || (placesAltitude() && other.placesAltitude());
        }

        public List<Holder<Biome>> resolve(Registry<Biome> registry) {
            if (biome != null) return registry.get(biome).<List<Holder<Biome>>>map(List::of).orElse(List.of());
            Category category = category();
            return category == null ? List.of() : category.resolve(registry);
        }

        public WorldEffects.Imbued imbue(Registry<Biome> registry) {
            Category category = category();
            return new WorldEffects.Imbued(resolve(registry), category != null && category.high(), category != null && category.low());
        }

        public Component region() {
            if (biome != null) return ItemBark.biomeName(biome);
            Category category = category();
            return category == null ? item.getName(new ItemStack(item)) : category.displayName();
        }

        public ItemStack stack() {
            return biome != null ? ItemBark.gathered(biome) : new ItemStack(item);
        }

        public boolean matches(ItemStack stack) {
            if (stack.getItem() != item) return false;
            return biome == null || biome.equals(ItemBark.biomeOf(stack));
        }

        public String step() {
            if (biome != null) return BrewRecipe.STEP_BARK + biome.identifier().toString();
            return BrewRecipe.STEP_BARK + "" + FIXED_MARK + BuiltInRegistries.ITEM.getKey(item);
        }
    }

    private Imbuements() {
    }

    public static List<Category> defaults() {
        return List.of(
                new Category(id("kelp"), "alchemia.imbuement.waters", List.of("#c:is_aquatic", "#minecraft:is_beach", "#c:is_stony_shores")),
                new Category(id("sweet_berries"), "alchemia.imbuement.taigas", List.of("#c:is_taiga")),
                new Category(id("golden_dandelion"), "alchemia.imbuement.grasslands", List.of("#c:is_plains", "#c:is_floral")),
                new Category(id("cactus_flower"), "alchemia.imbuement.drylands", List.of("#c:is_desert", "#c:is_badlands")),
                new Category(id("cocoa_beans"), "alchemia.imbuement.jungles", List.of("#c:is_jungle")),
                new Category(id("lily_pad"), "alchemia.imbuement.swamps", List.of("#c:is_swamp")),
                new Category(id("resin_clump"), "alchemia.imbuement.shadowed_woods", List.of("minecraft:pale_garden", "minecraft:dark_forest")),
                new Category(id("snowball"), "alchemia.imbuement.snowlands", List.of("#c:is_snowy", "#c:is_icy")),
                new Category(id("glow_lichen"), "alchemia.imbuement.caves", List.of("#c:is_cave")),
                new Category(id("echo_shard"), "alchemia.imbuement.deep_dark", List.of("minecraft:deep_dark")),
                new Category(id("netherite_scrap"), "alchemia.imbuement.nether", List.of("#c:is_nether")),
                new Category(id("chorus_flower"), "alchemia.imbuement.end", List.of("#c:is_end")),
                new Category(id("wind_charge"), "alchemia.imbuement.heights", HIGH),
                new Category(id("coal"), "alchemia.imbuement.depths", LOW));
    }

    private static Identifier id(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public static synchronized void install(List<Category> installed) {
        Map<Item, Category> byItem = new LinkedHashMap<>();
        List<Category> kept = new ArrayList<>();
        for (Category category : installed) {
            if (!BuiltInRegistries.ITEM.containsKey(category.item())) continue;
            Item item = BuiltInRegistries.ITEM.getValue(category.item());
            if (item == Items.AIR || item instanceof ItemBark || byItem.containsKey(item)) continue;
            byItem.put(item, category);
            kept.add(category);
        }
        categories = Map.copyOf(byItem);
        definitions = List.copyOf(kept);
    }

    public static List<Category> definitions() {
        return definitions;
    }

    public static boolean isAttuner(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof ItemBark || stack.is(AlchemiaItemTags.IMBUEMENTS) || categories.containsKey(stack.getItem());
    }

    public static @Nullable Attuner of(ItemStack stack) {
        if (!isAttuner(stack)) return null;
        if (stack.getItem() instanceof ItemBark) return new Attuner(stack.getItem(), ItemBark.biomeOf(stack));
        return new Attuner(stack.getItem(), null);
    }

    public static @Nullable Attuner parse(String step) {
        if (step.length() < 2 || step.charAt(0) != BrewRecipe.STEP_BARK) return null;
        if (step.charAt(1) == FIXED_MARK) {
            Identifier id = Identifier.tryParse(step.substring(2));
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;
            return new Attuner(BuiltInRegistries.ITEM.getValue(id), null);
        }
        Identifier id = Identifier.tryParse(step.substring(1));
        return id == null ? null : new Attuner(bark(), ResourceKey.create(Registries.BIOME, id));
    }

    public static List<Attuner> of(BrewRecipe recipe) {
        List<Attuner> found = new ArrayList<>();
        for (String step : recipe.steps()) {
            Attuner attuner = parse(step);
            if (attuner != null) found.add(attuner);
        }
        return found;
    }

    public static int slotOf(Player player, Attuner attuner) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (attuner.matches(inventory.getItem(slot))) return slot;
        }
        return -1;
    }

    public static boolean sameKind(ItemStack held, ItemStack wanted) {
        Attuner a = of(held);
        Attuner b = of(wanted);
        return a != null && b != null && a.item() == b.item() && Objects.equals(a.biome(), b.biome());
    }

    public static Item bark() {
        return ItemRegistry.getItemFromRegistry(ItemBark.ID).asItem();
    }

    public static WorldEffects.Imbued merge(List<WorldEffects.Imbued> parts) {
        List<Holder<Biome>> biomes = new ArrayList<>();
        boolean high = false;
        boolean low = false;
        for (WorldEffects.Imbued part : parts) {
            biomes.addAll(part.biomes());
            high |= part.high();
            low |= part.low();
        }
        return new WorldEffects.Imbued(List.copyOf(biomes), high, low);
    }

    public static List<Item> items() {
        Set<Item> items = new LinkedHashSet<>();
        items.add(bark());
        items.addAll(categories.keySet());
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(AlchemiaItemTags.IMBUEMENTS)) items.add(holder.value());
        return List.copyOf(items);
    }
}
