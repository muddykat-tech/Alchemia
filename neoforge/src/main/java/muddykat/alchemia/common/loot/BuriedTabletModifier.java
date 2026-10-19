package muddykat.alchemia.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import java.util.List;

public record BuriedTabletModifier(List<Identifier> tables, float chance, int priority) implements IGlobalLootModifier {

    public static final MapCodec<BuriedTabletModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.listOf().fieldOf("loot_tables").forGetter(BuriedTabletModifier::tables),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(BuriedTabletModifier::chance),
            Codec.INT.optionalFieldOf("priority", IGlobalLootModifier.DEFAULT_PRIORITY).forGetter(BuriedTabletModifier::priority)
    ).apply(instance, BuriedTabletModifier::new));

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!tables.contains(context.getQueriedLootTableId())) return generatedLoot;
        if (context.getRandom().nextFloat() >= chance) return generatedLoot;

        ObjectArrayList<ItemStack> unearthed = new ObjectArrayList<>();
        unearthed.add(new ItemStack(ItemRegistry.getItemFromRegistry(ItemAlchemyTablet.ID)));
        return unearthed;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
