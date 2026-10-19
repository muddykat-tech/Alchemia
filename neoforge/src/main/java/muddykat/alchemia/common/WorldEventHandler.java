package muddykat.alchemia.common;

import net.minecraft.world.entity.item.ItemEntity;
import java.util.List;
import java.util.ArrayList;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.common.utility.Handover;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.Alchemia;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedPotions;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class WorldEventHandler {

    private static final Identifier CAULDRON_ADVANCEMENT =
            Identifier.fromNamespaceAndPath(Alchemia.MODID, "cauldron");

    private static void awardCauldronAdvancement(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        AdvancementHolder advancement = serverPlayer.level().getServer().getAdvancements().get(CAULDRON_ADVANCEMENT);
        if (advancement == null) return;

        for (String criterion : advancement.value().criteria().keySet()) {
            serverPlayer.getAdvancements().award(advancement, criterion);
        }
    }

    @SubscribeEvent
    public static void cauldronCreation(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        BlockState block = event.getPlacedBlock();
        if (!(block.getBlock() instanceof CauldronBlock)) return;

        BlockState blockBelow = event.getPlacedAgainst();
        if (blockBelow.is(Blocks.CAMPFIRE)) {
            BlockPos pos = event.getPos();
            Direction facing = blockBelow.hasProperty(CampfireBlock.FACING)
                    ? blockBelow.getValue(CampfireBlock.FACING).getOpposite()
                    : Direction.NORTH;

            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 1 | 8);
            event.getLevel().setBlock(pos.below(), BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get()
                    .defaultBlockState().setValue(BlockAlchemyCauldron.FACING, facing), 1);

            awardCauldronAdvancement(player);
        }
    }

    @SubscribeEvent
    public static void placePotion(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isSecondaryUseActive()) return;

        InteractionResult result = BlockPlacedPotions.place(event.getLevel(), event.getPos(),
                event.getHitVec().getDirection(), player, event.getHand());
        if (result == InteractionResult.PASS) return;

        event.setCanceled(true);
        event.setCancellationResult(result);
    }

    private record PendingStrip(ServerLevel level, BlockPos pos, Block log, Direction face) {
    }

    private static final List<PendingStrip> PENDING_STRIPS = new ArrayList<>();

    private static boolean stripped(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().startsWith("stripped_");
    }

    @SubscribeEvent
    public static void watchStrip(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockState state = level.getBlockState(event.getPos());
        if (!state.is(BlockTags.LOGS) || stripped(state)) return;
        PENDING_STRIPS.add(new PendingStrip(level, event.getPos().immutable(), state.getBlock(), event.getFace() == null ? Direction.UP : event.getFace()));
    }

    @SubscribeEvent
    public static void dropBark(LevelTickEvent.Post event) {
        if (PENDING_STRIPS.isEmpty() || !(event.getLevel() instanceof ServerLevel level)) return;
        PENDING_STRIPS.removeIf(pending -> {
            if (pending.level() != level) return false;
            BlockState now = level.getBlockState(pending.pos());
            if (now.getBlock() != pending.log() && stripped(now)) {
                level.getBiome(pending.pos()).unwrapKey().ifPresent(biome -> {
                    BlockPos at = pending.pos().relative(pending.face());
                    ItemEntity bark = new ItemEntity(level, at.getX() + 0.5, at.getY() + 0.25, at.getZ() + 0.5, ItemBark.gathered(biome));
                    bark.setDefaultPickUpDelay();
                    level.addFreshEntity(bark);
                });
            }
            return true;
        });
    }

    @SubscribeEvent
    public static void onRecipeUpdate(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(event.getCrafting().getItem() instanceof ItemIngredientSeed)) return;

        Container items = event.getInventory();
        for (int i = 0; i < items.getContainerSize(); i++) {
            ItemStack found = items.getItem(i);
            if (!found.is(Items.SHEARS)) continue;

            ItemStack returned = found.copyWithCount(1);
            returned.setDamageValue(returned.getDamageValue() + 1);
            if (returned.getDamageValue() >= returned.getMaxDamage()) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
            } else {
                Handover.give(player, returned);
            }
            return;
        }
    }
}
