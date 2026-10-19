package muddykat.alchemia.common.items;

import muddykat.alchemia.registration.registers.DataComponentRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ItemBark extends Item {

    public static final String ID = "bark";

    public ItemBark(Properties properties) {
        super(properties);
    }

    public static @Nullable ResourceKey<Biome> biomeOf(ItemStack stack) {
        return stack.get(DataComponentRegistry.BARK_BIOME.get());
    }

    public static ItemStack gathered(ResourceKey<Biome> biome) {
        ItemStack bark = new ItemStack(ItemRegistry.getItemFromRegistry(ID));
        bark.set(DataComponentRegistry.BARK_BIOME.get(), biome);
        return bark;
    }

    public static Component biomeName(ResourceKey<Biome> biome) {
        return Component.translatable("biome." + biome.identifier().getNamespace() + "." + biome.identifier().getPath());
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceKey<Biome> biome = biomeOf(stack);
        return biome == null ? super.getName(stack) : Component.translatable("item.alchemia.bark.from", biomeName(biome));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        ResourceKey<Biome> biome = biomeOf(stack);
        tooltips.accept(Component.translatable(biome == null ? "alchemia.tooltip.bark.blank" : "alchemia.tooltip.bark")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
