package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemAlbedo;
import muddykat.alchemia.common.items.ItemCitrinitas;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemNigredo;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.common.items.ItemRubedo;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewModifiers;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.PotionGlass;
import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.common.utility.ParticleUtils;
import muddykat.alchemia.common.utility.PotionStacks;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import muddykat.alchemia.registration.registers.PotionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.world.InteractionResult.FAIL;
import static net.minecraft.world.InteractionResult.SUCCESS;

public class TileEntityAlchemyCauldron extends SyncedBlockEntity implements MenuProvider, Nameable {

    public static final int ACTIVE_SLOT = 0;
    private static final int RETIRED_REDSTONE_SLOT = 1;
    private static final int RETIRED_GUNPOWDER_SLOT = 2;
    public static final int TOOL_SLOT = 3;
    private static final int INVENTORY_SLOT_COUNT = 4;
    public static final int MAX_QUEUE = 12;

    public static final double STIR_STEP = 2.0;
    public static final double STIR_FINE_STEP = 0.5;
    public static final int FAST_STIRS = 2;
    private static final int STIR_BUDGET = 10;
    private static final int STIR_SOUND_INTERVAL = 12;
    private static final int FINISH_LIMIT = 4096;
    private static final int CONDITION_CHECK_INTERVAL = 20;
    private static final int EVENT_CONDITIONS_SHIFTED = 1;
    private static final int EVENT_BREWED = 2;
    private static final int BUBBLE_LIFE = 60;
    private static final int BUBBLE_RUSH_TICKS = 40;
    private static final int AMBIENT_BUBBLES = 3;
    private static final int RUSH_BUBBLES = 8;
    private static final int STIRRED_UP_TICKS = 60;

    private static final int ALIGNMENT_SPACE = 1;


    private static final int LEGACY_SLOT_COUNT = 8;
    private static final int LEGACY_REDSTONE_SLOT = 6;
    private static final int LEGACY_GUNPOWDER_SLOT = 7;
    public static final int REDSTONE_DURATION_BONUS = 1200;

    private int waterLevel;
    private final int maxWaterLevel = BrewBase.MAX_FILL_LEVEL;
    private ItemStackHandler inventory;

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
    private boolean linedUp = false;
    private PathPoint watchedAt;
    private PotionEnum watchedEffect;
    private int watchedStrength;
    private Set<WorldEffects.Condition> watchedHolding = Set.of();
    private int stirredUp;
    private int bubbleRush;
    private final java.util.ArrayDeque<Long> bubbles = new java.util.ArrayDeque<>();
    private boolean stirredUpRising;
    private @Nullable WorldEffects.Rule stirredUpBy;
    private String brewName = "";
    private boolean spoiled = false;
    private BrewBase base = BrewBases.defaultBase();
    private int instability = 0;
    private int bonusInstability = 0;
    private int durationBonus = 0;
    private boolean lingeringRequested = false;
    private boolean splash = false;
    private int potion_color = 0xffffff;

    private List<MobEffectInstance> effectList = new ArrayList<>();
    private List<String> potencyOrder = new ArrayList<>();
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
    public void load(CompoundTag tag) {
        super.load(tag);

        List<ItemStack> legacyQueue = new ArrayList<>();
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            inventory = resizeInventory(inventory, legacyQueue);
        }
        waterLevel = tag.getInt("waterLevel");
        xAlignment = tag.contains("xAlignment") ? tag.getInt("xAlignment") : balanceAlignment;
        yAlignment = tag.contains("yAlignment") ? tag.getInt("yAlignment") : balanceAlignment;

        if (tag.getInt("alignmentSpace") < ALIGNMENT_SPACE) {
            xAlignment = (int) Math.round(xAlignment * PotionMap.CELL);
            yAlignment = (int) Math.round(yAlignment * PotionMap.CELL);
        }
        needsUpdate = tag.getBoolean("needsUpdate");
        cooldown = tag.getInt("cooldown");

        addedIngredients.clear();
        addedIngredients.addAll(strings(tag.getList("addedIngredients", Tag.TAG_STRING)));

        effectList.clear();
        ListTag effects = tag.getList("effects", Tag.TAG_COMPOUND);
        for (int index = 0; index < effects.size(); index++) {
            MobEffectInstance instance = MobEffectInstance.load(effects.getCompound(index));
            if (instance != null) effectList.add(instance);
        }

