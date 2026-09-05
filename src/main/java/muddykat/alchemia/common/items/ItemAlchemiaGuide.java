package muddykat.alchemia.common.items;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.function.Consumer;

public class ItemAlchemiaGuide extends Item {

    private static final int TOOLTIP_LIST_LIMIT = 12;

    public ItemAlchemiaGuide(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        List<String> names = RecipeDiscovery.stored(stack);

        tooltips.accept(Component.translatable("alchemia.guide.recorded", names.size(), RecipeDiscovery.total())
                .withStyle(ChatFormatting.GRAY));
        tooltips.accept(Component.translatable("alchemia.guide.brews", BrewRecipeBook.recipes(stack).size())
                .withStyle(ChatFormatting.GRAY));

        BrewRecipe chosen = BrewRecipeBook.selectedRecipe(stack);
        if (chosen != null) {
            tooltips.accept(Component.translatable("alchemia.guide.selected", chosen.displayName()).withStyle(ChatFormatting.GOLD));
        }

        if (names.isEmpty()) {
            tooltips.accept(Component.translatable("alchemia.guide.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        int shown = 0;
        for (String name : names) {
            PotionEnum recipe = RecipeDiscovery.byName(name);
            if (recipe == null) continue;
            if (shown == TOOLTIP_LIST_LIMIT) {
                tooltips.accept(Component.translatable("alchemia.guide.more", names.size() - shown).withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            tooltips.accept(Component.literal(" ").append(recipe.getEffect().value().getDisplayName()).withStyle(ChatFormatting.BLUE));
            shown++;
        }
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

        return cauldron.replaySelectedRecipe(player, stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            muddykat.alchemia.client.ClientHooks.openGuide(stack, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !RecipeDiscovery.stored(stack).isEmpty();
    }
}
