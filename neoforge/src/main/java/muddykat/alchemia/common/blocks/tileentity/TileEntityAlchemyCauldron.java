package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemRubedo;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.utility.ParticleUtils;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.world.InteractionResult.FAIL;
import static net.minecraft.world.InteractionResult.SUCCESS;

public class TileEntityAlchemyCauldron extends SyncedBlockEntity implements MenuProvider, Nameable {

    public static final int ACTIVE_SLOT = 0;
    public static final int REDSTONE_SLOT = 1;
    public static final int GUNPOWDER_SLOT = 2;
    public static final int TOOL_SLOT = 3;
    private static final int INVENTORY_SLOT_COUNT = 4;
    public static final int MAX_QUEUE = 12;

    public static final double STIR_STEP = 2.0;
    public static final double STIR_FINE_STEP = 0.5;
    private static final int STIR_BUDGET = 10;
    private static final int STIR_SOUND_INTERVAL = 12;
    private static final int FINISH_LIMIT = 4096;

    private static final int ALIGNMENT_SPACE = 1;


    private static final int LEGACY_SLOT_COUNT = 8;
    private static final int LEGACY_REDSTONE_SLOT = 6;
    private static final int LEGACY_GUNPOWDER_SLOT = 7;
    public static final int REDSTONE_DURATION_BONUS = 1200;

    private int waterLevel;
    private final int maxWaterLevel = BrewBase.MAX_FILL_LEVEL;
    private ItemStacksResourceHandler inventory;

    private int xAlignment;
    private int yAlignment;

    private final int balanceAlignment;
    private Item potion_type = Items.POTION;

    private final Random random = new Random();
    private int cooldown = 0;

    private boolean needsUpdate = false;
    private final List<String> addedIngredients = new ArrayList<>();
    private final List<String> script = new ArrayList<>();
    private final BrewCourse course = new BrewCourse(0.0, 0.0);
    private int stirCount = 0;
    private int stirBudget = STIR_BUDGET;
    private UUID lastStirrer;
    private long lastApply = Long.MIN_VALUE;
    private String brewName = "";
    private boolean spoiled = false;
    private BrewBase base = BrewBases.defaultBase();
    private int instability = 0;
    private int bonusInstability = 0;
    private int durationBonus = 0;
    private boolean lingeringRequested = false;
    private int potion_color = 0xffffff;

    private Set<MobEffectInstance> effectList = new HashSet<>();
    private Component customName;

    protected ContainerData alchemicalCauldronData;