        brewName = tag.getString("brewName");
        spoiled = tag.getBoolean("spoiled");
        base = BrewBases.byId(tag.contains("base") ? tag.getString("base") : BrewBases.DEFAULT_ID);
        instability = tag.getInt("instability");
        bonusInstability = tag.getInt("bonusInstability");
        durationBonus = tag.getInt("durationBonus");
        potencyOrder = new ArrayList<>(strings(tag.getList("potencyOrder", Tag.TAG_STRING)));
        syncOrder();
        splash = tag.getBoolean("splash");
        stirCount = tag.getInt("stirCount");

        script.clear();
        if (tag.contains("courseX")) {
            script.addAll(strings(tag.getList("script", Tag.TAG_STRING)));

            List<ItemStack> entries = new ArrayList<>();
            ListTag planned = tag.getList("course", Tag.TAG_COMPOUND);
            for (int index = 0; index < planned.size(); index++) entries.add(ItemStack.of(planned.getCompound(index)));

            List<Integer> anchors = new ArrayList<>();
            for (int anchor : tag.getIntArray("courseAnchors")) anchors.add(anchor);

            List<Double> anchorFrom = new ArrayList<>();
            ListTag partials = tag.getList("courseAnchorFrom", Tag.TAG_DOUBLE);
            for (int index = 0; index < partials.size(); index++) anchorFrom.add(partials.getDouble(index));

            course.restore(tag.getDouble("courseX"), tag.contains("courseY") ? tag.getDouble("courseY") : yAlignment,
                    entries, anchors, anchorFrom, tag.getInt("courseSpent"), tag.getDouble("courseProgress"));
        } else {
            script.addAll(BrewRecipe.legacySteps(addedIngredients));

            course.reset(xAlignment, yAlignment);
            for (ItemStack stack : legacyQueue) plan(stack);
        }

