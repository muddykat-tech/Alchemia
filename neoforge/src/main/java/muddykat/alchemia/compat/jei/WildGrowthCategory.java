package muddykat.alchemia.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.effect.WildConversion;
import muddykat.alchemia.common.effect.WildGrowth;
import muddykat.alchemia.common.effect.WildGreening;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WildGrowthCategory implements IRecipeCategory<WildGrowthCategory.Entry> {

    public record Entry(int tier, List<ItemStack> from, List<ItemStack> to, Optional<HolderSet<Biome>> biomes,
                        WildGreening.@Nullable Named greening) {
    }

    public static final IRecipeType<Entry> TYPE = IRecipeType.create(Alchemia.MODID, "wild_growth", Entry.class);

    private static final int WIDTH = 152;
    private static final int HEIGHT = 32;
    private static final int POTION_X = 1;
    private static final int FROM_X = 44;
    private static final int ARROW_X = 68;
    private static final int TO_X = 100;
    private static final int SLOT_Y = 1;
    private static final int TEXT_Y = 22;
    private static final int TEXT_HEIGHT = 10;
    private static final int TEXT_COLOUR = 0xFF5A5A5A;
    private static final int DURATION = 2400;

    private static final Map<ResourceKey<Biome>, List<Item>> SCENERY = Map.of(
            Biomes.DESERT, List.of(Items.SAND, Items.DEAD_BUSH, Items.CACTUS),
            Biomes.PLAINS, List.of(Items.GRASS_BLOCK, Items.SHORT_GRASS, Items.OXEYE_DAISY),
            Biomes.FLOWER_FOREST, List.of(Items.ALLIUM, Items.LILAC, Items.PINK_TULIP, Items.CORNFLOWER, Items.ROSE_BUSH));

    private final IDrawable icon;

    public WildGrowthCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(potion(Items.POTION, 1));
    }

    public static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return entries;
        for (WildConversion conversion : WildConversion.all(connection.registryAccess())) {
            if (conversion.from().asItem() == Items.AIR || conversion.to().asItem() == Items.AIR) continue;
            entries.add(new Entry(conversion.tier(), List.of(new ItemStack(conversion.from())), List.of(new ItemStack(conversion.to())),
                    conversion.biomes(), null));
        }
        for (WildGreening.Named named : WildGreening.all(connection.registryAccess())) {
            WildGreening greening = named.greening();
            Optional<ResourceKey<Biome>> from = greening.from().stream().findFirst().flatMap(Holder::unwrapKey);
            entries.add(new Entry(greening.tier(), scenery(from.orElse(Biomes.PLAINS)), scenery(greening.to()), Optional.empty(), named));
        }
        return entries;
    }

    public static List<ItemStack> potions(int tier) {
        return List.of(potion(Items.POTION, tier), potion(Items.SPLASH_POTION, tier), potion(Items.LINGERING_POTION, tier));
    }

    private static ItemStack potion(Item item, int tier) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(),
                List.of(new MobEffectInstance(MobEffectRegistry.WILD_GROWTH, DURATION, tier - 1)), Optional.of("alchemical")));
        return stack;
    }

    private static List<ItemStack> scenery(ResourceKey<Biome> biome) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : SCENERY.getOrDefault(biome, List.of(Items.GRASS_BLOCK))) stacks.add(new ItemStack(item));
        return stacks;
    }

    private static Component biome(ResourceKey<Biome> key) {
        return Component.translatable(key.identifier().toLanguageKey("biome"));
    }

    private static Component biomes(HolderSet<Biome> set) {
        return set.unwrap().map(tag -> {
            Identifier id = tag.location();
            return Component.translatableWithFallback("tag.worldgen.biome." + id.getNamespace() + "." + id.getPath().replace('/', '.'), "#" + id);
        }, holders -> {
            MutableComponent names = Component.empty();
            for (int index = 0; index < holders.size(); index++) {
                if (index > 0) names.append(", ");
                Optional<ResourceKey<Biome>> key = holders.get(index).unwrapKey();
                names.append(key.isPresent() ? biome(key.get()) : Component.literal("?"));
            }
            return names;
        });
    }

    @Override
    public IRecipeType<Entry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("alchemia.jei.wild_growth");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, POTION_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStacks(potions(entry.tier()));
        WildGreening.Named named = entry.greening();
        if (named == null) {
            var from = builder.addSlot(RecipeIngredientRole.INPUT, FROM_X, SLOT_Y).setStandardSlotBackground().addItemStacks(entry.from());
            entry.biomes().ifPresent(set -> from.addRichTooltipCallback((view, tooltip) ->
                    tooltip.add(Component.translatable("alchemia.jei.wild_growth.only_in", biomes(set)).withStyle(ChatFormatting.GOLD))));
            builder.addSlot(RecipeIngredientRole.OUTPUT, TO_X, SLOT_Y).setOutputSlotBackground().addItemStacks(entry.to());
            return;
        }
        WildGreening greening = named.greening();
        Component from = biomes(greening.from());
        Component to = biome(greening.to());
        Component tier = Component.translatable("enchantment.level." + greening.tier());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, FROM_X, SLOT_Y).setStandardSlotBackground().addItemStacks(entry.from())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(from.copy().withStyle(ChatFormatting.GOLD)));
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, TO_X, SLOT_Y).setOutputSlotBackground().addItemStacks(entry.to())
                .addRichTooltipCallback((view, tooltip) -> {
                    tooltip.add(to.copy().withStyle(ChatFormatting.GREEN));
                    tooltip.add(Component.translatable("alchemia.jei.wild_growth." + greening.counts().getSerializedName(),
                            greening.steps(), tier).withStyle(ChatFormatting.GRAY));
                    if (greening.grass() > 0) {
                        tooltip.add(Component.translatable("alchemia.jei.wild_growth.grass", greening.grass()).withStyle(ChatFormatting.GRAY));
                    }
                });
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Entry entry, IFocusGroup focuses) {
        WildGreening.Named named = entry.greening();
        builder.addRecipeArrowWidget().setPosition(ARROW_X, SLOT_Y + 1);
        Component tier = Component.translatable("enchantment.level." + entry.tier());
        Component line = named == null
                ? Component.translatable("alchemia.jei.wild_growth.block", tier)
                : Component.translatable("alchemia.jei.wild_growth.biome", tier, biomes(named.greening().from()), biome(named.greening().to()));
        builder.addText(line, WIDTH - 2, TEXT_HEIGHT).setPosition(POTION_X, TEXT_Y).setColor(TEXT_COLOUR);
    }

    @Override
    public @Nullable Identifier getIdentifier(Entry entry) {
        WildGreening.Named named = entry.greening();
        String name = named != null ? "biome/" + named.id().getNamespace() + "/" + named.id().getPath() : "block/" + entry.tier() + "/" + entry.from().getFirst().getItem();
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "wild_growth/" + name.replace(':', '_'));
    }
}
