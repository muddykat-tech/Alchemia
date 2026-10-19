package muddykat.alchemia.common.items;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ItemRecipePage extends Item {

    public static final String ID = "recipe_page";

    public ItemRecipePage(Properties properties) {
        super(properties.stacksTo(16));
    }

    public static @Nullable BrewRecipe recipe(ItemStack stack) {
        return stack.get(DataComponentRegistry.PAGE_RECIPE.get());
    }

    public static ItemStack written(BrewRecipe recipe) {
        ItemStack page = new ItemStack(ItemRegistry.getItemFromRegistry(ID));
        page.set(DataComponentRegistry.PAGE_RECIPE.get(), recipe);
        return page;
    }

    @Override
    public Component getName(ItemStack stack) {
        BrewRecipe recipe = recipe(stack);
        return recipe == null ? super.getName(stack)
                : Component.translatable("item.alchemia.recipe_page.written", recipe.displayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        BrewRecipe recipe = recipe(stack);
        if (recipe == null) {
            tooltips.accept(Component.translatable("alchemia.page.blank").withStyle(ChatFormatting.DARK_GRAY));
            tooltips.accept(Component.translatable("alchemia.page.bind_hint").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltips.accept(Component.translatable("alchemia.guide.recipe.base", recipe.brewBase().displayName())
                .withStyle(ChatFormatting.GRAY));
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            tooltips.accept(Component.literal(" ").append(Component.translatable("alchemia.guide.effect.line",
                    effect.displayName(), Component.translatable("enchantment.level." + effect.potency())))
                    .withStyle(ChatFormatting.BLUE));
        }
        tooltips.accept(Component.translatable("alchemia.page.bind_hint").withStyle(ChatFormatting.DARK_GRAY));
        tooltips.accept(Component.translatable("alchemia.page.brew_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (recipe(player.getItemInHand(hand)) == null) return InteractionResult.PASS;
        if (level.isClientSide()) muddykat.alchemia.client.ClientHooks.openPage(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof TileEntityAlchemyCauldron cauldron)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.CONSUME;
        return cauldron.replay(player, recipe(stack));
    }
}