        refreshAlignment();
        updateWaterColor();
    }

    private static List<String> strings(ListTag list) {
        List<String> values = new ArrayList<>(list.size());
        for (int index = 0; index < list.size(); index++) values.add(list.getString(index));
        return values;
    }

    private static ListTag stringTags(List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) list.add(StringTag.valueOf(value));
        return list;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("waterLevel", waterLevel);
        tag.putInt("xAlignment", xAlignment);
        tag.putInt("yAlignment", yAlignment);
        tag.putInt("alignmentSpace", ALIGNMENT_SPACE);
        tag.putBoolean("needsUpdate", needsUpdate);
        tag.putInt("cooldown", cooldown);
        tag.put("addedIngredients", stringTags(addedIngredients));
        tag.put("script", stringTags(script));

        ListTag planned = new ListTag();
        for (ItemStack stack : course.entries()) planned.add(stack.save(new CompoundTag()));
        tag.put("course", planned);
        tag.putIntArray("courseAnchors", course.anchors());
        ListTag partials = new ListTag();
        for (double partial : course.anchorFrom()) partials.add(net.minecraft.nbt.DoubleTag.valueOf(partial));
        tag.put("courseAnchorFrom", partials);
        tag.putDouble("courseX", course.originX());
        tag.putDouble("courseY", course.originY());
        tag.putInt("courseSpent", course.spent());
        tag.putDouble("courseProgress", course.progress());
        tag.putInt("stirCount", stirCount);

        ListTag effects = new ListTag();
        for (MobEffectInstance instance : effectList) effects.add(instance.save(new CompoundTag()));
        tag.put("effects", effects);

        tag.putString("brewName", brewName);
        tag.putBoolean("spoiled", spoiled);
        tag.putString("base", base.id());
        tag.putInt("instability", instability);
        tag.putInt("bonusInstability", bonusInstability);
        tag.putInt("durationBonus", durationBonus);
        ListTag order = new ListTag();
        for (String token : potencyOrder) order.add(net.minecraft.nbt.StringTag.valueOf(token));
        tag.put("potencyOrder", order);
        tag.putBoolean("splash", splash);
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

            if (!splash) {
                message(player, "alchemia.brew.needs_gunpowder");
                return InteractionResult.FAIL;
            }
            lingeringRequested = true;
        }

        if (heldItem.equals(Items.GLASS_BOTTLE) || heldItem.equals(Items.DRAGON_BREATH) || heldItem instanceof ItemPotionGlass) {
            if (getWaterLevel() > 0) {
                ItemStack potion = drawPotion(heldItem instanceof ItemPotionGlass glass ? glass.glass() : null);
                Collection<MobEffectInstance> brewed = List.copyOf(getEffectList());
                List<String> used = recipeEntries(addedIngredients, "");
                List<String> steps = recipeEntries(script, String.valueOf(BrewRecipe.STEP_ADD));
                heldStack.shrink(1);
                if (!player.getInventory().add(potion)) player.drop(potion, false);

                if (!level.isClientSide() && !spoiled
                        && BrewRecipeBook.record(player, base, brewed, used, steps, getBrewName()) > 0) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                if (!level.isClientSide() && !spoiled && RecipeDiscovery.record(player, brewed) > 0) {
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.4F, 1.6F);
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(Component.translatable("alchemia.guide.discovered"), true);
                    }
                }

                settleAfterDraw();
                return InteractionResult.CONSUME;
            }
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, pos);
        }

        return SUCCESS;
    }

    public boolean canFillFrom(ItemStack stack) {
        return canFill() && BrewBases.forItem(stack) != null;
    }

    public ItemStack fillFrom(ItemStack stack) {
        BrewBases.Match match = BrewBases.forItem(stack);
        if (match == null || level == null) return stack;

        addFill(match.base(), match.fill().levels());
        announceFill(level, getBlockPos(), match.fill().levels());
        Item remainder = match.fill().remainderValue();
        return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
    }

    public boolean holdsFinishedBrew() {
        return waterLevel > 0 && !spoiled && !effectList.isEmpty();
    }

    public ItemStack bottle(ItemStack vessel) {
        ItemStack potion = drawPotion(vessel.getItem() instanceof ItemPotionGlass glass ? glass.glass() : null);
        settleAfterDraw();
        return potion;
    }

    private ItemStack drawPotion(@Nullable PotionGlass glass) {
        takeWaterPortion();
        ItemStack potion = getPotion();
        if (glass != null) PotionGlasses.apply(potion, glass);
        return potion;
    }

    private void settleAfterDraw() {
        if (level == null) return;
        BlockPos pos = getBlockPos();

        lingeringRequested = false;

        if (getWaterLevel() < 1) {
            resetEffectList();
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.SPLASH, pos);
                sync();
            }
        }

        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.SPLASH, pos);
            sync();
        }
    }

    public InteractionResult replaySelectedRecipe(Player player, ItemStack guide) {
        Level level = getLevel();
        BlockPos pos = getBlockPos();
        if (level == null) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        BrewRecipe recipe = BrewRecipeBook.selectedRecipe(guide);
        if (recipe == null || !recipe.isResolvable()) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.none_selected"), true);
            return InteractionResult.FAIL;
        }

        if (getWaterLevel() <= 0) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.no_water"), true);
            return InteractionResult.FAIL;
        }

        if (!recipe.brewBase().equals(base)) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.wrong_recipe_base",
                    recipe.brewBase().displayName()), true);
            return InteractionResult.FAIL;
        }

        if (!isUntouched() || course.planned() > 0) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.not_clean"), true);
            return InteractionResult.FAIL;
        }

        Component heldBack = heldBack(recipe);
        if (heldBack != null) {
            serverPlayer.displayClientMessage(heldBack, true);
            return InteractionResult.FAIL;
        }

        if (recipe.requiresGrinding() && !playerHasMortar(player)) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.needs_mortar"), true);
            return InteractionResult.FAIL;
        }

        if (recipe.requiresTrimming() && !carries(player, ItemNigredo.class)) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.needs_nigredo"), true);
            return InteractionResult.FAIL;
        }

        if (recipe.requiresTurning() && !carries(player, ItemAlbedo.class) && !carries(player, ItemCitrinitas.class)) {
            serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.needs_turning"), true);
            return InteractionResult.FAIL;
        }

        Map<ItemStack, Integer> required = requiredTally(recipe);

        for (Map.Entry<ItemStack, Integer> entry : required.entrySet()) {
            if (countIn(player, entry.getKey()) < entry.getValue()) {
                serverPlayer.displayClientMessage(Component.translatable("alchemia.brew.missing", entry.getKey().getHoverName()), true);
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
        serverPlayer.displayClientMessage(Component.translatable(brewedAsRecorded(recipe) ? "alchemia.brew.done" : "alchemia.brew.drifted"), true);
        sync();

        return InteractionResult.CONSUME;
    }

    private @Nullable Component heldBack(BrewRecipe recipe) {
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            PotionEnum potion = effect.asPotion();
            if (potion == null) continue;

            WorldEffects.Requirement requirement = WorldEffects.requirement(potion);
            if (requirement != null && !requirement.holds(level, getBlockPos())) {
                return Component.translatable("alchemia.brew.recipe_dormant", effect.displayName(), requirement.hint());
            }

            int cap = levelCap(potion);
            if (effect.potency() > cap) {
                Component hint = WorldEffects.levelHint(potion, cap + 1);
                return Component.translatable("alchemia.brew.recipe_held_back", effect.displayName(),
                        hint == null ? Component.empty() : hint);
            }
        }
        return null;
    }

    private boolean brewedAsRecorded(BrewRecipe recipe) {
        if (spoiled || effectList.size() != recipe.effects().size()) return false;

        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            PotionEnum potion = effect.asPotion();
            if (potion == null || heldPotency(potion.getEffect()) != effect.potency()) return false;
        }
        return true;
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
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemMortarPestle) return true;
        }
        return false;
    }

    private static boolean carries(Player player, Class<?> tool) {
        for (ItemStack stack : player.getInventory().items) {
            if (tool.isInstance(stack.getItem())) return true;
        }
        return false;
    }

    private static int countIn(Player player, ItemStack wanted) {
        int found = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (sameIngredient(stack, wanted)) found += stack.getCount();
        }
        return found;
    }

    private static void consumeFrom(Player player, ItemStack wanted, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!sameIngredient(stack, wanted)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
    }

    private Item resultType() {
        if (lingeringRequested) return Items.LINGERING_POTION;
        return splash ? Items.SPLASH_POTION : Items.POTION;
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

    private ItemStackHandler resizeInventory(ItemStackHandler saved, List<ItemStack> queue) {
        if (saved.getSlots() == INVENTORY_SLOT_COUNT) return saved;

        ItemStackHandler resized = createHandler();
        if (saved.getSlots() < LEGACY_SLOT_COUNT) {
            for (int slot = 0; slot < Math.min(saved.getSlots(), INVENTORY_SLOT_COUNT); slot++) {
                resized.setStackInSlot(slot, saved.getStackInSlot(slot));
            }
            return resized;
        }

        resized.setStackInSlot(ACTIVE_SLOT, saved.getStackInSlot(0));
        resized.setStackInSlot(RETIRED_REDSTONE_SLOT, saved.getStackInSlot(LEGACY_REDSTONE_SLOT));
        resized.setStackInSlot(RETIRED_GUNPOWDER_SLOT, saved.getStackInSlot(LEGACY_GUNPOWDER_SLOT));

        for (int slot = 1; slot < LEGACY_REDSTONE_SLOT; slot++) {
            ItemStack stack = saved.getStackInSlot(slot);
            for (int copy = 0; copy < stack.getCount() && queue.size() < MAX_QUEUE; copy++) {
                queue.add(stack.copyWithCount(1));
            }
        }
        return resized;
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(INVENTORY_SLOT_COUNT) {
            @Override
            public int getSlotLimit(int slot) {
                if (slot == RETIRED_REDSTONE_SLOT || slot == RETIRED_GUNPOWDER_SLOT) return 0;
                return slot == ACTIVE_SLOT ? 64 : 1;
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
        ItemStack heldStack = player.getItemInHand(hand);
        IFluidHandlerItem held = FluidUtil.getFluidHandler(heldStack.copyWithCount(1)).orElse(null);
        if (held == null) return null;

        for (int tank = 0; tank < held.getTanks(); tank++) {
            FluidStack resource = held.getFluidInTank(tank);
            if (resource.isEmpty()) continue;

            BrewBases.Match match = BrewBases.forFluid(resource);
            if (match == null) continue;
            if (!canAcceptFill(match.base(), match.fill().levels())) return refuseFill(player, match.base());

            int amount = match.fill().fluidAmount();
            FluidStack drained = held.drain(new FluidStack(resource, amount), IFluidHandler.FluidAction.EXECUTE);
            if (drained.getAmount() < amount) continue;

            if (!player.isCreative()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, held.getContainer()));
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
                ? BiomeColors.getAverageWaterColor(level, getBlockPos())
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

    public ItemStackHandler getInventory() {
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
        return inventory.getStackInSlot(ACTIVE_SLOT).copy();
    }

    public ItemStack getTool() {
        return inventory.getStackInSlot(TOOL_SLOT).copy();
    }

    public boolean hasTool() {
        return IngredientTool.of(getTool()) != null;
    }

    public boolean canApplyTool() {
        IngredientTool tool = IngredientTool.of(getTool());
        return tool != null && tool.canApply(getActiveIngredient());
    }

    public boolean canQueue() {
        ItemStack active = getActiveIngredient();
        BrewModifiers modifier = BrewModifiers.of(active);
        if (modifier != null) return modifierRefusal(modifier) == null;
        return !spoiled && !queueFull() && isBrewingInput(active);
    }

    public @Nullable String modifierRefusal(BrewModifiers modifier) {
        if (spoiled) return "alchemia.brew.ruined";
        if (waterLevel <= 0) return "alchemia.brew.no_water";
        return switch (modifier) {
            case REDSTONE -> {
                PotencyPlan plan = rotated(null);
                yield plan == null || plan.equals(currentPotency()) ? "alchemia.brew.redstone_full" : null;
            }
            case GUNPOWDER -> splash ? "alchemia.brew.already_splash" : null;
        };
    }

    private boolean applyModifier(BrewModifiers modifier) {
        if (modifierRefusal(modifier) != null) return false;
        return switch (modifier) {
            case REDSTONE -> addRedstoneCharge();
            case GUNPOWDER -> makeSplash();
        };
    }

    public boolean canStir() {
        return !spoiled && waterLevel > 0 && course.canStir();
    }

    private void setActive(ItemStack stack) {
        inventory.setStackInSlot(ACTIVE_SLOT, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    public InteractionResult queueActive(Player player) {
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack active = getActiveIngredient();
        BrewModifiers modifier = BrewModifiers.of(active);
        if (modifier != null) return addModifier(player, active, modifier);

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

    private InteractionResult addModifier(Player player, ItemStack active, BrewModifiers modifier) {
        String refusal = modifierRefusal(modifier);
        if (refusal != null) {
            message(player, refusal);
            return InteractionResult.FAIL;
        }
        if (!applyModifier(modifier)) return InteractionResult.FAIL;

        active.shrink(1);
        setActive(active);

        BlockPos pos = getBlockPos();
        if (modifier == BrewModifiers.GUNPOWDER) {
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 1.4F);
        } else {
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 0.8F);
        }
        level.gameEvent(null, GameEvent.SPLASH, pos);
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

        inventory.setStackInSlot(TOOL_SLOT, held.isEmpty() ? ItemStack.EMPTY : held.copy());

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

        if (WorldEffects.note(player, entry.effect().getRecipe())) writeGuideSound(player);
    }

    private boolean linedUpHere() {
        if (spoiled || waterLevel <= 0 || !PotionMap.isReady()) return false;

        PotionMap.PotionEffectPosition found = effectAt(getBrewPosition());
        return found.getEffect() != null && found.isPerfect() && !isDormant(found);
    }

    private void chimeIfLinedUp() {
        boolean now = linedUpHere();
        if (now && !linedUp && level != null) {
            level.playSound(null, getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        linedUp = now;
    }

    private int brewableStrength(PotionMap.PotionEffectPosition found) {
        if (found.getEffect() == null || isDormant(found)) return 0;
        return found.getStrength() + 1;
    }

    private void watchConditions() {
        if (spoiled || waterLevel <= 0 || !PotionMap.isReady()) {
            watchedAt = null;
            watchedEffect = null;
            watchedHolding = Set.of();
            return;
        }

        PathPoint at = getBrewPosition();
        PotionMap.PotionEffectPosition found = effectAt(at);
        int strength = brewableStrength(found);

        Set<WorldEffects.Condition> holding = found.getRecipe() == null
                ? Set.of() : WorldEffects.holding(found.getRecipe(), level, getBlockPos());

        boolean unmoved = at.equals(watchedAt) && found.getRecipe() == watchedEffect;
        if (unmoved && found.getRecipe() != null && strength != watchedStrength && level != null) {
            boolean rising = strength > watchedStrength;
            WorldEffects.Rule cause = WorldEffects.shifted(watchedHolding, holding);
            int param = (rising ? 1 : 0) | (cause == null ? 0 : cause.ordinal() + 1) << 1;

            level.playSound(null, getBlockPos(), rising ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), EVENT_CONDITIONS_SHIFTED, param);
        }

        watchedAt = at;
        watchedEffect = found.getRecipe();
        watchedStrength = strength;
        watchedHolding = holding;
        linedUp = linedUpHere();
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_BREWED) {
            bubbleRush = BUBBLE_RUSH_TICKS;
            return true;
        }
        if (id != EVENT_CONDITIONS_SHIFTED) return super.triggerEvent(id, param);

        int cause = (param >> 1) - 1;
        stirredUp = STIRRED_UP_TICKS;
        stirredUpRising = (param & 1) != 0;
        stirredUpBy = cause >= 0 && cause < WorldEffects.Rule.values().length ? WorldEffects.Rule.values()[cause] : null;
        return true;
    }

    private static @Nullable ParticleOptions shiftAccent(@Nullable WorldEffects.Rule cause) {
        if (cause == null) return null;
        return switch (cause) {
            case FULL_MOON, NEW_MOON, MOONLIGHT -> ParticleTypes.END_ROD;
            case THUNDER -> ParticleTypes.ELECTRIC_SPARK;
            case RAIN -> ParticleTypes.SPLASH;
            case CLEAR -> ParticleTypes.WAX_OFF;
            case DAY -> ParticleTypes.WAX_ON;
            case NIGHT -> ParticleTypes.GLOW;
            default -> null;
        };
    }

    public int heldPotency(MobEffect effect) {
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

    public record PotencyPlan(List<MobEffectInstance> effects, List<String> order) {
        public int redstone() {
            int charges = 0;
            for (String token : order) {
                if (token.equals(REDSTONE_TOKEN)) charges++;
            }
            return charges;
        }
    }

    public static final String REDSTONE_TOKEN = "redstone";

    public static String tokenOf(MobEffect effect) {
        return String.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(effect));
    }

    public PotencyPlan currentPotency() {
        return new PotencyPlan(List.copyOf(effectList), List.copyOf(potencyOrder));
    }

    public @Nullable PotencyPlan brewedWith(PotionMap.PotionEffectPosition found) {
        MobEffect brewing = found.getEffect();
        if (brewing == null) return null;
        return rotated(new MobEffectInstance(brewing, found.getDuration(), found.getStrength()));
    }

    private @Nullable PotencyPlan rotated(@Nullable MobEffectInstance incoming) {
        String incomingToken = incoming == null ? REDSTONE_TOKEN : tokenOf(incoming.getEffect());
        int incomingPotency = incoming == null ? 1 : incoming.getAmplifier() + 1;

        Map<String, MobEffectInstance> byToken = new java.util.HashMap<>();
        for (MobEffectInstance instance : effectList) byToken.put(tokenOf(instance.getEffect()), instance);

        List<String> older = new ArrayList<>(potencyOrder);
        if (incoming != null) older.remove(incomingToken);

        int overflow = incomingPotency - maxPotency();
        for (String token : older) {
            MobEffectInstance instance = byToken.get(token);
            overflow += instance == null ? 1 : instance.getAmplifier() + 1;
        }

        List<String> order = new ArrayList<>();
        List<MobEffectInstance> effects = new ArrayList<>();
        for (String token : older) {
            MobEffectInstance instance = byToken.get(token);
            int potency = instance == null ? 1 : instance.getAmplifier() + 1;
            int lost = Math.max(0, Math.min(overflow, potency));
            overflow -= lost;
            if (lost == potency) continue;

            order.add(token);
            if (instance != null) {
                effects.add(lost == 0 ? instance
                        : new MobEffectInstance(instance.getEffect(), instance.getDuration(), instance.getAmplifier() - lost));
            }
        }
        if (overflow > 0) return null;

        order.add(incomingToken);
        if (incoming != null) effects.add(incoming);
        return new PotencyPlan(effects, order);
    }

    private void apply(PotencyPlan plan) {
        effectList = new ArrayList<>(plan.effects());
        potencyOrder = new ArrayList<>(plan.order());
        durationBonus = plan.redstone() * REDSTONE_DURATION_BONUS;
    }

    private void syncOrder() {
        List<String> present = new ArrayList<>();
        for (MobEffectInstance instance : effectList) present.add(tokenOf(instance.getEffect()));

        List<String> order = new ArrayList<>();
        for (String token : potencyOrder) {
            if (token.equals(REDSTONE_TOKEN) || (present.contains(token) && !order.contains(token))) order.add(token);
        }
        for (String token : present) {
            if (!order.contains(token)) order.add(token);
        }

        int charges = durationBonus / REDSTONE_DURATION_BONUS;
        int listed = 0;
        for (String token : order) {
            if (token.equals(REDSTONE_TOKEN)) listed++;
        }
        while (listed > charges) {
            order.remove(REDSTONE_TOKEN);
            listed--;
        }
        while (listed < charges) {
            order.add(REDSTONE_TOKEN);
            listed++;
        }

        List<MobEffectInstance> sorted = new ArrayList<>();
        for (String token : order) {
            for (MobEffectInstance instance : effectList) {
                if (tokenOf(instance.getEffect()).equals(token)) sorted.add(instance);
            }
        }
        effectList = sorted;
        potencyOrder = order;
    }

    private boolean effectReady(PathPoint at) {
        PotionMap.PotionEffectPosition found = effectAt(at);
        return found.getEffect() != null && gainAt(at) > 0 && !isDormant(found) && brewedWith(found) != null;
    }

    public BrewCheck checkBrew(PathPoint at) {
        if (spoiled || waterLevel <= 0 || !PotionMap.isReady()) return BrewCheck.UNAVAILABLE;
        if (effectReady(at)) return BrewCheck.READY;

        PotionMap.PotionEffectPosition found = effectAt(at);
        if (found.getEffect() != null && isDormant(found)) return BrewCheck.DORMANT;
        if (found.getEffect() != null && gainAt(at) > 0) return BrewCheck.TOO_POTENT;
        return found.getEffect() != null ? BrewCheck.WEAKER : BrewCheck.NOTHING;
    }

    private boolean brewEffect() {
        PathPoint at = getBrewPosition();
        if (!effectReady(at)) return false;

        applyEffectPosition(effectAt(at));
        script.add(String.valueOf(BrewRecipe.STEP_BREW));
        updateWaterColor();
        if (level != null && !level.isClientSide()) {
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), EVENT_BREWED, 0);
        }
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
                MobEffect effect = effectAt(getBrewPosition()).getEffect();
                if (brewEffect()) recordInGuide(player, effect);

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

    private void recordInGuide(Player player, MobEffect effect) {
        if (player == null || level == null) return;

        PotionEnum recipe = RecipeDiscovery.byEffect(effect);
        boolean recorded = RecipeDiscovery.record(player, List.of(new MobEffectInstance(effect))) > 0;
        boolean revealed = recipe != null && WorldEffects.reveal(player, recipe, heldPotency(effect));
        if (recorded || revealed) writeGuideSound(player);
    }

    private void writeGuideSound(Player player) {
        if (level == null) return;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
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

    public boolean stir(Player player, int stirs) {
        if (level == null || level.isClientSide()) return false;

        stirCount++;
        boolean stirred = false;
        if (stirBudget > 0) {
            for (int count = 0; count < stirs && !teleporting() && stirOnce(false); count++) stirred = true;
        }

        if (stirred) {
            stirBudget--;
            refreshAlignment();
            if (player != null) lastStirrer = player.getUUID();
            noteReached(player);
            chimeIfLinedUp();

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
            case BrewRecipe.STEP_SPLASH -> makeSplash();
            default -> {
            }
        }
    }

    private boolean addRedstoneCharge() {
        if (spoiled) return false;

        PotencyPlan plan = rotated(null);
        if (plan == null) return false;

        apply(plan);
        addedIngredients.add(BrewRecipe.encode(BuiltInRegistries.ITEM.getKey(Items.REDSTONE).toString(), 0));
        script.add(String.valueOf(BrewRecipe.STEP_REDSTONE));
        updateWaterColor();
        return true;
    }

    private boolean makeSplash() {
        if (spoiled || splash) return false;

        splash = true;
        addedIngredients.add(BrewRecipe.encode(BuiltInRegistries.ITEM.getKey(Items.GUNPOWDER).toString(), 0));
        script.add(String.valueOf(BrewRecipe.STEP_SPLASH));
        return true;
    }

    private static void message(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(key), true);
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
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return null;

        String name = stack.getItem() instanceof ItemIngredient ? id.getPath() : id.toString();
        return BrewRecipe.encode(name, ItemIngredient.crushOf(stack), ItemIngredient.trimOf(stack),
                ItemIngredient.rotationOf(stack));
    }

    public static boolean isAddable(ItemStack stack) {
        return isBrewingInput(stack) || BrewModifiers.isModifier(stack);
    }

    public int redstoneCharges() {
        return durationBonus / REDSTONE_DURATION_BONUS;
    }

    public boolean isSplash() {
        return splash;
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
        potencyOrder.clear();
        durationBonus = 0;
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

        MobEffect mobEffect = potionEffectPos.getEffect();
        PotencyPlan brewed = brewedWith(potionEffectPos);
        if (mobEffect == null || brewed == null) return;

        BlockPos pos = getBlockPos();
        boolean perfect = potionEffectPos.isPerfect();
        boolean held = heldPotency(mobEffect) > 0;

        apply(brewed);

        if (held) {
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
            ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
        }

        if (perfect && level.isClientSide()) {
            for (int i = 0; i < 10; i++) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + random.nextFloat(), pos.getY() + 1D, pos.getZ() + random.nextFloat(), 0, 0.1, 0);
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
        splash = false;
        effectList = new ArrayList<>();
        potencyOrder = new ArrayList<>();
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
        syncOrder();
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public boolean checkForEffect(MobEffect effect, int duration) {
        return effectList.stream().noneMatch(instance -> instance.getEffect().equals(effect) && instance.getDuration() == duration);
    }

    public void ensureStrength(MobEffect effect, int effectStrength) {
        boolean replace = false;
        for (MobEffectInstance instance : effectList) {
            if (instance.getEffect().equals(effect) && instance.getAmplifier() < effectStrength) {
                replace = true;
            }
        }

        if (replace) {
            effectList.removeIf(instance -> instance.getEffect().equals(effect));
            effectList.add(new MobEffectInstance(effect, 1200, effectStrength));
            syncOrder();
        }
    }

    public ItemStack getPotion() {
        ItemStack customPotion = new ItemStack(resultType());
        if (spoiled) {
            PotionStacks.apply(customPotion, PotionRegistry.RUINED.get(), List.of(), SPOILED_COLOR);
            return customPotion;
        }
        if (getEffectList().isEmpty()) {
            ItemStack unbrewed = base.unbrewedStack();
            if (!unbrewed.isEmpty()) return unbrewed;

            PotionUtils.setPotion(customPotion, Potions.WATER);
            return customPotion;
        }

        List<MobEffectInstance> brewed = new ArrayList<>();
        for (MobEffectInstance instance : getEffectList()) {
            brewed.add(new MobEffectInstance(instance.getEffect(),
                    instance.getDuration() + durationBonus, instance.getAmplifier()));
        }

        PotionStacks.apply(customPotion, PotionRegistry.ALCHEMICAL.get(), brewed, getPotionColor());
        if (!brewName.isEmpty()) {
            customPotion.setHoverName(Component.literal(brewName).withStyle(style -> style.withItalic(false)));
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

    private void returnRetiredSlots(BlockPos pos) {
        for (int slot : new int[]{RETIRED_REDSTONE_SLOT, RETIRED_GUNPOWDER_SLOT}) {
            ItemStack leftover = inventory.getStackInSlot(slot);
            if (leftover.isEmpty()) continue;

            inventory.setStackInSlot(slot, ItemStack.EMPTY);
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, leftover.copy());
            setChanged();
        }
    }

    public void dropContents(BlockPos pos) {
        if (level == null) return;
        NonNullList<ItemStack> contents = NonNullList.create();
        for (int slot = 0; slot < inventory.getSlots(); slot++) contents.add(inventory.getStackInSlot(slot));
        contents.addAll(course.unstarted());
        Containers.dropContents(level, pos, contents);
    }

    public void tick() {
        if (level == null) return;
        BlockPos pos = getBlockPos();

        if (level.isClientSide()) {
            if (random.nextFloat() < 0.2F && waterLevel > 0 && !getEffectList().isEmpty()) {
                int color = getEffectList().stream().findFirst()
                        .map(instance -> instance.getEffect().getColor())
                        .orElse(MobEffects.WATER_BREATHING.getColor());

                ParticleUtils.generatePotionParticles(level, pos, color, false);
            }

            if (stirredUp > 0 && waterLevel > 0) {
                ParticleUtils.generateStirredUpParticles(level, pos, AlchemicalCauldronRenderer.surfaceHeight(waterLevel),
                        shiftAccent(stirredUpBy), stirredUpRising, stirredUp);
            }
            if (stirredUp > 0) stirredUp--;

            bubble(pos);

            if (random.nextFloat() < 0.2f) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + .1D;
                double z = pos.getZ() + random.nextDouble();
                ParticleOptions options = ParticleTypes.FLAME;
                level.addParticle(options, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }

        if (stirBudget < STIR_BUDGET) stirBudget++;

        if (!level.isClientSide()) returnRetiredSlots(pos);

        if (!level.isClientSide() && teleporting() && stirOnce(false)) {
            refreshAlignment();
            if (!teleporting() && lastStirrer != null) noteReached(level.getPlayerByUUID(lastStirrer));
            chimeIfLinedUp();
            sync();
        }

        if (!level.isClientSide() && level.getGameTime() % CONDITION_CHECK_INTERVAL == 0) watchConditions();

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

    private void bubble(BlockPos pos) {
        if (level == null) return;
        long now = level.getGameTime();
        while (!bubbles.isEmpty() && bubbles.peekFirst() <= now) bubbles.pollFirst();

        boolean brewing = waterLevel > 0 && !spoiled && !effectList.isEmpty();
        boolean rushing = bubbleRush > 0;
        if (rushing) bubbleRush--;
        if (!brewing && !rushing) return;
        if (waterLevel <= 0 || level.getBlockState(pos.above()).canOcclude()) return;

        int cap = rushing ? RUSH_BUBBLES : AMBIENT_BUBBLES;
        float chance = rushing ? 0.6F : bubbles.isEmpty() ? 0.08F : 0.025F;
        if (bubbles.size() >= cap || random.nextFloat() >= chance) return;

        int color = getPotionColor();
        level.addParticle(ParticleTypeRegistry.BREW_BUBBLE.get(),
                pos.getX() + 0.2 + random.nextDouble() * 0.6,
                pos.getY() + AlchemicalCauldronRenderer.surfaceHeight(waterLevel) + 0.003,
                pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                ((color >> 16) & 255) / 255.0, ((color >> 8) & 255) / 255.0, (color & 255) / 255.0);
        bubbles.addLast(now + BUBBLE_LIFE);
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
                int color = effects.get(i).getEffect().getColor();
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
