package muddykat.alchemia.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muddykat.alchemia.common.potion.AlchemicalPotions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.loot.IGlobalLootModifier;

import java.util.List;

public record AlchemicalPotionModifier(List<ResourceLocation> tables, MobEffect effect, Item item, int minLevel, int maxLevel, float chance) implements IGlobalLootModifier {

    public static final Codec<AlchemicalPotionModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("loot_tables").forGetter(AlchemicalPotionModifier::tables),
            BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("effect").forGetter(AlchemicalPotionModifier::effect),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.POTION).forGetter(AlchemicalPotionModifier::item),
            Codec.intRange(1, 10).optionalFieldOf("min_level", 1).forGetter(AlchemicalPotionModifier::minLevel),
            Codec.intRange(1, 10).optionalFieldOf("max_level", 1).forGetter(AlchemicalPotionModifier::maxLevel),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(AlchemicalPotionModifier::chance)
    ).apply(instance, AlchemicalPotionModifier::new));

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!tables.contains(context.getQueriedLootTableId())) return generatedLoot;
        if (context.getRandom().nextFloat() >= chance) return generatedLoot;
        int spread = Math.max(0, maxLevel - minLevel);
        int level = minLevel + (spread == 0 ? 0 : context.getRandom().nextInt(spread + 1));
        generatedLoot.add(AlchemicalPotions.of(item, effect, level));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
