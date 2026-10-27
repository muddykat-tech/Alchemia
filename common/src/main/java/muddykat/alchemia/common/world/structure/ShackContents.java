package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedPotions;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedPotions;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
#if MC_VER >= 26100
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
#elif MC_VER >= 12100
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.storage.loot.LootTable;
#else
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
#endif

import java.util.ArrayList;
import java.util.List;

final class ShackContents {

#if MC_VER >= 12100
    static final ResourceKey<LootTable> SHACK_CHEST = table("chests/forgotten_alchemy_shack");
    static final ResourceKey<LootTable> CELLAR_CHEST = table("chests/forgotten_alchemy_cellar");
    static final ResourceKey<LootTable> BRUSHED = table("archaeology/forgotten_alchemy_shack");
    static final ResourceKey<LootTable> TABLET = table("archaeology/forgotten_alchemy_shack_tablet");
#else
    static final ResourceLocation SHACK_CHEST = table("chests/forgotten_alchemy_shack");
    static final ResourceLocation CELLAR_CHEST = table("chests/forgotten_alchemy_cellar");
    static final ResourceLocation BRUSHED = table("archaeology/forgotten_alchemy_shack");
    static final ResourceLocation TABLET = table("archaeology/forgotten_alchemy_shack_tablet");
#endif

    static final int SPOT_CAVE_LOW = 0;
    static final int SPOT_CAVE_HIGH = 1;
    static final int SPOT_SAND = 2;
    static final int SPOT_PATH = 3;

    private static final int SHELF_SLOTS = 6;
    private static final Ingredients[] DRIED = {Ingredients.Lifeleaf, Ingredients.Druids_Rosemary, Ingredients.Healers_Heather,
            Ingredients.Coldleaf, Ingredients.Goodberry, Ingredients.Thornstick, Ingredients.Evergreen_Fern};
    private static final Ingredients[] STORED = {Ingredients.Terraria, Ingredients.Grasping_Root, Ingredients.Witch_Mushroom,
            Ingredients.Rainbow_Cap, Ingredients.Dreambeet};

    private final ShackCanvas canvas;
    private final int tablet;
    private final ShackBrews brews;

    ShackContents(ShackCanvas canvas, ShackStyle style) {
        this.canvas = canvas;
        this.tablet = canvas.pick(0, 0, 0, 90, style.raised() ? SPOT_PATH : SPOT_PATH + 1);
        this.brews = new ShackBrews(canvas);
    }

#if MC_VER >= 26100
    private static ResourceKey<LootTable> table(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(Alchemia.MODID, path));
    }
#elif MC_VER >= 12100
    private static ResourceKey<LootTable> table(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, path));
    }
#else
    private static ResourceLocation table(String path) {
        return new ResourceLocation(Alchemia.MODID, path);
    }
#endif

    void brushable(int x, int y, int z, Block block, int spot) {
        canvas.set(x, y, z, block);
        if (canvas.entity(x, y, z) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(spot == tablet ? TABLET : BRUSHED, canvas.hash(x, y, z, 92));
        }
    }

#if MC_VER >= 12100
    void container(int x, int y, int z, BlockState state, ResourceKey<LootTable> table) {
        canvas.set(x, y, z, state);
        if (canvas.entity(x, y, z) instanceof RandomizableContainer container) {
            container.setLootTable(table, canvas.hash(x, y, z, 93));
        }
    }
#else
    void container(int x, int y, int z, BlockState state, ResourceLocation table) {
        canvas.set(x, y, z, state);
        if (canvas.entity(x, y, z) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(table, canvas.hash(x, y, z, 93));
        }
    }
#endif

    void potions(int x, int y, int z, List<ItemStack> stacks) {
        if (stacks.isEmpty() || !(BlockRegistry.BLOCK_REGISTRY.get(BlockPlacedPotions.ID).get() instanceof BlockPlacedPotions block)) return;
        canvas.set(x, y, z, block.stateFor(stacks));
        if (canvas.entity(x, y, z) instanceof TileEntityPlacedPotions placed) placed.setContents(stacks);
    }

    void shelf(int x, int y, int z, Block shelf, Direction facing, List<ItemStack> items) {
        canvas.set(x, y, z, shelf.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
#if MC_VER >= 26100
        if (!(canvas.entity(x, y, z) instanceof ShelfBlockEntity display)) return;
        for (int slot = 0; slot < Math.min(items.size(), ShelfBlockEntity.MAX_ITEMS); slot++) display.getItems().set(slot, items.get(slot));
#endif
    }

    ItemStack special(int slot) {
        return brews.special(slot);
    }

    ItemStack treasureVision() {
        return brews.treasureVision();
    }

    static PotionGlass glass(ShackCanvas canvas, int slot) {
        PotionGlass[] glasses = PotionGlass.values();
        return glasses[canvas.pick(slot, 2, 0, 95, glasses.length)];
    }

    ItemStack vessel(int slot) {
        PotionGlass glass = glass(canvas, slot + 100);
        return glass.hasEmptyItem() ? new ItemStack(PotionGlasses.emptyItem(glass)) : new ItemStack(Items.GLASS_BOTTLE);
    }

    List<ItemStack> dried(int slot) {
        return stocked(slot, DRIED);
    }

    List<ItemStack> stored(int slot) {
        return stocked(slot, STORED);
    }

    private List<ItemStack> stocked(int slot, Ingredients[] pool) {
        List<ItemStack> items = new ArrayList<>();
        for (int index = 0; index < SHELF_SLOTS; index++) {
            float roll = canvas.chance(slot, index, 0, 96);
            if (roll < 0.25F) {
                items.add(ItemStack.EMPTY);
            } else if (roll < 0.55F) {
                items.add(vessel(slot * 3 + index));
            } else {
                items.add(ingredient(pool[canvas.pick(slot, index, 0, 97, pool.length)], 1 + canvas.pick(slot, index, 0, 98, 3)));
            }
        }
        return items;
    }

    private static ItemStack ingredient(Ingredients ingredient, int count) {
        var item = ItemRegistry.ITEM_REGISTRY.get(ingredient.getRegistryName());
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
    }
}
