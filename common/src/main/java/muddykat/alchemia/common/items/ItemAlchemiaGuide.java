package muddykat.alchemia.common.items;

import muddykat.alchemia.common.blocks.blockentity.BlockPlacedGuide;
import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ItemAlchemiaGuide extends Item {

    private static final int SURVEY_TICKS = 10;

    public record Binding(int pages, @Nullable BrewRecipe recipe, @Nullable String refusal) {
        static Binding refused(String key) {
            return new Binding(0, null, key);
        }

        public boolean bound() {
            return pages > 0 || recipe != null;
        }

        public Component message(ItemStack guide) {
            if (refusal != null) return Component.translatable(refusal);
            if (recipe != null) return Component.translatable("alchemia.guide.bound_recipe", recipe.displayName());
            return Component.translatable("alchemia.guide.bound_pages", pages, BrewRecipeBook.pages(guide));
        }
    }

    public ItemAlchemiaGuide(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean canBind(ItemStack stack) {
        return stack.is(Items.PAPER) || stack.getItem() instanceof ItemRecipePage;
    }

    public static Binding bind(ItemStack guide, ItemStack stack, Player player, boolean whole) {
        boolean free = player.getAbilities().instabuild;
        BrewRecipe recipe = stack.getItem() instanceof ItemRecipePage ? ItemRecipePage.recipe(stack) : null;

        if (recipe != null) {
            if (BrewRecipeBook.holds(guide, recipe)) return Binding.refused("alchemia.guide.bind_duplicate");
            int added = BrewRecipeBook.bindPages(guide, 1);
            if (added == 0 && BrewRecipeBook.freePages(guide) == 0) return Binding.refused("alchemia.guide.bind_full");

            BrewRecipeBook.write(guide, recipe);
            if (!free) stack.shrink(1);
            return new Binding(added, recipe, null);
        }

        int bound = BrewRecipeBook.bindPages(guide, whole ? stack.getCount() : 1);
        if (bound == 0) return Binding.refused("alchemia.guide.bind_full");
        if (!free) stack.shrink(bound);
        return new Binding(bound, null, null);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess carried) {
        if (!canBind(other) || !slot.allowModification(player)) return false;

        Binding binding = bind(self, other, player, action == ClickAction.PRIMARY);
        if (binding.bound()) {
            slot.setChanged();
            player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.9F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendOverlayMessage(binding.message(self));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        int recipes = BrewRecipeBook.recipes(stack).size();
        tooltips.accept(Component.translatable("alchemia.guide.tooltip.pages", recipes, BrewRecipeBook.pages(stack))
                .withStyle(ChatFormatting.GRAY));
        tooltips.accept(Component.translatable("alchemia.guide.recorded", RecipeDiscovery.stored(stack).size(), RecipeDiscovery.total())
                .withStyle(ChatFormatting.GRAY));
        tooltips.accept(Component.translatable("alchemia.guide.tooltip.ingredients",
                IngredientDiscovery.found(stack), IngredientDiscovery.totalAll()).withStyle(ChatFormatting.GRAY));

        BrewRecipe chosen = BrewRecipeBook.selectedRecipe(stack);
        if (chosen != null) {
            tooltips.accept(Component.translatable("alchemia.guide.selected", chosen.displayName()).withStyle(ChatFormatting.GOLD));
        }

        tooltips.accept(Component.translatable(BrewRecipeBook.freePages(stack) > 0
                ? "alchemia.guide.tooltip.bind" : "alchemia.guide.tooltip.full").withStyle(ChatFormatting.DARK_GRAY));
        tooltips.accept(Component.translatable("alchemia.guide.tooltip.place").withStyle(ChatFormatting.DARK_GRAY));
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof BlockResearchDesk) {
            return InteractionResult.PASS;
        }
        return BlockPlacedGuide.place(context.getLevel(), context.getClickedPos(), context.getClickedFace(), player, context.getItemInHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        muddykat.alchemia.common.guide.GuideNews.survey(player, stack);
        if (level.isClientSide()) {
            muddykat.alchemia.client.ClientHooks.openGuide(hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (owner instanceof Player player && owner.tickCount % SURVEY_TICKS == 0) {
            muddykat.alchemia.common.guide.GuideNews.survey(player, stack);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !RecipeDiscovery.stored(stack).isEmpty();
    }
}
