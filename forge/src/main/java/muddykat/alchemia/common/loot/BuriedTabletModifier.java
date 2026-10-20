package muddykat.alchemia.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.loot.IGlobalLootModifier;

import java.util.List;

public record BuriedTabletModifier(List<ResourceLocation> tables, float chance) implements IGlobalLootModifier {

    public static final Codec<BuriedTabletModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("loot_tables").forGetter(BuriedTabletModifier::tables),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(BuriedTabletModifier::chance)
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
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
