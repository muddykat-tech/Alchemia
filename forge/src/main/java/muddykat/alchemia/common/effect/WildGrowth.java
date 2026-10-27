package muddykat.alchemia.common.effect;

import muddykat.alchemia.common.blocks.BlockIngredientWild;
import muddykat.alchemia.common.blocks.helper.GrowthClimate;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
#if MC_VER >= 26300
import net.minecraft.world.level.block.BonemealSource;
#endif
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.GenerationStep;
#if MC_VER >= 26300
import net.minecraft.world.level.levelgen.feature.Feature;
#else
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
#endif
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WildGrowth {

    public static final int MAX_STRENGTH = 4;
    private static final float WALK_CONVERT = 0.15F;
    private static final float SPLASH_CONVERT = 0.3F;
    private static final float SPLASH_CONVERT_PER_LEVEL = 0.15F;
    private static final float SAND_SHARE = 0.17F;
    private static final float NEIGHBOUR_PULL = 0.55F;
    private static final float LONE_SEED = 0.5F;
    private static final int AURA_TICKS = 5;
    private static final float AURA_RADIUS = 1.5F;
    private static final float AURA_RADIUS_PER_LEVEL = 0.5F;
    private static final int AURA_COLUMNS = 2;
    private static final float AURA_CONVERT = 0.2F;
    private static final float CACTUS_TREE_SPLASH = 0.15F;
    private static final float CACTUS_TREE_TICK = 0.02F;
    private static final float EDGE_FLOOR = 0.1F;
    private static final double SPLASH_CORE = 0.4;
    private static final float SPLASH_BLOOM = 0.12F;
    private static final int UNDERCOAT_DEPTH = 6;
    private static final float FIELD_BLOOM = 0.1F;
    private static final float PLANT_BONEMEAL = 0.3F;
    private static final float CROP_STEP = 0.35F;
    private static final float BONUS_HARVEST = 0.01F;
    private static final float BONUS_HARVEST_PER_LEVEL = 0.005F;
    private static final int[][] RING = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final float WALK_BLOOM = 0.18F;
    private static final float WALK_BLOOM_PER_LEVEL = 0.04F;
    private static final float FLOWER_SHARE = 0.6F;
    private static final float TALL_GRASS_SHARE = 0.3F;
    private static final float MATURE = 0.25F;
    private static final float MATURE_PER_LEVEL = 0.1F;
    private static final int MATURE_PASSES = 8;
    private static final float TREE_CHANCE = 0.01F;
    private static final int TREE_ATTEMPTS = 4;
    private static final double TREE_CLEARANCE = 6.0;
    private static final int CANOPY_REACH = 3;
    private static final int CANOPY_HEIGHT = 12;
    private static final List<Block> FLOWERS = List.of(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
            Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY);

    @FunctionalInterface
    private interface Planter {
        boolean place(ServerLevel level, BlockPos root, RandomSource random);
    }

    private WildGrowth() {
    }

    public static void walk(ServerLevel level, LivingEntity walker, int strength) {
        if (walker.isSpectator()) return;
        RandomSource random = walker.getRandom();
        BlockPos feet = walker.blockPosition();
        if (walker.tickCount % AURA_TICKS == 0) aura(level, feet, strength, random);
        if (!walker.onGround() || feet.equals(BlockPos.containing(walker.xo, walker.yo, walker.zo))) return;
        BlockPos ground = beneath(level, walker.getOnPos());
        if (random.nextFloat() < WALK_BLOOM + WALK_BLOOM_PER_LEVEL * strength) bloom(level, ground, strength, random);
    }

    private static void aura(ServerLevel level, BlockPos feet, int strength, RandomSource random) {
        float radius = AURA_RADIUS + AURA_RADIUS_PER_LEVEL * strength;
        for (int index = 0; index < AURA_COLUMNS + strength; index++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double reach = radius * random.nextDouble();
            BlockPos ground = surface(level, feet.offset((int) Math.round(Math.cos(angle) * reach), 0, (int) Math.round(Math.sin(angle) * reach)), 1, -3);
            if (ground == null) continue;
            convert(level, ground, strength, random, AURA_CONVERT * strength * falloff(reach / radius));
        }
    }

    public static void quicken(ServerLevel level, BlockPos centre, int radius, int count, RandomSource random) {
        for (int index = 0; index < count; index++) {
            BlockPos pos = centre.offset(random.nextInt(radius * 2 + 1) - radius, random.nextInt(6) - 2, random.nextInt(radius * 2 + 1) - radius);
            BlockState state = level.getBlockState(pos);
            if (!plant(state) || !state.isRandomlyTicking()) continue;
            if (wilden(level, pos, state)) state = level.getBlockState(pos);
            if (state.is(Blocks.CACTUS) && random.nextFloat() < CACTUS_TREE_TICK && transplant(level, pos, random)) continue;
            state.randomTick(level, pos, random);
            if (random.nextInt(4) == 0) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    public static void tend(ServerLevel level, BlockPos centre, float radius, int strength, int columns, RandomSource random) {
        for (int index = 0; index < columns; index++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double reach = radius * random.nextDouble();
            BlockPos ground = surface(level, centre.offset((int) Math.round(Math.cos(angle) * reach), 0, (int) Math.round(Math.sin(angle) * reach)), 3, -4);
            if (ground == null) continue;
            float weight = falloff(reach / radius);
            float chance = WALK_CONVERT * strength * weight;
            ripen(level, ground.above(), strength, random);
            convert(level, ground, strength, random, chance);
            undercoat(level, ground, strength, random, chance);
            if (random.nextFloat() < FIELD_BLOOM * weight) bloom(level, ground, strength, random);
        }
    }

    private static void ripen(ServerLevel level, BlockPos pos, int strength, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (wilden(level, pos, state)) state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || upper(state) || !GrowthClimate.allows(state, level, pos)) return;
        if (crop.isMaxAge(state)) {
            if (random.nextFloat() < BONUS_HARVEST + BONUS_HARVEST_PER_LEVEL * strength) bonus(level, pos, state);
            return;
        }
        if (random.nextFloat() >= CROP_STEP) return;
        IntegerProperty age = age(state);
        if (age != null && !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            level.setBlock(pos, state.setValue(age, Math.min(crop.getAge(state) + 1, crop.getMaxAge())), Block.UPDATE_CLIENTS);
        } else {
            state.randomTick(level, pos, random);
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 2, 0.3, 0.2, 0.3, 0.0);
    }

    private static void bonus(ServerLevel level, BlockPos pos, BlockState state) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null);
        Item seed = state.getBlock().asItem();
        ItemStack harvest = drops.stream().filter(stack -> !stack.is(seed)).findFirst()
                .orElse(drops.isEmpty() ? ItemStack.EMPTY : drops.get(0));
        if (harvest.isEmpty()) return;
        Block.popResource(level, pos, harvest.copyWithCount(1));
        level.sendParticles(ParticleTypeRegistry.WILD_LEAF.get(), pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0, 0.0, 0.12, 0.0, 1.0);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.6F, 1.3F);
    }

    private static boolean wilden(ServerLevel level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof BlockIngredientWild) || state.getValue(BlockIngredientWild.WILD)) return false;
        level.setBlock(pos, state.setValue(BlockIngredientWild.WILD, true), Block.UPDATE_ALL);
        level.sendParticles(ParticleTypeRegistry.WILD_LEAF.get(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 0, 0.0, 0.1, 0.0, 1.0);
        return true;
    }

    private static boolean upper(BlockState state) {
        return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
    }

    private static IntegerProperty age(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && property.getName().equals("age")) return integer;
        }
        return null;
    }

    public static void overflow(ServerLevel level, Vec3 at, int strength, RandomSource random) {
        overflow(level, at, strength, 2 + strength, random);
    }

    public static void overflow(ServerLevel level, Vec3 at, int strength, int radius, RandomSource random) {
        BlockPos centre = BlockPos.containing(at);
        float convertChance = SPLASH_CONVERT + SPLASH_CONVERT_PER_LEVEL * strength;
        List<int[]> columns = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) columns.add(new int[]{dx, dz});
            }
        }
        columns.sort(Comparator.comparingInt(column -> column[0] * column[0] + column[1] * column[1]));
        for (int[] column : columns) {
            int dx = column[0];
            int dz = column[1];
            float weight = core(Math.sqrt(dx * dx + dz * dz) / (radius + 0.5));
            float chance = weight >= 1.0F ? 1.0F : convertChance * weight;
            boolean passed = false;
            for (int dy = 3; dy >= -Math.max(4, radius); dy--) {
                BlockPos pos = centre.offset(dx, dy, dz);
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) continue;
                if (withered(level, state)) {
                    revive(level, pos, state, strength);
                    continue;
                }
                if (plant(state)) {
                    flourish(level, pos, state, strength, random);
                    continue;
                }
                if (state.getBlock() instanceof BushBlock) continue;
                if (!state.isCollisionShapeFullBlock(level, pos)) {
                    if (passed) break;
                    passed = true;
                    continue;
                }
                convert(level, pos, strength, random, chance);
                undercoat(level, pos, strength, random, chance);
                if (random.nextFloat() < SPLASH_BLOOM * weight) bloom(level, pos, strength, random);
                break;
            }
        }
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BONE_MEAL_USE, SoundSource.NEUTRAL, 1.2F, 0.8F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.NEUTRAL, 1.0F, 0.7F);
        for (int index = 0; index < 70; index++) {
            double reach = 0.3 + random.nextDouble() * radius;
            level.sendParticles(ParticleTypeRegistry.WILD_SWIRL.get(), at.x, at.y + random.nextDouble() * 0.6, at.z, 0, reach,
                    0.04 + random.nextDouble() * 0.09, (random.nextBoolean() ? 1.0 : -1.0) * (0.08 + random.nextDouble() * 0.12), 1.0);
        }
        for (int index = 0; index < 30; index++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            level.sendParticles(ParticleTypeRegistry.WILD_LEAF.get(), at.x, at.y + 0.3, at.z, 0, Math.cos(angle) * 0.25,
                    0.18 + random.nextDouble() * 0.2, Math.sin(angle) * 0.25, 1.0);
        }
    }

    private static void undercoat(ServerLevel level, BlockPos surface, int strength, RandomSource random, float chance) {
        for (int depth = 1; depth <= UNDERCOAT_DEPTH; depth++) {
            BlockPos pos = surface.below(depth);
            if (!level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) return;
            if (depth > 1 && !exposed(level, pos)) return;
            convert(level, pos, strength, random, chance);
        }
    }

    private static boolean exposed(ServerLevel level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos beside = pos.relative(side);
            if (!level.getBlockState(beside).isCollisionShapeFullBlock(level, beside)) return true;
        }
        return false;
    }

    private static void flourish(ServerLevel level, BlockPos pos, BlockState state, int strength, RandomSource random) {
        if (wilden(level, pos, state)) state = level.getBlockState(pos);
        if (state.is(Blocks.CACTUS) && random.nextFloat() < CACTUS_TREE_SPLASH && transplant(level, pos, random)) return;
        if (!(state.getBlock() instanceof BonemealableBlock)) {
            if (state.isRandomlyTicking()) {
                for (int pass = 0; pass < 3; pass++) level.getBlockState(pos).randomTick(level, pos, random);
            }
            return;
        }
        if (!(state.getBlock() instanceof CropBlock crop)) {
            if (random.nextFloat() >= PLANT_BONEMEAL) return;
            bonemeal(level, pos, random, 1);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.35, 0.35, 0.35, 0.0);
            return;
        }
        if (upper(state) || !GrowthClimate.allows(state, level, pos)) return;
        boolean mature = random.nextFloat() < MATURE + MATURE_PER_LEVEL * strength;
        IntegerProperty age = age(state);
        if (mature && age != null && !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            level.setBlockAndUpdate(pos, state.setValue(age, crop.getMaxAge()));
        } else {
            bonemeal(level, pos, random, mature ? MATURE_PASSES : 1);
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, mature ? 6 : 3, 0.35, 0.35, 0.35, 0.0);
    }

    private static void bonemeal(ServerLevel level, BlockPos pos, RandomSource random, int passes) {
        for (int pass = 0; pass < passes; pass++) {
            BlockState current = level.getBlockState(pos);
            if (!(current.getBlock() instanceof BonemealableBlock block) || !valid(block, level, pos, current)) break;
            perform(block, level, random, pos, current);
        }
    }

    private static boolean valid(BonemealableBlock block, ServerLevel level, BlockPos pos, BlockState state) {
#if MC_VER >= 26300
        return block.isValidBonemealTarget(level, pos, state, BonemealSource.MOB);
#else
        return block.isValidBonemealTarget(level, pos, state, false);
#endif
    }

    private static void perform(BonemealableBlock block, ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
#if MC_VER >= 26300
        block.performBonemeal(level, random, pos, state, BonemealSource.MOB);
#else
        block.performBonemeal(level, random, pos, state);
#endif
    }

    private static boolean revive(ServerLevel level, BlockPos pos, BlockState state, int strength) {
        WildConversion conversion = conversion(level, pos, state, strength, WildConversion.Kind.PLANT);
        if (conversion == null) return false;
        boolean doubled = state.getBlock() instanceof DoublePlantBlock;
        if (doubled && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) return false;
        BlockState alive = conversion.to().defaultBlockState();
        if (!alive.canSurvive(level, pos)) return false;
        BlockPos above = pos.above();
        boolean tall = alive.getBlock() instanceof DoublePlantBlock;
        BlockState top = level.getBlockState(above);
        if (tall && !top.isAir() && !(doubled && top.is(state.getBlock()))) return false;
        if (tall) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (doubled) level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            DoublePlantBlock.placeAt(level, alive, pos, Block.UPDATE_ALL);
        } else {
            if (doubled) level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlockAndUpdate(pos, alive);
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.0);
        level.sendParticles(ParticleTypeRegistry.WILD_LEAF.get(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 0, 0.0, 0.1, 0.0, 1.0);
        return true;
    }

    public static boolean plant(BlockState state) {
        if (state.is(Blocks.CACTUS) || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.NETHER_WART) || state.is(Blocks.VINE)
                || state.is(Blocks.CHORUS_FLOWER) || state.is(BlockTags.CROPS)) return true;
        return !state.canOcclude() && state.getBlock() instanceof BonemealableBlock;
    }

    public static boolean convert(ServerLevel level, BlockPos pos, int strength, RandomSource random, float chance) {
        BlockState state = level.getBlockState(pos);
        BlockPos coverPos = pos.above();
        BlockState cover = level.getBlockState(coverPos);
        boolean withered = withered(level, cover);
        WildConversion step = conversion(level, pos, state, strength, WildConversion.Kind.GROUND);
        if (step == null) return withered && random.nextFloat() < chance && revive(level, coverPos, cover, strength);
        if (step.to() instanceof SpreadingSnowyDirtBlock && (cover.canOcclude() || !cover.getFluidState().isEmpty())) return false;
        if (chance < 1.0F) {
            if (step.tier() == MAX_STRENGTH) chance *= SAND_SHARE;
            float lean = greener(level, pos, rank(level, state));
            chance = lean == 0.0F ? chance * LONE_SEED : chance + (1.0F - chance) * NEIGHBOUR_PULL * lean;
            if (random.nextFloat() >= chance) return false;
        }
        if (withered) return restore(level, pos, state, step, coverPos, cover, strength);
        BlockState next = step.to().defaultBlockState();
        if (!cover.isAir()) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
            if (!cover.canSurvive(level, coverPos)) {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                return false;
            }
        }
        level.setBlockAndUpdate(pos, Block.updateFromNeighbourShapes(next, level, pos));
        settle(level, pos, step.tier(), strength);
        return true;
    }

    private static boolean restore(ServerLevel level, BlockPos pos, BlockState original, WildConversion step, BlockPos coverPos, BlockState cover,
                                   int strength) {
        int deepest = 0;
        int hops = WildConversion.all(level.registryAccess()).size();
        for (WildConversion next = step; next != null && hops-- > 0;
             next = conversion(level, pos, next.to().defaultBlockState(), strength, WildConversion.Kind.GROUND)) {
            deepest = Math.max(deepest, next.tier());
            level.setBlock(pos, next.to().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (!revive(level, coverPos, cover, strength)) continue;
            level.setBlockAndUpdate(pos, Block.updateFromNeighbourShapes(next.to().defaultBlockState(), level, pos));
            settle(level, pos, deepest, strength);
            return true;
        }
        level.setBlock(pos, original, Block.UPDATE_CLIENTS);
        return false;
    }

    private static void settle(ServerLevel level, BlockPos pos, int tier, int strength) {
        WildLands.converted(level, pos, tier, strength);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 4, 0.35, 0.05, 0.35, 0.0);
        level.sendParticles(ParticleTypeRegistry.WILD_LEAF.get(), pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0, 0.0, 0.08, 0.0, 1.0);
    }

    private static boolean sprout(ServerLevel level, BlockPos ground, RandomSource random) {
        for (int attempt = 0; attempt < TREE_ATTEMPTS; attempt++) {
            BlockPos spot = attempt == 0 ? ground : ground.offset(random.nextInt(9) - 4, random.nextInt(3) - 1, random.nextInt(9) - 4);
            if (!level.getBlockState(spot).is(Blocks.GRASS_BLOCK) || !level.getBiome(spot).is(Biomes.FLOWER_FOREST)) continue;
            BlockPos root = spot.above();
            BlockState cover = level.getBlockState(root);
            if (!cover.isAir() && !yielding(cover)) continue;
            if (!tree(level, root, random)) continue;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, root.getX() + 0.5, root.getY() + 2.0, root.getZ() + 0.5, 30, 1.5, 2.0, 1.5, 0.0);
            level.playSound(null, root, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1.0F, 0.6F);
            return true;
        }
        return false;
    }

    private static boolean yielding(BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty() || state.hasBlockEntity()) return false;
        return state.is(BlockTags.LEAVES) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.getBlock() instanceof BushBlock
                || state.is(Blocks.SNOW) || state.is(Blocks.VINE) || state.is(Blocks.GLOW_LICHEN);
    }

    private static boolean growTree(ServerLevel level, BlockPos root, Planter planter, RandomSource random) {
        if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(root).expandTowards(0.0, TREE_CLEARANCE, 0.0)).isEmpty()) return false;
        Map<BlockPos, BlockState> cleared = new LinkedHashMap<>();
        for (BlockPos at : BlockPos.betweenClosed(root.offset(-CANOPY_REACH, 0, -CANOPY_REACH), root.offset(CANOPY_REACH, CANOPY_HEIGHT, CANOPY_REACH))) {
            BlockState state = level.getBlockState(at);
            if (yielding(state)) cleared.put(at.immutable(), state);
        }
        for (BlockPos at : cleared.keySet()) level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        boolean grown = planter.place(level, root, random);
        for (Map.Entry<BlockPos, BlockState> entry : cleared.entrySet()) {
            if (!grown || level.getBlockState(entry.getKey()).isAir()) level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_CLIENTS);
        }
        if (!grown) return false;
        for (Map.Entry<BlockPos, BlockState> entry : cleared.entrySet()) {
            BlockState state = entry.getValue();
            if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) || !level.getBlockState(entry.getKey()).equals(state)) continue;
            BlockPos partner = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? entry.getKey().above() : entry.getKey().below();
            if (!level.getBlockState(partner).is(state.getBlock())) level.setBlock(entry.getKey(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private static boolean transplant(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos root = pos;
        while (level.getBlockState(root.below()).is(Blocks.CACTUS)) root = root.below();
        List<Planter> trees = biomeTrees(level, root);
        if (trees.isEmpty()) return false;
        List<BlockPos> column = new ArrayList<>();
        List<BlockState> states = new ArrayList<>();
        for (BlockPos at = root; level.getBlockState(at).is(Blocks.CACTUS); at = at.above()) {
            column.add(at);
            states.add(level.getBlockState(at));
        }
        for (int index = column.size() - 1; index >= 0; index--) level.setBlock(column.get(index), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (!growTree(level, root, trees.get(random.nextInt(trees.size())), random)) {
            for (int index = 0; index < column.size(); index++) level.setBlock(column.get(index), states.get(index), Block.UPDATE_CLIENTS);
            return false;
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, root.getX() + 0.5, root.getY() + 2.0, root.getZ() + 0.5, 30, 1.5, 2.0, 1.5, 0.0);
        level.sendParticles(ParticleTypeRegistry.WILD_LEAF.get(), root.getX() + 0.5, root.getY() + 1.0, root.getZ() + 0.5, 0, 0.0, 0.15, 0.0, 1.0);
        level.playSound(null, root, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1.0F, 0.6F);
        return true;
    }

    private static List<Planter> biomeTrees(ServerLevel level, BlockPos pos) {
        List<HolderSet<PlacedFeature>> steps = level.getBiome(pos).value().getGenerationSettings().features();
        int vegetation = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        List<Planter> trees = new ArrayList<>();
        if (steps.size() <= vegetation) return trees;
        for (Holder<PlacedFeature> placed : steps.get(vegetation)) {
#if MC_VER >= 26300
            if (placed.value().getFeatures().noneMatch(feature -> feature.value() instanceof TreeFeature)) continue;
#else
            if (placed.value().getFeatures().noneMatch(feature -> feature.feature() instanceof TreeFeature)) continue;
#endif
            trees.add((at, root, random) -> placed.value().feature().value().place(at, at.getChunkSource().getGenerator(), random, root));
        }
        return trees;
    }

    private static boolean tree(ServerLevel level, BlockPos root, RandomSource random) {
#if MC_VER >= 26300
        Holder<Feature> tree = level.registryAccess().lookupOrThrow(Registries.FEATURE).get(VegetationFeatures.TREES_FLOWER_FOREST).orElse(null);
#else
        Holder<ConfiguredFeature<?, ?>> tree = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .get(VegetationFeatures.TREES_FLOWER_FOREST).orElse(null);
#endif
        if (tree == null) return false;
        return growTree(level, root, (at, base, chance) -> tree.value().place(at, at.getChunkSource().getGenerator(), chance, base), random);
    }

    private static float core(double distance) {
        if (distance <= SPLASH_CORE) return 1.0F;
        double edge = 1.0 - Mth.clamp((distance - SPLASH_CORE) / (1.0 - SPLASH_CORE), 0.0, 1.0);
        return (float) (0.9 * edge * edge);
    }

    private static float falloff(double distance) {
        double edge = Mth.clamp((distance - 0.3) / 0.7, 0.0, 1.0);
        return (float) (1.0 - (1.0 - EDGE_FLOOR) * edge * edge * (3.0 - 2.0 * edge));
    }

    private static float greener(ServerLevel level, BlockPos pos, int rank) {
        int count = 0;
        for (int[] offset : RING) {
            for (int dy = 1; dy >= -1; dy--) {
                if (rank(level, level.getBlockState(pos.offset(offset[0], dy, offset[1]))) > rank) {
                    count++;
                    break;
                }
            }
        }
        return count / (float) RING.length;
    }

    static boolean greenable(ServerLevel level, BlockPos ground) {
        if (level.getBlockState(ground.above()).is(Blocks.CACTUS)) return false;
        return rank(level, level.getBlockState(ground)) >= 0;
    }

    private static int rank(ServerLevel level, BlockState state) {
        int rank = -1;
        for (WildConversion conversion : WildConversion.all(level.registryAccess())) {
            if (conversion.kind() != WildConversion.Kind.GROUND) continue;
            if (state.is(conversion.from())) return MAX_STRENGTH - conversion.tier();
            if (state.is(conversion.to())) rank = MAX_STRENGTH;
        }
        return rank;
    }

    private static WildConversion conversion(ServerLevel level, BlockPos pos, BlockState state, int strength, WildConversion.Kind kind) {
        Holder<Biome> biome = null;
        for (WildConversion conversion : WildConversion.all(level.registryAccess())) {
            if (conversion.kind() != kind || conversion.tier() > strength || !state.is(conversion.from())) continue;
            if (conversion.biomes().isPresent()) {
                if (biome == null) biome = level.getBiome(pos);
                if (!conversion.allows(biome)) continue;
            }
            return conversion;
        }
        return null;
    }

    private static boolean withered(ServerLevel level, BlockState state) {
        for (WildConversion conversion : WildConversion.all(level.registryAccess())) {
            if (conversion.kind() == WildConversion.Kind.PLANT && state.is(conversion.from())) return true;
        }
        return false;
    }

    public static void bloom(ServerLevel level, BlockPos ground, int strength, RandomSource random) {
        BlockPos above = ground.above();
        if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK)) return;
        WildLands.bloomed(level, ground, strength);
        if (random.nextFloat() < TREE_CHANCE && level.getBiome(ground).is(Biomes.FLOWER_FOREST) && sprout(level, ground, random)) return;
        if (!level.getBlockState(above).isAir()) return;
        if (random.nextFloat() < FLOWER_SHARE) {
            BlockState flower = FLOWERS.get(random.nextInt(FLOWERS.size())).defaultBlockState();
            if (flower.canSurvive(level, above)) level.setBlockAndUpdate(above, flower);
        } else {
            int tufts = 3 + random.nextInt(4);
            for (int index = 0; index < tufts; index++) {
                BlockPos spot = index == 0 ? above : above.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
                if (!level.getBlockState(spot).isAir() || !level.getBlockState(spot.below()).is(Blocks.GRASS_BLOCK)) continue;
                if (random.nextFloat() < TALL_GRASS_SHARE && level.getBlockState(spot.above()).isAir()) {
                    DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), spot, Block.UPDATE_ALL);
                } else {
                    level.setBlockAndUpdate(spot, Blocks.GRASS.defaultBlockState());
                }
            }
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, above.getX() + 0.5, above.getY() + 0.3, above.getZ() + 0.5, 3, 0.3, 0.2, 0.3, 0.0);
    }

    static BlockPos surface(ServerLevel level, BlockPos around, int top, int bottom) {
        for (int dy = top; dy >= bottom; dy--) {
            BlockPos pos = around.above(dy);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || plant(state) || withered(level, state) || state.getBlock() instanceof BushBlock) continue;
            BlockPos ground = beneath(level, pos);
            return level.getBlockState(ground).isCollisionShapeFullBlock(level, ground) ? ground : null;
        }
        return null;
    }

    private static BlockPos beneath(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) ? pos : pos.below();
    }
}
