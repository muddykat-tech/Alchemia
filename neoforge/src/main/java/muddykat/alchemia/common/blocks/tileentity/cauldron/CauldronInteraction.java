package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.effect.TriggerMobEffect;
import muddykat.alchemia.common.guide.GuideShelf;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.common.items.ItemRubedo;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.common.utility.Handover;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class CauldronInteraction {

    private CauldronInteraction() {
    }

    public static InteractionResult use(TileEntityAlchemyCauldron cauldron, BlockPos pos, Player player, InteractionHand hand) {
        Level level = cauldron.getLevel();
        if (level == null) return InteractionResult.FAIL;

        ItemStack heldStack = player.getItemInHand(hand);
        Item heldItem = heldStack.getItem();

        if (heldItem.equals(Items.BUCKET) && !cauldron.canFill()) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            cauldron.empty();
            return InteractionResult.SUCCESS;
        }

        InteractionResult filled = CauldronFilling.tryFill(cauldron, player, hand, heldStack, pos, level);
        if (filled != null) return filled;

        if (heldItem instanceof ItemRubedo) {
            if (!cauldron.addRubedo(player)) return InteractionResult.CONSUME;

            heldStack.shrink(1);
            player.setItemInHand(hand, heldStack);
            return InteractionResult.CONSUME;
        }

        if (heldItem instanceof ItemIngredient) {
            if (cauldron.getWaterLevel() <= 0) return InteractionResult.FAIL;
            if (level.isClientSide()) return InteractionResult.CONSUME;

            if (!cauldron.plan(heldStack)) {
                TileEntityAlchemyCauldron.message(player, cauldron.isSpoiled() ? "alchemia.brew.ruined" : "alchemia.brew.queue_full");
                return InteractionResult.CONSUME;
            }

            GuideShelf shelf = cauldron.journal().shelf(player);
            IngredientDiscovery.record(shelf, heldStack);
            shelf.settle(pos);
            heldStack.shrink(1);
            player.setItemInHand(hand, heldStack);

            level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            cauldron.sync();
            return InteractionResult.CONSUME;
        }

        if (heldItem.equals(Items.DRAGON_BREATH)) {
            if (cauldron.getWaterLevel() <= 0) return InteractionResult.FAIL;

            if (!cauldron.isSplash()) {
                TileEntityAlchemyCauldron.message(player, "alchemia.brew.needs_gunpowder");
                return InteractionResult.CONSUME;
            }
            if (cauldron.getEffectList().stream().anyMatch(instance -> instance.getEffect().value() instanceof TriggerMobEffect trigger && !trigger.lingers())) {
                TileEntityAlchemyCauldron.message(player, "alchemia.brew.lingering_unstable");
                return InteractionResult.CONSUME;
            }
            cauldron.requestLingering(true);
            if (!level.isClientSide()) cauldron.journal().ingredient(player, heldStack);
        }

        if (heldItem.equals(Items.GLASS_BOTTLE) || heldItem.equals(Items.DRAGON_BREATH) || heldItem instanceof ItemPotionGlass) {
            if (cauldron.getWaterLevel() > 0) {
                PotionGlass vessel = heldItem instanceof ItemPotionGlass glass ? glass.glass() : null;
                ItemStack potion = draw(cauldron, vessel);
                Collection<MobEffectInstance> brewed = List.copyOf(cauldron.getEffectList());
                List<String> used = cauldron.script().recordedAdded(cauldron.getCourse().unstarted());
                List<String> steps = new ArrayList<>(cauldron.script().recordedSteps(cauldron.getCourse().unstarted()));
                if (vessel != null && vessel.hasEmptyItem()) steps.add(BrewRecipe.bottleStep(vessel));
                heldStack.shrink(1);
                Handover.give(player, potion);

                if (!level.isClientSide() && !cauldron.isSpoiled()) cauldron.journal().bottled(player, brewed, used, steps);

                settle(cauldron);
                return InteractionResult.CONSUME;
            }
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(cauldron, pos);
        }

        return InteractionResult.SUCCESS;
    }

    public static ItemStack bottle(TileEntityAlchemyCauldron cauldron, ItemStack vessel) {
        ItemStack potion = draw(cauldron, vessel.getItem() instanceof ItemPotionGlass glass ? glass.glass() : null);
        settle(cauldron);
        return potion;
    }

    public static ItemStack potion(TileEntityAlchemyCauldron cauldron) {
        ItemStack customPotion = new ItemStack(cauldron.resultType());
        if (cauldron.isSpoiled()) {
            customPotion.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(Optional.empty(), Optional.of(TileEntityAlchemyCauldron.SPOILED_COLOR), List.of(), Optional.of("ruined")));
            return customPotion;
        }
        if (cauldron.getEffectList().isEmpty()) {
            ItemStack unbrewed = cauldron.getBase().unbrewedStack();
            if (!unbrewed.isEmpty()) return unbrewed;

            customPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            return customPotion;
        }

        List<MobEffectInstance> brewed = new ArrayList<>();
        for (MobEffectInstance instance : cauldron.getEffectList()) {
            brewed.add(new MobEffectInstance(instance.getEffect(),
                    instance.getDuration() + cauldron.getDurationBonus(), instance.getAmplifier()));
        }

        customPotion.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(cauldron.getPotionColor()), brewed, Optional.of("alchemical")));
        if (cauldron.isRadiant()) customPotion.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        if (!cauldron.getBrewName().isEmpty()) {
            customPotion.set(DataComponents.CUSTOM_NAME,
                    Component.literal(cauldron.getBrewName()).withStyle(style -> style.withItalic(false)));
        }
        if (cauldron.getLevel() != null && !cauldron.getLevel().isClientSide()) cauldron.sync();
        return customPotion;
    }

    private static ItemStack draw(TileEntityAlchemyCauldron cauldron, @Nullable PotionGlass glass) {
        cauldron.takeWaterPortion();
        ItemStack potion = cauldron.getPotion();
        if (glass != null) PotionGlasses.apply(potion, glass);
        return potion;
    }

    private static void settle(TileEntityAlchemyCauldron cauldron) {
        Level level = cauldron.getLevel();
        if (level == null) return;
        BlockPos pos = cauldron.getBlockPos();

        cauldron.requestLingering(false);

        if (cauldron.getWaterLevel() < 1) {
            cauldron.resetEffectList();
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.SPLASH, pos);
                cauldron.sync();
            }
        }

        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            cauldron.sync();
        }
    }
}
