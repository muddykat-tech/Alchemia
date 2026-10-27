package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.TileEntityResearchDesk;
import muddykat.alchemia.common.guide.GuideShelf;
import muddykat.alchemia.common.items.ItemAlbedo;
import muddykat.alchemia.common.items.ItemCitrinitas;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemNigredo;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BrewReplay {

    private BrewReplay() {
    }

    public static InteractionResult fromDesk(TileEntityAlchemyCauldron cauldron, Player player) {
        Level level = cauldron.getLevel();
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = cauldron.getBlockPos();
        TileEntityResearchDesk nearest = null;
        for (TileEntityResearchDesk desk : GuideShelf.desksNear(level, pos)) {
            if (BrewRecipeBook.selectedRecipe(desk.guide()) == null) continue;
            if (nearest == null || desk.getBlockPos().distSqr(pos) < nearest.getBlockPos().distSqr(pos)) {
                nearest = desk;
            }
        }
        if (nearest == null) return InteractionResult.PASS;
        return replay(cauldron, player, BrewRecipeBook.selectedRecipe(nearest.guide()));
    }

    public static InteractionResult replay(TileEntityAlchemyCauldron cauldron, Player player, @Nullable BrewRecipe recipe) {
        Level level = cauldron.getLevel();
        BlockPos pos = cauldron.getBlockPos();
        if (level == null) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        if (recipe == null || !recipe.isResolvable()) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.none_selected"));
            return InteractionResult.FAIL;
        }

        if (cauldron.getWaterLevel() <= 0) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.no_water"));
            return InteractionResult.FAIL;
        }

        if (!recipe.brewBase().equals(cauldron.getBase())) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.wrong_recipe_base",
                    recipe.brewBase().displayName()));
            return InteractionResult.FAIL;
        }

        if (!cauldron.isUntouched() || cauldron.getCourse().planned() > 0) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.not_clean"));
            return InteractionResult.FAIL;
        }

        Component heldBack = heldBack(cauldron, recipe);
        if (heldBack != null) {
            serverPlayer.sendOverlayMessage(heldBack);
            return InteractionResult.FAIL;
        }

        if (recipe.requiresGrinding() && !carries(player, ItemMortarPestle.class)) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.needs_mortar"));
            return InteractionResult.FAIL;
        }

        if (recipe.requiresTrimming() && !carries(player, ItemNigredo.class)) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.needs_nigredo"));
            return InteractionResult.FAIL;
        }

        if (recipe.requiresTurning() && !carries(player, ItemAlbedo.class) && !carries(player, ItemCitrinitas.class)) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.needs_turning"));
            return InteractionResult.FAIL;
        }

        for (Imbuements.Attuner needed : Imbuements.of(recipe)) {
            if (Imbuements.slotOf(player, needed) < 0) {
                serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.needs_attuner", needed.stack().getHoverName()));
                return InteractionResult.FAIL;
            }
        }

        Map<ItemStack, Integer> required = requiredTally(recipe);

        for (Map.Entry<ItemStack, Integer> entry : required.entrySet()) {
            if (countIn(player, entry.getKey()) < entry.getValue()) {
                serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.missing", entry.getKey().getHoverName()));
                return InteractionResult.FAIL;
            }
        }

        for (Map.Entry<ItemStack, Integer> entry : required.entrySet()) {
            consumeFrom(player, entry.getKey(), entry.getValue());
        }
        if (!player.getAbilities().instabuild) {
            for (Imbuements.Attuner needed : Imbuements.of(recipe)) {
                int slot = Imbuements.slotOf(player, needed);
                if (slot >= 0) player.getInventory().getItem(slot).shrink(1);
            }
        }

        cauldron.setBrewName(recipe.name());
        cauldron.performAll(recipe.steps());

        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.gameEvent(null, GameEvent.SPLASH, pos);
        serverPlayer.sendOverlayMessage(Component.translatable(brewedAsRecorded(cauldron, recipe) ? "alchemia.brew.done" : "alchemia.brew.drifted"));
        cauldron.sync();

        return InteractionResult.CONSUME;
    }

    public static Map<ItemStack, Integer> requiredTally(BrewRecipe recipe) {
        Map<ItemStack, Integer> required = new LinkedHashMap<>();

        for (Map.Entry<ItemStack, Integer> entry : recipe.tally().entrySet()) {
            ItemStack existing = null;
            for (ItemStack seen : required.keySet()) {
                if (sameIngredient(seen, entry.getKey())) {
                    existing = seen;
                    break;
                }
            }
            required.merge(existing == null ? entry.getKey() : existing, entry.getValue(), Integer::sum);
        }
        return required;
    }

    private static @Nullable Component heldBack(TileEntityAlchemyCauldron cauldron, BrewRecipe recipe) {
        Level level = cauldron.getLevel();
        BlockPos pos = cauldron.getBlockPos();
        List<Imbuements.Attuner> needed = Imbuements.of(recipe);
        WorldEffects.Imbued attuned = needed.isEmpty() ? cauldron.attunedHolder() : CauldronImbuement.imbue(needed, level);
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            AlchemiaEffect potion = effect.asPotion();
            if (potion == null) continue;

            WorldEffects.Requirement requirement = WorldEffects.requirement(potion);
            if (requirement != null && !requirement.holds(level, pos, attuned)) {
                return Component.translatable("alchemia.brew.recipe_dormant", effect.displayName(), requirement.hint());
            }

            int cap = WorldEffects.levelCap(potion, level, pos, attuned);
            if (effect.potency() > cap) {
                Component hint = WorldEffects.levelHint(potion, cap + 1);
                return Component.translatable("alchemia.brew.recipe_held_back", effect.displayName(),
                        hint == null ? Component.empty() : hint);
            }
        }
        return null;
    }

    private static boolean brewedAsRecorded(TileEntityAlchemyCauldron cauldron, BrewRecipe recipe) {
        if (cauldron.isSpoiled() || cauldron.getEffectList().size() != recipe.effects().size()) return false;

        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            AlchemiaEffect potion = effect.asPotion();
            if (potion == null || cauldron.heldPotency(potion.effect()) != effect.potency()) return false;
        }
        return true;
    }

    private static boolean sameIngredient(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem();
    }

    private static boolean carries(Player player, Class<?> tool) {
        for (ItemStack stack : player.getInventory()) {
            if (tool.isInstance(stack.getItem())) return true;
        }
        return false;
    }

    private static int countIn(Player player, ItemStack wanted) {
        int found = 0;
        for (ItemStack stack : player.getInventory()) {
            if (sameIngredient(stack, wanted)) found += stack.getCount();
        }
        return found;
    }

    private static void consumeFrom(Player player, ItemStack wanted, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory()) {
            if (remaining <= 0) break;
            if (!sameIngredient(stack, wanted)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
    }
}