    public TileEntityAlchemyCauldron(BlockPos pos, BlockState state) {
        this(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), pos, state);
    }

    public TileEntityAlchemyCauldron(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        alchemicalCauldronData = createIntArray();
        inventory = createHandler();
        waterLevel = 0;
        this.balanceAlignment = map().getMiddlePosition();
        this.xAlignment = map().getMiddlePosition();
        this.yAlignment = map().getMiddlePosition();
        this.course.reset(xAlignment, yAlignment);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        List<ItemStack> legacyQueue = new ArrayList<>(input.read("queue", ItemStack.CODEC.listOf()).orElse(List.of()));

        input.child("Inventory").ifPresent(saved -> {
            inventory.deserialize(saved);
            inventory = resizeInventory(inventory, legacyQueue);
        });
        waterLevel = input.getIntOr("waterLevel", 0);
        xAlignment = input.getIntOr("xAlignment", balanceAlignment);
        yAlignment = input.getIntOr("yAlignment", balanceAlignment);

        if (input.getIntOr("alignmentSpace", 0) < ALIGNMENT_SPACE) {
            xAlignment = (int) Math.round(xAlignment * PotionMap.CELL);
            yAlignment = (int) Math.round(yAlignment * PotionMap.CELL);
        }
        needsUpdate = input.getBooleanOr("needsUpdate", false);
        cooldown = input.getIntOr("cooldown", 0);

        addedIngredients.clear();
        addedIngredients.addAll(input.read("addedIngredients", Codec.STRING.listOf()).orElse(List.of()));

        effectList.clear();
        effectList.addAll(input.read("effects", MobEffectInstance.CODEC.listOf()).orElse(List.of()));
        brewName = input.getString("brewName").orElse("");
        spoiled = input.getBooleanOr("spoiled", false);
        base = BrewBases.byId(input.getString("base").orElse(BrewBases.DEFAULT_ID));
        instability = input.getIntOr("instability", 0);
        bonusInstability = input.getIntOr("bonusInstability", 0);
        durationBonus = input.getIntOr("durationBonus", 0);
        stirCount = input.getIntOr("stirCount", 0);

        script.clear();
        Optional<Double> courseX = input.read("courseX", Codec.DOUBLE);

        if (courseX.isPresent()) {
            script.addAll(input.read("script", Codec.STRING.listOf()).orElse(List.of()));
            course.restore(courseX.get(), input.read("courseY", Codec.DOUBLE).orElse((double) yAlignment),
                    input.read("course", ItemStack.CODEC.listOf()).orElse(List.of()),
                    input.read("courseAnchors", Codec.INT.listOf()).orElse(List.of()),
                    input.getIntOr("courseSpent", 0),
                    input.read("courseProgress", Codec.DOUBLE).orElse(0.0));
        } else {
            List<String> upgraded = BrewRecipe.upgradeEntries(addedIngredients);
            addedIngredients.clear();
            addedIngredients.addAll(upgraded);
            script.addAll(BrewRecipe.legacySteps(upgraded));

            course.reset(xAlignment, yAlignment);
            for (ItemStack stack : legacyQueue) plan(stack);
        }

        refreshAlignment();
        updateWaterColor();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
        output.putInt("waterLevel", waterLevel);
        output.putInt("xAlignment", xAlignment);
        output.putInt("yAlignment", yAlignment);
        output.putInt("alignmentSpace", ALIGNMENT_SPACE);
        output.putBoolean("needsUpdate", needsUpdate);
        output.putInt("cooldown", cooldown);
        output.store("addedIngredients", Codec.STRING.listOf(), List.copyOf(addedIngredients));
        output.store("script", Codec.STRING.listOf(), List.copyOf(script));
        output.store("course", ItemStack.CODEC.listOf(), course.entries());
        output.store("courseAnchors", Codec.INT.listOf(), course.anchors());
        output.store("courseX", Codec.DOUBLE, course.originX());
        output.store("courseY", Codec.DOUBLE, course.originY());
        output.putInt("courseSpent", course.spent());
        output.store("courseProgress", Codec.DOUBLE, course.progress());
        output.putInt("stirCount", stirCount);
        output.store("effects", MobEffectInstance.CODEC.listOf(), List.copyOf(effectList));
        output.putString("brewName", brewName);
        output.putBoolean("spoiled", spoiled);
        output.putString("base", base.id());
        output.putInt("instability", instability);
        output.putInt("bonusInstability", bonusInstability);
        output.putInt("durationBonus", durationBonus);
    }

    @Override
    public InteractionResult onActivated(BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        Level level = getLevel();
        if (level == null) return FAIL;

        ItemStack heldStack = player.getItemInHand(hand);
        Item heldItem = heldStack.getItem();

        if (heldItem.equals(Items.BUCKET) && !canFill()) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            empty();
            return SUCCESS;
        }

        InteractionResult filled = tryFill(player, hand, heldStack, pos, level);
        if (filled != null) return filled;

        if (heldItem instanceof ItemRubedo) {
            if (!addRubedo(player)) return InteractionResult.FAIL;

            heldStack.shrink(1);
            player.setItemInHand(hand, heldStack);
            return InteractionResult.CONSUME;
        }

        if (heldItem instanceof ItemIngredient) {
            if (getWaterLevel() <= 0) return InteractionResult.FAIL;
            if (level.isClientSide()) return InteractionResult.CONSUME;

            if (!plan(heldStack)) {
                message(player, spoiled ? "alchemia.brew.ruined" : "alchemia.brew.queue_full");
                return InteractionResult.FAIL;
            }

            IngredientDiscovery.record(player, heldStack);
            heldStack.shrink(1);
            player.setItemInHand(hand, heldStack);

            level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            sync();
            return InteractionResult.CONSUME;
        }

        if (heldItem.equals(Items.DRAGON_BREATH)) {
            if (getWaterLevel() <= 0) return InteractionResult.FAIL;

            if (!hasGunpowder()) {
                message(player, "alchemia.brew.needs_gunpowder");
                return InteractionResult.FAIL;
            }
            lingeringRequested = true;
        }

        if (heldItem.equals(Items.GLASS_BOTTLE) || heldItem.equals(Items.DRAGON_BREATH) || heldItem instanceof ItemPotionGlass) {
            if (getWaterLevel() > 0) {
                takeWaterPortion();
                ItemStack potion = getPotion();
                if (heldItem instanceof ItemPotionGlass glass) PotionGlasses.apply(potion, glass.glass());
                Collection<MobEffectInstance> brewed = List.copyOf(getEffectList());
                List<String> used = recipeEntries(addedIngredients, "");
                List<String> steps = recipeEntries(script, String.valueOf(BrewRecipe.STEP_ADD));
                heldStack.shrink(1);
                player.getInventory().add(potion);

                if (hasGunpowder() && !level.isClientSide()) {
                    inventory.set(GUNPOWDER_SLOT, ItemResource.EMPTY, 0);
                }
                lingeringRequested = false;

                if (!level.isClientSide() && !spoiled) BrewRecipeBook.record(player, base, brewed, used, steps, getBrewName());

                if (!level.isClientSide() && !spoiled && RecipeDiscovery.record(player, brewed) > 0) {
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.4F, 1.6F);
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendOverlayMessage(Component.translatable("alchemia.guide.discovered"));
                    }
                }

                if (getWaterLevel() < 1) {
                    resetEffectList();
                    if (!level.isClientSide()) {
                        level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.SPLASH, pos);
                        sync();
                    }
                }

                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.SPLASH, pos);
                    sync();
                }

                return InteractionResult.CONSUME;
            }
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this, pos);
        }

        return SUCCESS;
    }

    public InteractionResult replaySelectedRecipe(Player player, ItemStack guide) {
        Level level = getLevel();
        BlockPos pos = getBlockPos();
        if (level == null) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        BrewRecipe recipe = BrewRecipeBook.selectedRecipe(guide);
        if (recipe == null || !recipe.isResolvable()) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.none_selected"));
            return InteractionResult.FAIL;
        }

        if (getWaterLevel() <= 0) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.no_water"));
            return InteractionResult.FAIL;
        }

        if (!recipe.brewBase().equals(base)) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.wrong_recipe_base",
                    recipe.brewBase().displayName()));
            return InteractionResult.FAIL;
        }

        if (!isUntouched() || course.planned() > 0) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.not_clean"));
            return InteractionResult.FAIL;
        }

        if (recipe.requiresGrinding() && !playerHasMortar(player)) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.needs_mortar"));
            return InteractionResult.FAIL;
        }

        Map<ItemStack, Integer> required = requiredTally(recipe);

        for (Map.Entry<ItemStack, Integer> entry : required.entrySet()) {
            if (countIn(player, entry.getKey()) < entry.getValue()) {
                serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.missing", entry.getKey().getHoverName()));
                return InteractionResult.FAIL;
            }
        }

        for (Map.Entry<ItemStack, Integer> entry : required.entrySet()) {
            consumeFrom(player, entry.getKey(), entry.getValue());
        }

        setBrewName(recipe.name());

        for (String step : recipe.steps()) {
            perform(step);
            if (spoiled) break;
        }
        refreshAlignment();

        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.gameEvent(null, GameEvent.SPLASH, pos);
        serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.done"));
        sync();

        return InteractionResult.CONSUME;
    }

    private static boolean sameIngredient(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem();
    }

    public static Map<ItemStack, Integer> requiredTally(BrewRecipe recipe) {
        Map<ItemStack, Integer> required = new LinkedHashMap<>();

        for (Map.Entry<ItemStack, Integer> entry : recipe.tally().entrySet()) {
            ItemStack existing = null;
            for (ItemStack seen : required.keySet()) {
                if (sameIngredient(seen, entry.getKey())) {
                    existing = seen;
                    break;
                }
            }
            required.merge(existing == null ? entry.getKey() : existing, entry.getValue(), Integer::sum);
        }
        return required;
    }

    private static boolean playerHasMortar(Player player) {
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() instanceof ItemMortarPestle) return true;
        }
        return false;
    }

    private static int countIn(Player player, ItemStack wanted) {
        int found = 0;
        for (ItemStack stack : player.getInventory()) {
            if (sameIngredient(stack, wanted)) found += stack.getCount();
        }
        return found;
    }

    private static void consumeFrom(Player player, ItemStack wanted, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory()) {
            if (remaining <= 0) break;
            if (!sameIngredient(stack, wanted)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
    }

    private Item resultType() {
        if (lingeringRequested) return Items.LINGERING_POTION;
        return hasGunpowder() ? Items.SPLASH_POTION : Items.POTION;
    }

    public void setSplashResult() {
        potion_type = Items.SPLASH_POTION;
        BlockPos pos = getBlockPos();
        if (level != null && level.isClientSide()) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5d, pos.getY() + 1, pos.getZ() + 0.5d, 0, 0.1, 0);
        }
        if (level != null && !level.isClientSide()) sync();
    }

    public void setLingeringResult() {
        potion_type = Items.LINGERING_POTION;
    }

    public void setDefaultResult() {
        potion_type = Items.POTION;
    }

    private ItemStacksResourceHandler resizeInventory(ItemStacksResourceHandler saved, List<ItemStack> queue) {
        if (saved.size() == INVENTORY_SLOT_COUNT) return saved;

        ItemStacksResourceHandler resized = createHandler();
        if (saved.size() < LEGACY_SLOT_COUNT) {
            for (int slot = 0; slot < Math.min(saved.size(), INVENTORY_SLOT_COUNT); slot++) {
                resized.set(slot, saved.getResource(slot), saved.getAmountAsInt(slot));
            }
            return resized;
        }

        resized.set(ACTIVE_SLOT, saved.getResource(0), saved.getAmountAsInt(0));
        resized.set(REDSTONE_SLOT, saved.getResource(LEGACY_REDSTONE_SLOT), saved.getAmountAsInt(LEGACY_REDSTONE_SLOT));
        resized.set(GUNPOWDER_SLOT, saved.getResource(LEGACY_GUNPOWDER_SLOT), saved.getAmountAsInt(LEGACY_GUNPOWDER_SLOT));

        for (int slot = 1; slot < LEGACY_REDSTONE_SLOT; slot++) {
            ItemResource resource = saved.getResource(slot);
            if (resource.isEmpty()) continue;

            int amount = saved.getAmountAsInt(slot);
            for (int copy = 0; copy < amount && queue.size() < MAX_QUEUE; copy++) {
                queue.add(resource.toStack(1));
            }
        }
        return resized;
    }

    private ItemStacksResourceHandler createHandler() {
        return new ItemStacksResourceHandler(INVENTORY_SLOT_COUNT) {
            @Override
            protected int getCapacity(int index, ItemResource resource) {
                return index == REDSTONE_SLOT || index == ACTIVE_SLOT ? 64 : 1;
            }
        };
    }

    public void setFullWater() {
        addFill(BrewBases.defaultBase(), BrewBase.MAX_FILL_LEVEL);
    }

    private @Nullable InteractionResult tryFill(Player player, InteractionHand hand, ItemStack heldStack, BlockPos pos, Level level) {
        Item heldItem = heldStack.getItem();
        BrewBases.Match match = BrewBases.forItem(heldStack);
        if (match == null) return tryFluidFill(player, hand, pos, level);

        if (!canAcceptFill(match.base(), match.fill().levels())) return refuseFill(player, match.base());

        Item remainder = match.fill().remainderValue();
        if (!player.isCreative()) {
            if (remainder != null) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(remainder)));
            } else {
                heldStack.shrink(1);
                player.setItemInHand(hand, heldStack);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(heldItem));
        addFill(match.base(), match.fill().levels());
        announceFill(level, pos, match.fill().levels());
        return SUCCESS;
    }

    private @Nullable InteractionResult tryFluidFill(Player player, InteractionHand hand, BlockPos pos, Level level) {
        ResourceHandler<FluidResource> held = ItemAccess.forPlayerInteraction(player, hand).oneByOne()
                .getCapability(Capabilities.Fluid.ITEM);
        if (held == null) return null;

        for (int index = 0; index < held.size(); index++) {
            FluidResource resource = held.getResource(index);
            if (resource.isEmpty()) continue;

            BrewBases.Match match = BrewBases.forFluid(resource);
            if (match == null) continue;
            if (!canAcceptFill(match.base(), match.fill().levels())) return refuseFill(player, match.base());

            int amount = match.fill().fluidAmount();
            try (Transaction transaction = Transaction.openRoot()) {
                if (held.extract(index, resource, amount, transaction) < amount) continue;
                transaction.commit();
            }

            addFill(match.base(), match.fill().levels());
            announceFill(level, pos, match.fill().levels());
            return SUCCESS;
        }
        return null;
    }

    private InteractionResult refuseFill(Player player, BrewBase filled) {
        if (waterLevel > 0 && !filled.equals(base)) message(player, "alchemia.brew.wrong_base");
        return FAIL;
    }

    private void announceFill(Level level, BlockPos pos, int levels) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, levels >= BrewBase.MAX_FILL_LEVEL ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_EMPTY,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
    }

    public void addFill(BrewBase filled, int levels) {
        if (waterLevel == 0) {
            resetEffectList();
            this.base = filled;
        }

        this.waterLevel = Math.min(this.maxWaterLevel, this.waterLevel + levels);
        refreshBaseColor();

        needsUpdate = true;
        markUpdated();

        if (level != null && !level.isClientSide()) sync();
    }

    public boolean canAcceptFill(BrewBase filled, int levels) {
        if (waterLevel == 0) return true;
        return base.equals(filled) && isUntouched() && waterLevel + levels <= maxWaterLevel;
    }

    private void refreshBaseColor() {
        if (level == null || !level.isClientSide()) return;
        potion_color = base.biomeTint()
                ? BiomeColors.getAverageWaterColor((BlockAndTintGetter) level, getBlockPos())
                : base.tintOrDefault();
    }

    public boolean shouldRenderFace(Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }

    public String getBrewName() {
        return brewName;
    }

    public void setBrewName(String name) {
        this.brewName = name == null ? "" : name.trim();
        setChanged();
        if (level != null && !level.isClientSide()) sync();
    }

    public ItemStacksResourceHandler getInventory() {
        return inventory;
    }

    public List<ItemStack> getQueuedIngredients() {
        return course.plannedStacks();
    }

    public int queueSize() {
        return course.planned();
    }

    public boolean queueFull() {
        return course.planned() >= MAX_QUEUE;
    }

    public BrewCourse getCourse() {
        return course;
    }

    public int getStirCount() {
        return stirCount;
    }

    public PathPoint getBrewPosition() {
        return PotionMap.isReady() ? course.position(map()) : new PathPoint(xAlignment, yAlignment);
    }

    private void refreshAlignment() {
        if (!PotionMap.isReady()) return;

        PathPoint at = course.position(map());
        xAlignment = at.roundedX();
        yAlignment = at.roundedY();
    }

    private boolean plan(ItemStack stack) {
        if (spoiled || queueFull() || !isBrewingInput(stack)) return false;

        String entry = encodeOf(stack);
        if (entry == null) return false;

        course.add(stack);
        script.add(BrewRecipe.STEP_ADD + entry);
        addedIngredients.add(entry);
        setChanged();
        return true;
    }

    private static void dropFromEnd(List<String> list, String value, int occurrence) {
        int seen = 0;
        for (int index = list.size() - 1; index >= 0; index--) {
            if (!list.get(index).equals(value)) continue;
            if (++seen < occurrence) continue;

            list.remove(index);
            return;
        }
    }

    private List<String> recipeEntries(List<String> recorded, String prefix) {
        List<ItemStack> idle = course.unstarted();
        for (ItemStack stack : idle) {
            if (VanillaIngredients.isLure(stack)) return List.copyOf(recorded);
        }

        List<String> trimmed = new ArrayList<>(recorded);
        for (int index = idle.size() - 1; index >= 0; index--) {
            String entry = encodeOf(idle.get(index));
            if (entry != null) dropFromEnd(trimmed, prefix + entry, 1);
        }
        return List.copyOf(trimmed);
    }

    public ItemStack getActiveIngredient() {
        int amount = inventory.getAmountAsInt(ACTIVE_SLOT);
        return amount <= 0 ? ItemStack.EMPTY : inventory.getResource(ACTIVE_SLOT).toStack(amount);
    }

    public ItemStack getTool() {
        int amount = inventory.getAmountAsInt(TOOL_SLOT);
        return amount <= 0 ? ItemStack.EMPTY : inventory.getResource(TOOL_SLOT).toStack(amount);
    }

    public boolean hasTool() {
        return IngredientTool.of(getTool()) != null;
    }

    public boolean canApplyTool() {
        IngredientTool tool = IngredientTool.of(getTool());
        return tool != null && tool.canApply(getActiveIngredient());
    }

    public boolean canQueue() {
        return !spoiled && !queueFull() && isBrewingInput(getActiveIngredient());
    }

    public boolean canStir() {
        return !spoiled && waterLevel > 0 && course.canStir();
    }

    private void setActive(ItemStack stack) {
        if (stack.isEmpty()) {
            inventory.set(ACTIVE_SLOT, ItemResource.EMPTY, 0);
        } else {
            inventory.set(ACTIVE_SLOT, ItemResource.of(stack), stack.getCount());
        }
    }

    public InteractionResult queueActive(Player player) {
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack active = getActiveIngredient();
        if (!isBrewingInput(active)) {
            message(player, "alchemia.brew.no_ingredient");
            return InteractionResult.FAIL;
        }
        if (queueFull()) {
            message(player, "alchemia.brew.queue_full");
            return InteractionResult.FAIL;
        }

        if (!plan(active)) return InteractionResult.FAIL;

        active.shrink(1);
        setActive(active);

        BlockPos pos = getBlockPos();
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.4F, 1.5F);
        setChanged();
        sync();
        return InteractionResult.SUCCESS;
    }

    public InteractionResult unqueue(Player player, int index) {
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;
        if (!course.canRemove(index)) return InteractionResult.FAIL;

        List<ItemStack> planned = course.plannedStacks();
        String entry = encodeOf(planned.get(index));
        int occurrence = 0;
        for (int later = index; later < planned.size(); later++) {
            if (entry != null && entry.equals(encodeOf(planned.get(later)))) occurrence++;
        }

        ItemStack removed = course.remove(index);
        if (entry != null) {
            dropFromEnd(script, BrewRecipe.STEP_ADD + entry, occurrence);
            dropFromEnd(addedIngredients, entry, occurrence);
        }

        if (!player.getInventory().add(removed)) {
            Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), removed);
        }

        course.compact(map());
        refreshAlignment();
        setChanged();
        sync();
        return InteractionResult.SUCCESS;
    }

    public boolean applyTool(Player player) {
        if (!(level instanceof ServerLevel server)) return false;

        ItemStack held = getTool();
        IngredientTool tool = IngredientTool.of(held);
        if (tool == null) return false;

        ItemStack active = getActiveIngredient();
        if (!tool.canApply(active)) return false;

        long now = server.getGameTime();
        if (now < lastApply + Math.max(1, tool.ticksPerStep() / 2)) return false;
        lastApply = now;

        tool.applyStep(active, held, server, player);
        setActive(active);

        if (held.isEmpty()) {
            inventory.set(TOOL_SLOT, ItemResource.EMPTY, 0);
        } else {
            inventory.set(TOOL_SLOT, ItemResource.of(held), held.getCount());
        }

        tool.playStep(server, getBlockPos());
        setChanged();
        sync();
        return true;
    }

    public enum BrewCheck {
        READY,
        NOTHING,
        WEAKER,
        TOO_POTENT,
        DORMANT,
        UNAVAILABLE
    }

    public boolean isDormant(PotionMap.PotionEffectPosition found) {
        return found.getRecipe() != null && WorldEffects.dormant(found.getRecipe(), level, getBlockPos());
    }

    private void noteReached(Player player) {
        if (player == null || !PotionMap.isReady()) return;

        PathPoint at = getBrewPosition();
        PotionMap.MapEntry entry = map().nearest(at.x(), at.y(), PotionMap.EFFECT_RADIUS);
        if (entry == null) return;

        WorldEffects.note(player, entry.effect().getRecipe());
    }

    private int heldPotency(Holder<MobEffect> effect) {
        for (MobEffectInstance instance : effectList) {
            if (instance.getEffect().equals(effect)) return instance.getAmplifier() + 1;
        }
        return 0;
    }

    public PotionMap.PotionEffectPosition rawEffectAt(PathPoint at) {
        return map().getEffectPotion(at);
    }

    public int levelCap(PotionEnum recipe) {
        return WorldEffects.levelCap(recipe, level, getBlockPos());
    }

    public PotionMap.PotionEffectPosition effectAt(PathPoint at) {
        PotionMap.PotionEffectPosition found = rawEffectAt(at);
        if (found.getEffect() == null) return found;

        int cap = levelCap(found.getRecipe());
        return cap == Integer.MAX_VALUE ? found : found.capped(cap - 1);
    }

    public int gainAt(PathPoint at) {
        PotionMap.PotionEffectPosition found = effectAt(at);
        if (found.getEffect() == null) return 0;
        return Math.max(0, found.getStrength() + 1 - heldPotency(found.getEffect()));
    }

    private boolean effectReady(PathPoint at) {
        PotionMap.PotionEffectPosition found = effectAt(at);
        int gain = gainAt(at);
        return found.getEffect() != null && gain > 0 && !isDormant(found) && totalPotency() + gain <= maxPotency();
    }

    private boolean redstoneReady() {
        return hasRedstone() && totalPotency() < maxPotency();
    }

    public BrewCheck checkBrew(PathPoint at) {
        if (spoiled || waterLevel <= 0 || !PotionMap.isReady()) return BrewCheck.UNAVAILABLE;
        if (effectReady(at)) return BrewCheck.READY;

        PotionMap.PotionEffectPosition found = effectAt(at);
        if (found.getEffect() != null && isDormant(found)) return redstoneReady() ? BrewCheck.READY : BrewCheck.DORMANT;
        if (found.getEffect() != null && gainAt(at) > 0) return BrewCheck.TOO_POTENT;
        if (redstoneReady()) return BrewCheck.READY;
        return found.getEffect() != null ? BrewCheck.WEAKER : BrewCheck.NOTHING;
    }

    private boolean brewEffect() {
        PathPoint at = getBrewPosition();
        if (!effectReady(at)) return false;

        applyEffectPosition(effectAt(at));
        script.add(String.valueOf(BrewRecipe.STEP_BREW));
        updateWaterColor();
        return true;
    }

    public InteractionResult brew(Player player) {
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        switch (checkBrew(getBrewPosition())) {
            case NOTHING -> message(player, "alchemia.brew.nothing");
            case WEAKER -> message(player, "alchemia.brew.weaker");
            case TOO_POTENT -> message(player, "alchemia.brew.too_potent");
            case DORMANT -> message(player, "alchemia.brew.dormant");
            case UNAVAILABLE -> message(player, waterLevel <= 0 ? "alchemia.brew.no_water" : "alchemia.brew.ruined");
            case READY -> {
                brewEffect();
                applyRedstone();

                BlockPos pos = getBlockPos();
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.1F);
                level.gameEvent(null, GameEvent.SPLASH, pos);
                setChanged();
                sync();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    private boolean arrive(BrewCourse.Arrival arrival) {
        if (level == null) return true;
        BlockPos pos = getBlockPos();

        if (arrival.path().dead()) return destabilise();

        if (arrival.path().stalled()) {
            level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.7F, 0.6F);
            return true;
        }

        level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.SPLASH, pos);
        return true;
    }

    private void recordStir(boolean fine) {
        char kind = fine ? BrewRecipe.STEP_FINE : BrewRecipe.STEP_STIR;

        if (!script.isEmpty()) {
            String last = script.get(script.size() - 1);
            if (!last.isEmpty() && last.charAt(0) == kind) {
                script.set(script.size() - 1, kind + String.valueOf(countOf(last) + 1));
                return;
            }
        }
        script.add(kind + "1");
    }

    private static int countOf(String step) {
        try {
            return Math.max(0, Integer.parseInt(step.substring(1)));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private boolean teleporting() {
        return PotionMap.isReady() && course.teleporting(map());
    }

    private boolean stirOnce(boolean fine) {
        if (!canStir() || !PotionMap.isReady()) return false;
        if (!course.stir(map(), fine ? STIR_FINE_STEP : STIR_STEP, this::arrive)) return false;

        recordStir(fine);
        return true;
    }

    private void stirRepeatedly(int limit, boolean fine) {
        for (int count = 0; count < limit; count++) {
            if (!stirOnce(fine)) return;
        }
    }

    public boolean stir(Player player, boolean fine) {
        if (level == null || level.isClientSide()) return false;

        stirCount++;
        boolean stirred = stirBudget > 0 && !teleporting() && stirOnce(fine);

        if (stirred) {
            stirBudget--;
            refreshAlignment();
            if (player != null) lastStirrer = player.getUUID();
            noteReached(player);

            if (stirCount % STIR_SOUND_INTERVAL == 0) {
                level.playSound(null, getBlockPos(), SoundEvents.BOAT_PADDLE_WATER, SoundSource.BLOCKS,
                        0.25F, 0.8F + random.nextFloat() * 0.4F);
            }
        }

        setChanged();
        sync();
        return stirred;
    }

    private void perform(String step) {
        if (step.isEmpty()) return;

        char kind = step.charAt(0);
        String value = step.substring(1);

        switch (kind) {
            case BrewRecipe.STEP_ADD -> plan(BrewRecipe.stackFor(value));
            case BrewRecipe.STEP_STIR -> stirRepeatedly(countOf(step), false);
            case BrewRecipe.STEP_FINE -> stirRepeatedly(countOf(step), true);
            case BrewRecipe.STEP_FINISH -> stirRepeatedly(FINISH_LIMIT, false);
            case BrewRecipe.STEP_BREW -> brewEffect();
            case BrewRecipe.STEP_REDSTONE -> addRedstoneCharge();
            default -> {
            }
        }
    }

    public void addRedstoneCharge() {
        if (spoiled || totalPotency() >= maxPotency()) return;

        durationBonus += REDSTONE_DURATION_BONUS;
        addedIngredients.add(BrewRecipe.encode(BuiltInRegistries.ITEM.getKey(Items.REDSTONE).toString(), 0));
        script.add(String.valueOf(BrewRecipe.STEP_REDSTONE));
        updateWaterColor();
    }

    private void applyRedstone() {
        int held = redstoneCount();
        if (held <= 0 || spoiled) return;

        int room = maxPotency() - totalPotency();
        int taken = Math.min(held, room);
        if (taken <= 0) return;

        for (int charge = 0; charge < taken; charge++) {
            addedIngredients.add(BrewRecipe.encode(BuiltInRegistries.ITEM.getKey(Items.REDSTONE).toString(), 0));
            script.add(String.valueOf(BrewRecipe.STEP_REDSTONE));
        }

        int left = held - taken;
        if (left > 0) {
            inventory.set(REDSTONE_SLOT, inventory.getResource(REDSTONE_SLOT), left);
        } else {
            inventory.set(REDSTONE_SLOT, ItemResource.EMPTY, 0);
        }

        durationBonus += taken * REDSTONE_DURATION_BONUS;
        updateWaterColor();
    }

    private static void message(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(Component.translatable(key));
        }
    }

    public boolean isUntouched() {
        for (String step : script) {
            if (step.isEmpty() || step.charAt(0) != BrewRecipe.STEP_ADD) return false;
        }
        return effectList.isEmpty() && instability == 0;
    }

    public static boolean isBrewingInput(ItemStack stack) {
        return stack.getItem() instanceof ItemIngredient || VanillaIngredients.isVanillaIngredient(stack);
    }

    private static String encodeOf(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return null;

        String name = stack.getItem() instanceof ItemIngredient ? id.getPath() : id.toString();
        return BrewRecipe.encode(name, ItemIngredient.crushOf(stack));
    }

    private boolean slotHolds(int slot, Item item) {
        return slot < inventory.size() && inventory.getAmountAsInt(slot) > 0
                && inventory.getResource(slot).getItem() == item;
    }

    public boolean hasRedstone() {
        return redstoneCount() > 0;
    }

    public int redstoneCount() {
        return slotHolds(REDSTONE_SLOT, Items.REDSTONE) ? inventory.getAmountAsInt(REDSTONE_SLOT) : 0;
    }

    public int redstoneCharges() {
        return durationBonus / REDSTONE_DURATION_BONUS;
    }

    public boolean hasGunpowder() {
        return slotHolds(GUNPOWDER_SLOT, Items.GUNPOWDER);
    }

    public int getDurationBonus() {
        return durationBonus;
    }

    public boolean isSpoiled() {
        return spoiled;
    }

    public BrewBase getBase() {
        return base;
    }

    private PotionMap map() {
        return PotionMap.get(base);
    }

    public int getInstability() {
        return instability;
    }

    public int getInstabilityCap() {
        return base.instabilityCap() + bonusInstability;
    }

    public boolean hasBonusInstability() {
        return bonusInstability > 0;
    }

    public boolean addRubedo(Player player) {
        if (spoiled || getWaterLevel() <= 0) return false;

        if (bonusInstability > 0) {
            message(player, "alchemia.brew.rubedo_already");
            return false;
        }

        bonusInstability = 1;
        if (level != null && !level.isClientSide()) {
            level.playSound(null, getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.2F);
            message(player, "alchemia.brew.rubedo_added");
        }
        sync();
        return true;
    }

    private boolean destabilise() {
        instability++;
        if (instability >= getInstabilityCap()) {
            spoil();
            return false;
        }

        if (level != null && !level.isClientSide()) {
            BlockPos pos = getBlockPos();
            level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.9F, 0.4F);
            level.playSound(null, pos, SoundEvents.WITHER_HURT, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            ParticleUtils.generateEvaporationParticles(level, pos, SPOILED_COLOR);
            sync();
        }
        return true;
    }

    private void spoil() {
        spoiled = true;
        instability = getInstabilityCap();
        effectList.clear();
        updateWaterColor();

        if (level != null && !level.isClientSide()) {
            BlockPos pos = getBlockPos();
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.5F);
            level.playSound(null, pos, SoundEvents.WITHER_HURT, SoundSource.BLOCKS, 0.5F, 0.5F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            ParticleUtils.generateEvaporationParticles(level, pos, SPOILED_COLOR);
            sync();
        }
    }

    private void applyEffectPosition(PotionMap.PotionEffectPosition potionEffectPos) {
        if (level == null) return;

        Holder<MobEffect> mobEffect = potionEffectPos.getEffect();
        BlockPos pos = getBlockPos();

        if (mobEffect != null) {
            int effectDuration = potionEffectPos.getDuration();
            int effectStrength = potionEffectPos.getStrength();
            boolean perfect = potionEffectPos.isPerfect();
            boolean useSmoke = false;

            if (effectList.stream().map(MobEffectInstance::getEffect).toList().contains(mobEffect)) {
                effectList.removeIf(instance -> instance.getEffect().equals(mobEffect));
                if (perfect) {
                    if (!level.isClientSide()) {
                        level.playSound(null, pos, SoundEvents.GLOW_SQUID_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
                }
            } else {
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                useSmoke = true;
            }

            addEffect(new MobEffectInstance(mobEffect, effectDuration, effectStrength));

            if (useSmoke) {
                ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
            }
            if (perfect && level.isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + random.nextFloat(), pos.getY() + 1D, pos.getZ() + random.nextFloat(), 0, 0.1, 0);
                }
            }
        }

    }

    public int getXAlignment() {
        return xAlignment;
    }

    public int getYAlignment() {
        return yAlignment;
    }

    public boolean canFill() {
        return waterLevel == 0;
    }

    public void takeWaterPortion() {
        this.waterLevel = this.waterLevel - 1;
        if (level == null) return;
        refreshBaseColor();
        if (!level.isClientSide()) sync();
    }

    @Override
    public Component getName() {
        return customName != null ? customName : TextUtils.getTranslation("container.alchemical_cauldron");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    private ContainerData createIntArray() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> TileEntityAlchemyCauldron.this.waterLevel;
                    case 1 -> TileEntityAlchemyCauldron.this.xAlignment;
                    case 2 -> TileEntityAlchemyCauldron.this.yAlignment;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> TileEntityAlchemyCauldron.this.waterLevel = value;
                    case 1 -> TileEntityAlchemyCauldron.this.xAlignment = value;
                    case 2 -> TileEntityAlchemyCauldron.this.yAlignment = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AlchemicalCauldronMenu(id, playerInventory, this, alchemicalCauldronData);
    }

    public static int maxPotency() {
        return Configuration.maxPotency();
    }

    public int totalPotency() {
        int total = redstoneCharges();
        for (MobEffectInstance instance : effectList) total += instance.getAmplifier() + 1;
        return total;
    }

    public Collection<MobEffectInstance> getEffectList() {
        return effectList;
    }

    public void resetEffectList() {
        List<ItemStack> kept = course.unstarted();

        addedIngredients.clear();
        script.clear();
        spoiled = false;
        instability = 0;
        bonusInstability = 0;
        durationBonus = 0;
        effectList.clear();
        effectList = new HashSet<>();
        xAlignment = map().getMiddlePosition();
        yAlignment = map().getMiddlePosition();

        course.reset(xAlignment, yAlignment);
        for (ItemStack stack : kept) plan(stack);

        markUpdated();
        updateWaterColor();
        if (level != null) {
            level.setBlockAndUpdate(getBlockPos(), getBlockState());
            if (!level.isClientSide()) sync();
        }
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        effectList.add(mobEffectInstance);
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public boolean checkForEffect(Holder<MobEffect> effect, int duration) {
        return effectList.stream().noneMatch(instance -> instance.getEffect().equals(effect) && instance.getDuration() == duration);
    }

    public void ensureStrength(Holder<MobEffect> effect, int effectStrength) {
        boolean replace = false;
        for (MobEffectInstance instance : effectList) {
            if (instance.getEffect().equals(effect) && instance.getAmplifier() < effectStrength) {
                replace = true;
            }
        }

        if (replace) {
            effectList.removeIf(instance -> instance.getEffect().equals(effect));
            effectList.add(new MobEffectInstance(effect, 1200, effectStrength));
        }
    }

    public ItemStack getPotion() {
        ItemStack customPotion = new ItemStack(resultType());
        if (spoiled) {
            customPotion.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(Optional.empty(), Optional.of(SPOILED_COLOR), List.of(), Optional.of("ruined")));
            return customPotion;
        }
        if (getEffectList().isEmpty()) {
            ItemStack unbrewed = base.unbrewedStack();
            if (!unbrewed.isEmpty()) return unbrewed;

            customPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            return customPotion;
        }

        List<MobEffectInstance> brewed = new ArrayList<>();
        for (MobEffectInstance instance : getEffectList()) {
            brewed.add(new MobEffectInstance(instance.getEffect(),
                    instance.getDuration() + durationBonus, instance.getAmplifier()));
        }

        customPotion.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(getPotionColor()), brewed, Optional.of("alchemical")));
        if (!brewName.isEmpty()) {
            customPotion.set(DataComponents.CUSTOM_NAME,
                    Component.literal(brewName).withStyle(style -> style.withItalic(false)));
        }
        if (level != null && !level.isClientSide()) sync();
        return customPotion;
    }

    private void markUpdated() {
        this.setChanged();
        if (this.getLevel() != null) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void tick() {
        if (level == null) return;
        BlockPos pos = getBlockPos();

        if (level.isClientSide()) {
            if (random.nextFloat() < 0.2F && waterLevel > 0 && !getEffectList().isEmpty()) {
                int color = getEffectList().stream().findFirst()
                        .map(instance -> instance.getEffect().value().getColor())
                        .orElse(MobEffects.WATER_BREATHING.value().getColor());

                ParticleUtils.generatePotionParticles(level, pos, color, false);
            }

            if (random.nextFloat() < 0.2f) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + .1D;
                double z = pos.getZ() + random.nextDouble();
                ParticleOptions options = ParticleTypes.FLAME;
                level.addParticle(options, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }

        if (stirBudget < STIR_BUDGET) stirBudget++;

        if (!level.isClientSide() && teleporting() && stirOnce(false)) {
            refreshAlignment();
            if (!teleporting() && lastStirrer != null) noteReached(level.getPlayerByUUID(lastStirrer));
            sync();
        }

        if (waterLevel > 0 && !level.isClientSide()) {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition).inflate(1.125));
            if (items.isEmpty()) cooldown = 20;

            for (ItemEntity entity : items) {
                ItemStack thrown = entity.getItem();
                if (!(thrown.getItem() instanceof ItemIngredient) || cooldown >= 1) continue;

                int taken = 0;
                while (taken < thrown.getCount() && plan(thrown)) taken++;
                if (taken == 0) continue;

                thrown.shrink(taken);
                if (thrown.isEmpty()) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                } else {
                    entity.setItem(thrown);
                }

                cooldown = 20;
                needsUpdate = true;
                level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        if (this.cooldown > 0) this.cooldown--;

        updateWaterColor();

        if (!level.isClientSide() && needsUpdate) {
            sync();
            needsUpdate = false;
        }
    }

    public static final int SPOILED_COLOR = 0x3B3226;

    private void updateWaterColor() {
        if (spoiled) {
            potion_color = SPOILED_COLOR;
            return;
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        List<MobEffectInstance> effects = getEffectList().stream().toList();

        if (!effects.isEmpty()) {
            int count = Math.min(effects.size(), 3);

            for (int i = 0; i < count; i++) {
                int color = effects.get(i).getEffect().value().getColor();
                red += (color >> 16) & 255;
                green += (color >> 8) & 255;
                blue += color & 255;
            }

            red = Math.max(0, Math.min(255, red / count));
            green = Math.max(0, Math.min(255, green / count));
            blue = Math.max(0, Math.min(255, blue / count));
            potion_color = (red << 16) | (green << 8) | blue;
        } else {
            refreshBaseColor();
        }
    }

    public void empty() {
        if (level == null) return;
        BlockPos pos = getBlockPos();
        ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
        waterLevel = 0;
        brewName = "";
        base = BrewBases.defaultBase();
        instability = 0;
        setDefaultResult();
        resetEffectList();

        refreshBaseColor();

        needsUpdate = true;

        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.SHROOMLIGHT_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            sync();
        }
    }

    public int getPotionColor() {
        return potion_color;
    }
}
