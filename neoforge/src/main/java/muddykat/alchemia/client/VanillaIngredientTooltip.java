package muddykat.alchemia.client;

import muddykat.alchemia.Alchemia;
import com.mojang.datafixers.util.Either;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.GuideAccess;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.utility.TextUtils;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class VanillaIngredientTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!GuideAccess.reading()) return;

        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(event.getItemStack());
        if (drift == null) return;

        event.getToolTip().add(TextUtils.brewingSource());

        event.getToolTip().add(Component.translatable(
                drift.lure() ? "alchemia.tooltip.lure" : "alchemia.tooltip.bearing",
                drift.target().getEffect().value().getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));

        int crush = ItemIngredient.crushOf(event.getItemStack());
        if (crush < Ingredients.MAX_CRUSH_UNITS) {
            event.getToolTip().add(Component.translatable("alchemia.tooltip.grind", crush, Ingredients.MAX_CRUSH_UNITS).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            event.getToolTip().add(Component.translatable("alchemia.tooltip.ground").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (drift.lure()) {
            event.getToolTip().add(Component.translatable("alchemia.tooltip.lure.hint").withStyle(ChatFormatting.DARK_GRAY));
            if (crush > 0) {
                event.getToolTip().add(Component.translatable("alchemia.tooltip.lure.strength",
                        String.format(java.util.Locale.ROOT, "%.1f", VanillaIngredients.grindScale(crush))).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            event.getToolTip().add(Component.translatable("alchemia.tooltip.path").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @SubscribeEvent
    public static void onModifierTooltip(ItemTooltipEvent event) {
        if (!GuideAccess.reading()) return;

        var stack = event.getItemStack();
        String key = stack.is(net.minecraft.world.item.Items.REDSTONE) ? "alchemia.tooltip.use.redstone"
                : stack.is(net.minecraft.world.item.Items.GUNPOWDER) ? "alchemia.tooltip.use.gunpowder"
                : stack.is(net.minecraft.world.item.Items.GLOWSTONE_DUST) ? "alchemia.tooltip.use.glowstone"
                : stack.is(net.minecraft.world.item.Items.DRAGON_BREATH) ? "alchemia.tooltip.use.dragon_breath"
                : null;
        if (key == null) {
            var attuner = muddykat.alchemia.common.potion.Imbuements.of(stack);
            if (attuner == null || attuner.biome() != null || stack.getItem() instanceof muddykat.alchemia.common.items.ItemBark || attuner.blank()) return;
            event.getToolTip().add(TextUtils.brewingSource());
            event.getToolTip().add(Component.translatable("alchemia.tooltip.use.attune", attuner.region()).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        event.getToolTip().add(TextUtils.brewingSource());
        event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
    }

    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (!GuideAccess.reading()) return;

        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(event.getItemStack());
        if (drift == null || drift.lure() || !PotionMap.isReady()) return;

        PotionMap map = PotionMap.get(BrewBases.defaultBase());
        List<PathPoint> offsets = map.fixedDriftOffsets(drift.target(), Ingredients.MAX_CRUSH_UNITS);
        if (offsets.size() < 2) return;

        double reached = PathGeometry.length(map.fixedDriftOffsets(drift.target(), ItemIngredient.crushOf(event.getItemStack())));
        event.getTooltipElements().add(Either.right(new IngredientPathTooltip(
                offsets, reached, false, Component.translatable("alchemia.tooltip.label.path"))));
    }
}
