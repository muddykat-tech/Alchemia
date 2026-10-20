package muddykat.alchemia.common.items;

import muddykat.alchemia.common.guide.GuideShelf;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.List;

public class ItemAlchemistNotes extends Item {

    public static final String ID = "alchemist_notes";
    private static final int GLYPHS = 16;

    public ItemAlchemistNotes(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack notes = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(notes);

        BlockPos at = player.blockPosition();
        GuideShelf shelf = GuideShelf.around(player, level, at);
        if (shelf.isEmpty()) {
            message(player, "alchemia.notes.no_guide");
            return InteractionResultHolder.fail(notes);
        }

        PotionEnum secret = unnoted(WorldEffects.notesIn(shelf.iterator().next()), level);
        if (secret == null) {
            message(player, "alchemia.notes.known");
            return InteractionResultHolder.fail(notes);
        }

        WorldEffects.note(shelf, secret);
        shelf.settle(at);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 0.7F, 1.2F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getEyeY(), player.getZ(), GLYPHS, 0.3, 0.3, 0.3, 0.5);
        }
        if (!player.getAbilities().instabuild) notes.shrink(1);
        return InteractionResultHolder.consume(notes);
    }

    private static PotionEnum unnoted(Set<String> notes, Level level) {
        List<PotionEnum> hidden = new ArrayList<>();
        for (PotionEnum potion : PotionEnum.values()) {
            if (WorldEffects.requirement(potion) != null && !WorldEffects.wakeVisible(notes, potion)) hidden.add(potion);
        }
        return hidden.isEmpty() ? null : hidden.get(level.getRandom().nextInt(hidden.size()));
    }

    private static void message(Player player, String key) {
        if (player instanceof ServerPlayer server) server.displayClientMessage(Component.translatable(key), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("alchemia.tooltip.notes").withStyle(ChatFormatting.GRAY));
        tooltips.add(Component.translatable("alchemia.tooltip.notes.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
