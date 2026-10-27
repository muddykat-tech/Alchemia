package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.guide.GuideShelf;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.MapReveals;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WhirlNotes;
import muddykat.alchemia.common.potion.Whirlpool;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class CauldronJournal {

    private final TileEntityAlchemyCauldron cauldron;
    private @Nullable PathPoint revealedAt;
    private @Nullable UUID revealedBy;

    public CauldronJournal(TileEntityAlchemyCauldron cauldron) {
        this.cauldron = cauldron;
    }

    public GuideShelf shelf(@Nullable Player player) {
        return GuideShelf.around(player, cauldron.getLevel(), cauldron.getBlockPos());
    }

    public void ingredient(@Nullable Player player, ItemStack used) {
        GuideShelf shelf = shelf(player);
        IngredientDiscovery.record(shelf, used);
        shelf.settle(cauldron.getBlockPos());
    }

    public void bottled(Player player, Collection<MobEffectInstance> brewed, List<String> used, List<String> steps) {
        Level level = cauldron.getLevel();
        BlockPos pos = cauldron.getBlockPos();
        GuideShelf shelf = shelf(player);

        BrewRecipeBook.Recorded recorded = BrewRecipeBook.record(shelf,
                BrewRecipeBook.brewed(cauldron.getBase(), brewed, used, steps, cauldron.getBrewName()));
        boolean discovered = RecipeDiscovery.record(shelf, brewed) > 0;
        shelf.settle(pos);

        if (recorded == BrewRecipeBook.Recorded.WRITTEN) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (discovered) {
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.4F, 1.6F);
        }
        if (recorded == BrewRecipeBook.Recorded.NO_PAGES) {
            TileEntityAlchemyCauldron.message(player, "alchemia.guide.no_pages");
        }
    }

    public void brewed(@Nullable Player player, MobEffect effect) {
        if (cauldron.getLevel() == null) return;

        GuideShelf shelf = shelf(player);
        AlchemiaEffect recipe = RecipeDiscovery.byEffect(effect);
        boolean recorded = RecipeDiscovery.record(shelf, List.of(new MobEffectInstance(effect))) > 0;
        boolean revealed = recipe != null && WorldEffects.reveal(shelf, recipe, cauldron.heldPotency(effect));
        shelf.settle(cauldron.getBlockPos());
        if (recorded || revealed) writeSound(player);
    }

    public void reached(@Nullable Player player) {
        if (cauldron.getLevel() == null || !PotionMap.isReady()) return;

        PathPoint at = cauldron.getBrewPosition();
        PotionMap.MapEntry entry = PotionMap.get(cauldron.getBase()).nearest(at.x(), at.y(), PotionMap.EFFECT_RADIUS);
        if (entry == null || WorldEffects.requirement(entry.effect().getRecipe()) == null) return;

        GuideShelf shelf = shelf(player);
        boolean noted = WorldEffects.note(shelf, entry.effect().getRecipe());
        shelf.settle(cauldron.getBlockPos());
        if (noted) writeSound(player);
    }

    public void reveal(@Nullable Player player) {
        Level level = cauldron.getLevel();
        if (level == null || level.isClientSide() || player == null || cauldron.getWaterLevel() <= 0 || !PotionMap.isReady()
                || !Configuration.fogOfWar()) return;

        PathPoint at = cauldron.getBrewPosition();
        if (revealedAt != null && player.getUUID().equals(revealedBy) && revealedAt.distanceTo(at) < MapReveals.CELL) return;
        revealedAt = at;
        revealedBy = player.getUUID();

        GuideShelf shelf = shelf(player);
        if (MapReveals.reveal(shelf, cauldron.getBase(), PotionMap.get(cauldron.getBase()), at)) shelf.settleQuietly(cauldron.getBlockPos());
    }

    public void exit(Whirlpool pool, @Nullable Player player) {
        if (cauldron.getLevel() == null) return;

        GuideShelf shelf = shelf(player);
        boolean noted = WhirlNotes.reveal(shelf, pool.mark(cauldron.getBase()));
        shelf.settle(cauldron.getBlockPos());
        if (noted) writeSound(player);
    }

    private void writeSound(@Nullable Player player) {
        Level level = cauldron.getLevel();
        if (level == null || player == null) return;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
