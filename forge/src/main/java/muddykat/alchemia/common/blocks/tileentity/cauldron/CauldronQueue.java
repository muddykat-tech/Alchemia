package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.potion.BrewModifiers;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

public final class CauldronQueue {

    private final TileEntityAlchemyCauldron cauldron;
    private long lastApply = Long.MIN_VALUE;
    private long lastToolSound = -1000L;

    public CauldronQueue(TileEntityAlchemyCauldron cauldron) {
        this.cauldron = cauldron;
    }

    public InteractionResult queueActive(Player player) {
        Level level = cauldron.getLevel();
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack active = cauldron.getActiveIngredient();
        BrewModifiers modifier = BrewModifiers.of(active);
        if (modifier != null) return addModifier(level, player, active, modifier);

        if (!TileEntityAlchemyCauldron.isBrewingInput(active)) {
            TileEntityAlchemyCauldron.message(player, "alchemia.brew.no_ingredient");
            return InteractionResult.FAIL;
        }
        if (cauldron.queueFull()) {
            TileEntityAlchemyCauldron.message(player, "alchemia.brew.queue_full");
            return InteractionResult.FAIL;
        }

        if (!cauldron.plan(active)) return InteractionResult.FAIL;
        cauldron.journal().ingredient(player, active);

        active.shrink(1);
        cauldron.inventory().setActive(active);

        BlockPos pos = cauldron.getBlockPos();
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.4F, 1.5F);
        cauldron.setChanged();
        cauldron.sync();
        return InteractionResult.SUCCESS;
    }

    public InteractionResult unqueue(Player player, int index) {
        Level level = cauldron.getLevel();
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;
        if (!cauldron.getCourse().canRemove(index)) return InteractionResult.FAIL;

        List<ItemStack> planned = cauldron.getCourse().plannedStacks();
        String entry = BrewScript.encodeOf(planned.get(index));
        int occurrence = 0;
        for (int later = index; later < planned.size(); later++) {
            if (entry != null && entry.equals(BrewScript.encodeOf(planned.get(later)))) occurrence++;
        }

        ItemStack removed = cauldron.getCourse().remove(index);
        if (entry != null) cauldron.script().forget(entry, occurrence);

        if (!player.getInventory().add(removed)) {
            Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), removed);
        }

        cauldron.getCourse().compact(PotionMap.get(cauldron.getBase()));
        cauldron.refreshAlignment();
        cauldron.setChanged();
        cauldron.sync();
        return InteractionResult.SUCCESS;
    }

    public boolean applyTool(Player player, boolean fast) {
        if (!(cauldron.getLevel() instanceof ServerLevel server)) return false;

        ItemStack held = cauldron.getTool();
        IngredientTool tool = IngredientTool.of(held);
        if (tool == null) return false;

        ItemStack active = cauldron.getActiveIngredient();
        if (!tool.canApply(active)) return false;

        long now = server.getGameTime();
        if (now < lastApply + Math.max(1, tool.ticksPerStep() / (fast ? TileEntityAlchemyCauldron.FAST_TOOL_DIVISOR * 2 : 2))) return false;
        lastApply = now;

        tool.applyStep(active, held, server, player);
        cauldron.inventory().setActive(active);
        cauldron.inventory().setTool(held);

        if (!tool.canApply(active)) {
            tool.playDone(server, cauldron.getBlockPos());
            lastToolSound = now;
        } else if (now - lastToolSound >= tool.soundGap()) {
            tool.playStep(server, cauldron.getBlockPos());
            lastToolSound = now;
        }
        cauldron.setChanged();
        cauldron.sync();
        return true;
    }

    private InteractionResult addModifier(Level level, Player player, ItemStack active, BrewModifiers modifier) {
        String refusal = cauldron.modifierRefusal(modifier);
        if (refusal != null) {
            TileEntityAlchemyCauldron.message(player, refusal);
            return InteractionResult.FAIL;
        }
        if (!cauldron.applyModifier(modifier)) return InteractionResult.FAIL;
        cauldron.journal().ingredient(player, active);

        active.shrink(1);
        cauldron.inventory().setActive(active);

        BlockPos pos = cauldron.getBlockPos();
        if (modifier == BrewModifiers.IMBUEMENT && active.getItem() != Imbuements.bark()) {
            level.playSound(null, pos, SoundEvents.WET_GRASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.1F);
        } else if (modifier == BrewModifiers.IMBUEMENT) {
#if MC_VER >= 26300
            level.playSound(null, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 0.7F, 1.3F);
#else
            level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.7F, 1.3F);
#endif
        } else if (modifier == BrewModifiers.GLOWSTONE) {
            level.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.5F, 1.8F);
        } else if (modifier == BrewModifiers.GUNPOWDER) {
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 1.4F);
        } else {
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 0.8F);
        }
        level.gameEvent(null, GameEvent.SPLASH, pos);
        cauldron.setChanged();
        cauldron.sync();
        return InteractionResult.SUCCESS;
    }
}
