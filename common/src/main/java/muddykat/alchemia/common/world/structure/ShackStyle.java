package muddykat.alchemia.common.world.structure;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
#if MC_VER >= 26100
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

import java.util.Locale;

public enum ShackStyle {
    OAK(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.OAK_FENCE,
            Blocks.OAK_TRAPDOOR, "oak_shelf", Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, "red_carpet", "red_bed",
            Blocks.MOSS_CARPET, Blocks.VINE, Blocks.COARSE_DIRT, "leaf_litter", false),
    SPRUCE(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE,
            Blocks.SPRUCE_TRAPDOOR, "spruce_shelf", Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, "brown_carpet", "brown_bed",
            Blocks.MOSS_CARPET, null, Blocks.PODZOL, "fern", false),
    BIRCH(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_STAIRS, Blocks.BIRCH_SLAB, Blocks.BIRCH_FENCE,
            Blocks.BIRCH_TRAPDOOR, "birch_shelf", Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, "yellow_carpet", "white_bed",
            Blocks.MOSS_CARPET, Blocks.VINE, Blocks.COARSE_DIRT, "leaf_litter", false),
    DARK_OAK(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB,
            Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_TRAPDOOR, "dark_oak_shelf", Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE,
            "green_carpet", "green_bed", Blocks.MOSS_CARPET, Blocks.VINE, Blocks.ROOTED_DIRT, "leaf_litter", false),
#if MC_VER >= 26100
    PALE_OAK(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_STAIRS, Blocks.PALE_OAK_SLAB,
            Blocks.PALE_OAK_FENCE, Blocks.PALE_OAK_TRAPDOOR, "pale_oak_shelf", Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
            "light_gray_carpet", "light_gray_bed", Blocks.PALE_MOSS_CARPET, Blocks.PALE_HANGING_MOSS, Blocks.ROOTED_DIRT,
            "pale_moss_carpet", false),
#endif
    SWAMP(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_SLAB,
            Blocks.MANGROVE_FENCE, Blocks.MANGROVE_TRAPDOOR, "mangrove_shelf", Blocks.MUD_BRICKS, Blocks.PACKED_MUD,
            "green_carpet", "lime_bed", Blocks.MOSS_CARPET, Blocks.VINE, Blocks.MUD, "lily_pad", true);

    private final Block log;
    private final Block strippedLog;
    private final Block planks;
    private final Block stairs;
    private final Block slab;
    private final Block fence;
    private final Block trapdoor;
    private final String shelf;
    private final Block stone;
    private final Block wornStone;
    private final String carpet;
    private final String bed;
    private final Block overgrowth;
    private final @Nullable Block creeper;
    private final Block soil;
    private final String litter;
    private final boolean raised;

    ShackStyle(Block log, Block strippedLog, Block planks, Block stairs, Block slab, Block fence, Block trapdoor, String shelf, Block stone,
               Block wornStone, String carpet, String bed, Block overgrowth, @Nullable Block creeper, Block soil, String litter, boolean raised) {
        this.log = log;
        this.strippedLog = strippedLog;
        this.planks = planks;
        this.stairs = stairs;
        this.slab = slab;
        this.fence = fence;
        this.trapdoor = trapdoor;
        this.shelf = shelf;
        this.stone = stone;
        this.wornStone = wornStone;
        this.carpet = carpet;
        this.bed = bed;
        this.overgrowth = overgrowth;
        this.creeper = creeper;
        this.soil = soil;
        this.litter = litter;
        this.raised = raised;
    }

    public static ShackStyle of(Holder<Biome> biome) {
        if (biome.is(Biomes.SWAMP)) return SWAMP;
#if MC_VER >= 26100
        if (biome.is(Biomes.PALE_GARDEN)) return PALE_OAK;
#endif
        if (biome.is(Biomes.DARK_FOREST)) return DARK_OAK;
        if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) return BIRCH;
        if (biome.is(BiomeTags.IS_TAIGA) || biome.is(Biomes.GROVE)) return SPRUCE;
        return OAK;
    }

    public static ShackStyle byName(String name) {
        for (ShackStyle style : values()) {
            if (style.id().equals(name)) return style;
        }
        return OAK;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Block log() {
        return log;
    }

    public Block strippedLog() {
        return strippedLog;
    }

    public Block planks() {
        return planks;
    }

    public Block stairs() {
        return stairs;
    }

    public Block slab() {
        return slab;
    }

    public Block fence() {
        return fence;
    }

    public Block trapdoor() {
        return trapdoor;
    }

    public @Nullable Block shelf() {
        return ShackShell.vanilla(shelf, null);
    }

    public Block stone() {
        return stone;
    }

    public Block wornStone() {
        return wornStone;
    }

    public Block carpet() {
        return ShackShell.vanilla(carpet, Blocks.MOSS_CARPET);
    }

    public Block bed() {
        return ShackShell.vanilla(bed, Blocks.HAY_BLOCK);
    }

    public Block overgrowth() {
        return overgrowth;
    }

    public @Nullable Block creeper() {
        return creeper;
    }

    public Block soil() {
        return soil;
    }

    public @Nullable Block litter() {
        return ShackShell.vanilla(litter, null);
    }

    public boolean raised() {
        return raised;
    }
}
