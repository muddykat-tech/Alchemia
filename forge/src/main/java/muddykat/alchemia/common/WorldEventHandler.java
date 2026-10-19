package muddykat.alchemia.common;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.Alchemia;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedPotions;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;

public class WorldEventHandler {

    private static final ResourceLocation CAULDRON_ADVANCEMENT =
            new ResourceLocation(Alchemia.MODID, "cauldron");

    private static void awardCauldronAdvancement(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Advancement advancement = serverPlayer.level().getServer().getAdvancements().getAdvancement(CAULDRON_ADVANCEMENT);
        if (advancement == null) return;

        for (String criterion : advancement.getCriteria().keySet()) {
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

    @SubscribeEvent
    public static void drinkPhial(LivingEntityUseItemEvent.Start event) {
        if (!event.getItem().is(Items.POTION)) return;
        PotionGlass glass = PotionGlasses.of(event.getItem());
        if (glass.hasEmptyItem()) event.setDuration(Math.max(1, Math.round(glass.drinkSeconds() * 20.0F)));
    }

    @SubscribeEvent
    public static void returnPhial(LivingEntityUseItemEvent.Finish event) {
        PotionGlass glass = PotionGlasses.of(event.getItem());
        if (!glass.hasEmptyItem() || !event.getResultStack().is(Items.GLASS_BOTTLE)) return;
        event.setResultStack(new ItemStack(PotionGlasses.emptyItem(glass), event.getResultStack().getCount()));
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
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
            } else if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
            return;
        }
    }
}
