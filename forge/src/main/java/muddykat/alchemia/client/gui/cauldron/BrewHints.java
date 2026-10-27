package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.cauldron.PotencyPlan;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import javax.annotation.Nullable;

public final class BrewHints {

    private BrewHints() {
    }

    public static TileEntityAlchemyCauldron.BrewCheck check(BrewContext context) {
        return context.cauldron().checkBrew(context.brewAt());
    }

    public static boolean ready(BrewContext context) {
        return check(context) == TileEntityAlchemyCauldron.BrewCheck.READY;
    }

    public static @Nullable PotencyPlan preview(BrewContext context) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        if (!ready(context) || cauldron.gainAt(context.brewAt()) <= 0) return null;
        return cauldron.brewedWith(cauldron.effectAt(context.brewAt()));
    }

    public static @Nullable Component rotationNote(BrewContext context) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        PotencyPlan preview = preview(context);
        if (preview == null) return null;

        MobEffect brewing = preview.effects().get(preview.effects().size() - 1).getEffect();
        Component note = null;
        for (MobEffectInstance instance : cauldron.getEffectList()) {
            if (instance.getEffect().equals(brewing)) continue;

            int left = 0;
            for (MobEffectInstance kept : preview.effects()) {
                if (kept.getEffect().equals(instance.getEffect())) left = kept.getAmplifier() + 1;
            }
            if (left > instance.getAmplifier()) continue;

            Component name = instance.getEffect().getDisplayName();
            Component part = left == 0
                    ? Component.translatable("alchemia.gui.brew.pushed", name)
                    : Component.translatable("alchemia.gui.brew.fades", name, left);
            note = note == null ? part : Component.empty().append(note).append(", ").append(part);
        }

        int lostRedstone = cauldron.redstoneCharges() - preview.redstone();
        if (lostRedstone > 0) {
            Component part = Component.translatable("alchemia.gui.brew.pushed_redstone", lostRedstone);
            note = note == null ? part : Component.empty().append(note).append(", ").append(part);
        }
        return note;
    }

    public static @Nullable Component heldBack(BrewContext context) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        PotionMap.PotionEffectPosition raw = cauldron.rawEffectAt(context.brewAt());
        PotionMap.PotionEffectPosition capped = cauldron.effectAt(context.brewAt());

        if (raw.getEffect() == null || raw.getStrength() <= capped.getStrength()) return null;
        return context.knowledge().levelHint(raw.getRecipe(), capped.getStrength() + 2);
    }
}
