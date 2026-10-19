package muddykat.alchemia.common.items;

import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.guide.GuideShelf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class ItemAlchemyTablet extends Item {

    public static final String ID = "alchemy_tablet";
    private static final int SHARDS = 14;
    private static final int GLYPHS = 24;

    public ItemAlchemyTablet(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack tablet = player.getItemInHand(hand);
        BlockPos at = player.blockPosition();
        GuideShelf shelf = GuideShelf.around(player, level, at);
        ItemStack primary = shelf.isEmpty() ? ItemStack.EMPTY : shelf.iterator().next();
        if (primary.isEmpty()) {
            message(player, "alchemia.tablet.no_guide");
            return InteractionResult.FAIL;
        }

        MagnumOpus.Stage stage = GreatWork.next(primary);
        if (stage == null) {
            message(player, "alchemia.tablet.known");
            return InteractionResult.FAIL;
        }

        for (ItemStack guide : shelf) GreatWork.learn(guide, List.of(stage.result()));
        shelf.settle(at);

        crack(level, player, tablet);
        tablet.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    private static void crack(Level level, Player player, ItemStack tablet) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DECORATED_POT_SHATTER, SoundSource.PLAYERS, 0.9F, 1.15F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.75F);
        if (!(level instanceof ServerLevel server)) return;

        double x = player.getX();
        double y = player.getEyeY() - 0.35;
        double z = player.getZ();
        double reach = 0.55;
        double ax = x - Math.sin(Math.toRadians(player.getYRot())) * reach;
        double az = z + Math.cos(Math.toRadians(player.getYRot())) * reach;
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, tablet.getItem()), ax, y, az, SHARDS, 0.12, 0.1, 0.12, 0.08);
        server.sendParticles(ParticleTypes.ENCHANT, ax, y + 0.2, az, GLYPHS, 0.3, 0.35, 0.3, 0.6);
    }

    private static void message(Player player, String key) {
        if (player instanceof ServerPlayer server) server.sendOverlayMessage(Component.translatable(key));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltips, TooltipFlag flag) {
        tooltips.accept(Component.translatable("alchemia.tooltip.tablet").withStyle(ChatFormatting.GRAY));
        tooltips.accept(Component.translatable("alchemia.tooltip.tablet.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
