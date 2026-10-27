package muddykat.alchemia.common.blocks.tileentity;

import com.mojang.serialization.Codec;
import muddykat.alchemia.common.blocks.tileentity.cauldron.BrewPotency;
import muddykat.alchemia.common.blocks.tileentity.cauldron.BrewReplay;
import muddykat.alchemia.common.blocks.tileentity.cauldron.BrewScript;
import muddykat.alchemia.common.blocks.tileentity.cauldron.BrewStability;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronAmbience;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronBoiler;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronBrewing;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronColour;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronFilling;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronImbuement;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronInteraction;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronInventory;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronJournal;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronQueue;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronRuin;
import muddykat.alchemia.common.blocks.tileentity.cauldron.CauldronStirrer;
import muddykat.alchemia.common.blocks.tileentity.cauldron.ConditionWatch;
import muddykat.alchemia.common.blocks.tileentity.cauldron.PotencyPlan;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.BrewModifiers;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.CauldronHeat;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.common.utility.ParticleUtils;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import muddykat.alchemia.common.utility.ValueInput;
import muddykat.alchemia.common.utility.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TileEntityAlchemyCauldron extends SyncedBlockEntity implements MenuProvider, Nameable {

    public static final int ACTIVE_SLOT = CauldronInventory.ACTIVE_SLOT;
    public static final int TOOL_SLOT = CauldronInventory.TOOL_SLOT;
    public static final int MAX_QUEUE = 12;
    public static final int DATA_COUNT = 5;
    public static final int DATA_HEAT = 3;
    public static final int DATA_BOIL = 4;
    public static final int FAST_TOOL_DIVISOR = 3;

    public static final double STIR_STEP = 2.0;
    public static final double STIR_FINE_STEP = 0.5;
    public static final int FAST_STIRS = 2;
    private static final int FINISH_LIMIT = 4096;
    private static final int CONDITION_CHECK_INTERVAL = 20;
    private static final int ALIGNMENT_SPACE = 1;
    public static final int SPOILED_COLOR = 0x3B3226;

    private int waterLevel;
    private final int maxWaterLevel = BrewBase.MAX_FILL_LEVEL;

    private int xAlignment;
    private int yAlignment;

    private final int balanceAlignment;
    private Item potion_type = Items.POTION;

    private int cooldown = 0;

    private boolean needsUpdate = false;
    private final BrewCourse course = new BrewCourse(0.0, 0.0);
    private String brewName = "";
    private BrewBase base = BrewBases.defaultBase();
    private boolean lingeringRequested = false;
    private boolean splash = false;
    private Component customName;

    private final BrewPotency potency = new BrewPotency();
    private final BrewStability stability = new BrewStability();
    private final CauldronImbuement imbuement = new CauldronImbuement();
    private final BrewScript script = new BrewScript();
    private final CauldronBoiler boiler = new CauldronBoiler();
    private final ConditionWatch watch = new ConditionWatch(this);
    private final CauldronAmbience ambience = new CauldronAmbience(this);
    private final CauldronInventory inventory = new CauldronInventory(MAX_QUEUE);
    private final CauldronJournal journal = new CauldronJournal(this);
    private final CauldronStirrer stirrer = new CauldronStirrer(this);
    private final CauldronQueue queue = new CauldronQueue(this);
    private final CauldronColour colour = new CauldronColour();

    protected ContainerData alchemicalCauldronData;

    public TileEntityAlchemyCauldron(BlockPos pos, BlockState state) {
        this(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), pos, state);
    }

    public TileEntityAlchemyCauldron(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        alchemicalCauldronData = createIntArray();
        waterLevel = 0;
        this.balanceAlignment = map().getMiddlePosition();
        this.xAlignment = map().getMiddlePosition();
        this.yAlignment = map().getMiddlePosition();
        this.course.reset(xAlignment, yAlignment);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);

        List<ItemStack> legacyQueue = new ArrayList<>(input.read("queue", ItemStack.CODEC.listOf()).orElse(List.of()));

        inventory.load(input, legacyQueue);
        waterLevel = input.getIntOr("waterLevel", 0);
        xAlignment = input.getIntOr("xAlignment", balanceAlignment);
        yAlignment = input.getIntOr("yAlignment", balanceAlignment);

        if (input.getIntOr("alignmentSpace", 0) < ALIGNMENT_SPACE) {
            xAlignment = (int) Math.round(xAlignment * PotionMap.CELL);
            yAlignment = (int) Math.round(yAlignment * PotionMap.CELL);
        }
        needsUpdate = input.getBooleanOr("needsUpdate", false);
        cooldown = input.getIntOr("cooldown", 0);

        List<String> addedIngredients = script.added();
        addedIngredients.clear();
        addedIngredients.addAll(input.read("addedIngredients", Codec.STRING.listOf()).orElse(List.of()));

        potency.load(input);
        brewName = input.getString("brewName").orElse("");
        stability.load(input);
        base = BrewBases.byId(input.getString("base").orElse(BrewBases.DEFAULT_ID));
        splash = input.getBooleanOr("splash", false);
        imbuement.load(input);
        stirrer.restoreCount(input.getIntOr("stirCount", 0));

        script.steps().clear();
        Optional<Double> courseX = input.read("courseX", Codec.DOUBLE);

        if (courseX.isPresent()) {
            script.steps().addAll(input.read("script", Codec.STRING.listOf()).orElse(List.of()));
            course.restore(courseX.get(), input.read("courseY", Codec.DOUBLE).orElse((double) yAlignment),
                    input.read("course", ItemStack.CODEC.listOf()).orElse(List.of()),
                    input.read("courseAnchors", Codec.INT.listOf()).orElse(List.of()),
                    input.read("courseAnchorFrom", Codec.DOUBLE.listOf()).orElse(List.of()),
                    input.getIntOr("courseSpent", 0),
                    input.read("courseProgress", Codec.DOUBLE).orElse(0.0),
                    input.read("courseHeadSkip", Codec.DOUBLE).orElse(0.0));
        } else {
            List<String> upgraded = BrewRecipe.upgradeEntries(addedIngredients);
            addedIngredients.clear();
            addedIngredients.addAll(upgraded);
            script.steps().addAll(BrewRecipe.legacySteps(upgraded));

            course.reset(xAlignment, yAlignment);
            for (ItemStack stack : legacyQueue) plan(stack);
        }

        refreshAlignment();
        updateWaterColor();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        inventory.save(output);
        output.putInt("waterLevel", waterLevel);
        output.putInt("xAlignment", xAlignment);
        output.putInt("yAlignment", yAlignment);
        output.putInt("alignmentSpace", ALIGNMENT_SPACE);
        output.putBoolean("needsUpdate", needsUpdate);
        output.putInt("cooldown", cooldown);
        output.store("addedIngredients", Codec.STRING.listOf(), List.copyOf(script.added()));
        output.store("script", Codec.STRING.listOf(), List.copyOf(script.steps()));
        output.store("course", ItemStack.CODEC.listOf(), course.entries());
        output.store("courseAnchors", Codec.INT.listOf(), course.anchors());
        output.store("courseAnchorFrom", Codec.DOUBLE.listOf(), course.anchorFrom());
        output.store("courseX", Codec.DOUBLE, course.originX());
        output.store("courseY", Codec.DOUBLE, course.originY());
        output.putInt("courseSpent", course.spent());
        output.store("courseProgress", Codec.DOUBLE, course.progress());
        output.store("courseHeadSkip", Codec.DOUBLE, course.headSkip());
        output.putInt("stirCount", stirrer.count());
        potency.save(output);
        output.putString("brewName", brewName);
        stability.save(output);
        output.putString("base", base.id());
        output.putBoolean("splash", splash);
        imbuement.save(output);
    }

    @Override
    public InteractionResult onActivated(BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        return CauldronInteraction.use(this, pos, player, hand);
    }

    public boolean canFillFrom(ItemStack stack) {
        return canFill() && BrewBases.forItem(stack) != null;
    }

    public ItemStack fillFrom(ItemStack stack) {
        BrewBases.Match match = BrewBases.forItem(stack);
        if (match == null || level == null) return stack;

        addFill(match.base(), match.fill().levels());
        CauldronFilling.announce(level, getBlockPos(), match.fill().levels());
        Item remainder = match.fill().remainderValue();
        return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
    }

    public boolean holdsFinishedBrew() {
        return waterLevel > 0 && !isSpoiled() && !potency.effects().isEmpty();
    }

    public ItemStack bottle(ItemStack vessel) {
        return CauldronInteraction.bottle(this, vessel);
    }

    public void requestLingering(boolean requested) {
        lingeringRequested = requested;
    }

    public InteractionResult replaySelectedRecipe(Player player, ItemStack guide) {
        return replay(player, BrewRecipeBook.selectedRecipe(guide));
    }

    public InteractionResult replayFromDesk(Player player) {
        return BrewReplay.fromDesk(this, player);
    }

    public InteractionResult replay(Player player, @Nullable BrewRecipe recipe) {
        return BrewReplay.replay(this, player, recipe);
    }

    public void performAll(List<String> steps) {
        for (String step : steps) {
            perform(step);
            if (isSpoiled()) break;
        }
        refreshAlignment();
    }

    public Item resultType() {
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

    public void setFullWater() {
        addFill(BrewBases.defaultBase(), BrewBase.MAX_FILL_LEVEL);
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
        colour.refreshBase(this);
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
        return inventory.slots();
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
        return stirrer.count();
    }

    public PathPoint getBrewPosition() {
        return PotionMap.isReady() ? course.position(map()) : new PathPoint(xAlignment, yAlignment);
    }

    public void refreshAlignment() {
        if (!PotionMap.isReady()) return;

        PathPoint at = course.position(map());
        xAlignment = at.roundedX();
        yAlignment = at.roundedY();
    }

    public boolean plan(ItemStack stack) {
        if (isSpoiled() || waterLevel <= 0 || queueFull() || !isBrewingInput(stack)) return false;

        String entry = BrewScript.encodeOf(stack);
        if (entry == null) return false;

        course.add(stack);
        script.ingredient(entry);
        setChanged();
        return true;
    }

    public ItemStack getActiveIngredient() {
        return inventory.active();
    }

    public ItemStack getTool() {
        return inventory.tool();
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
        return !isSpoiled() && waterLevel > 0 && !queueFull() && isBrewingInput(active);
    }

    public @Nullable String modifierRefusal(BrewModifiers modifier) {
        if (isSpoiled()) return "alchemia.brew.ruined";
        if (waterLevel <= 0) return "alchemia.brew.no_water";
        return switch (modifier) {
            case REDSTONE -> {
                PotencyPlan plan = potency.rotated(null);
                yield plan == null || plan.equals(currentPotency()) ? "alchemia.brew.redstone_full" : null;
            }
            case GUNPOWDER -> splash ? "alchemia.brew.already_splash" : null;
            case GLOWSTONE -> imbuement.primed() ? "alchemia.brew.already_primed" : null;
            case IMBUEMENT -> {
                Imbuements.Attuner offered = Imbuements.of(getActiveIngredient());
                yield offered == null || offered.blank() ? "alchemia.brew.bark_blank" : imbuement.clashing(offered) ? "alchemia.brew.already_imbued" : null;
            }
        };
    }

    public boolean applyModifier(BrewModifiers modifier) {
        if (modifierRefusal(modifier) != null) return false;
        return switch (modifier) {
            case REDSTONE -> addRedstoneCharge();
            case GLOWSTONE -> prime();
            case GUNPOWDER -> makeSplash();
            case IMBUEMENT -> attune(Imbuements.of(getActiveIngredient()));
        };
    }

    public boolean canStir() {
        return !isSpoiled() && waterLevel > 0 && course.canStir();
    }

    public BrewPotency potency() {
        return potency;
    }

    public BrewStability stability() {
        return stability;
    }

    public BrewScript script() {
        return script;
    }

    public CauldronImbuement imbuement() {
        return imbuement;
    }

    public CauldronBoiler boiler() {
        return boiler;
    }

    public ConditionWatch watch() {
        return watch;
    }

    public CauldronInventory inventory() {
        return inventory;
    }

    public CauldronJournal journal() {
        return journal;
    }

    public InteractionResult queueActive(Player player) {
        return queue.queueActive(player);
    }

    public InteractionResult unqueue(Player player, int index) {
        return queue.unqueue(player, index);
    }

    public boolean applyTool(Player player, boolean fast) {
        return queue.applyTool(player, fast);
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
        return found.getRecipe() != null && WorldEffects.dormant(found.getRecipe(), level, getBlockPos(), attunedHolder());
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (ambience.event(id, param)) return true;
        return super.triggerEvent(id, param);
    }

    public int heldPotency(MobEffect effect) {
        return potency.held(effect);
    }

    public PotionMap.PotionEffectPosition rawEffectAt(PathPoint at) {
        return map().getEffectPotion(at);
    }

    public int levelCap(AlchemiaEffect recipe) {
        return WorldEffects.levelCap(recipe, level, getBlockPos(), attunedHolder());
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
        return Math.max(0, found.getStrength() + glowBoost(found) + 1 - heldPotency(found.getEffect()));
    }

    public PotencyPlan currentPotency() {
        return potency.current();
    }

    public @Nullable PotencyPlan brewedWith(PotionMap.PotionEffectPosition found) {
        MobEffect brewing = found.getEffect();
        if (brewing == null) return null;
        return potency.rotated(new MobEffectInstance(brewing, found.getDuration(), found.getStrength() + glowBoost(found)));
    }

    public boolean effectReady(PathPoint at) {
        PotionMap.PotionEffectPosition found = effectAt(at);
        return found.getEffect() != null && gainAt(at) > 0 && !isDormant(found) && brewedWith(found) != null;
    }

    public BrewCheck checkBrew(PathPoint at) {
        if (isSpoiled() || waterLevel <= 0 || !PotionMap.isReady()) return BrewCheck.UNAVAILABLE;
        if (effectReady(at)) return BrewCheck.READY;

        PotionMap.PotionEffectPosition found = effectAt(at);
        if (found.getEffect() != null && isDormant(found)) return BrewCheck.DORMANT;
        if (found.getEffect() != null && gainAt(at) > 0) return BrewCheck.TOO_POTENT;
        return found.getEffect() != null ? BrewCheck.WEAKER : BrewCheck.NOTHING;
    }

    public InteractionResult brew(Player player) {
        return CauldronBrewing.brew(this, player);
    }

    public boolean canHeat() {
        return waterLevel > 0 && !isSpoiled() && PotionMap.isReady();
    }

    public double heatIntensity() {
        return boiler.intensity(waterLevel);
    }

    public void stoke(@Nullable Player player) {
        if (level == null || level.isClientSide() || !canHeat() || boiler.boiling()) return;

        boolean full = boiler.pump(level.getGameTime(), waterLevel, player);
        journal.reveal(player);
        if (!full) return;

        if (checkBrew(getBrewPosition()) == BrewCheck.READY) {
            boiler.boil(waterLevel);
            brew(player);
        } else {
            boiler.strain(waterLevel);
        }
    }

    private void tickHeat() {
        if (level == null || !canHeat()) {
            boiler.douse();
            return;
        }

        boiler.tick(level.getGameTime(), waterLevel);
        stirrer.swirl();
    }

    public void revealFor(@Nullable Player player) {
        journal.reveal(player);
    }

    public boolean stir(Player player, int stirs) {
        return stirrer.stir(player, stirs);
    }

    private void perform(String step) {
        if (step.isEmpty()) return;

        char kind = step.charAt(0);
        String value = step.substring(1);

        switch (kind) {
            case BrewRecipe.STEP_ADD -> plan(BrewRecipe.stackFor(value));
            case BrewRecipe.STEP_WHIRL -> {
                double[] drift = BrewScript.driftOf(value);
                if (drift != null && PotionMap.isReady()) {
                    course.rebase(map());
                    course.shift(drift[0], drift[1]);
                    refreshAlignment();
                }
            }
            case BrewRecipe.STEP_STIR -> stirrer.stirRepeatedly(BrewScript.countOf(step), false);
            case BrewRecipe.STEP_FINE -> stirrer.stirRepeatedly(BrewScript.countOf(step), true);
            case BrewRecipe.STEP_FINISH -> stirrer.stirRepeatedly(FINISH_LIMIT, false);
            case BrewRecipe.STEP_BREW -> CauldronBrewing.brewEffect(this);
            case BrewRecipe.STEP_REDSTONE -> addRedstoneCharge();
            case BrewRecipe.STEP_SPLASH -> makeSplash();
            case BrewRecipe.STEP_GLOW -> prime();
            case BrewRecipe.STEP_BARK -> attune(Imbuements.parse(step));
            default -> {
            }
        }
    }

    private boolean addRedstoneCharge() {
        if (isSpoiled()) return false;

        PotencyPlan plan = potency.rotated(null);
        if (plan == null) return false;

        potency.apply(plan);
        script.modifier(Items.REDSTONE, BrewRecipe.STEP_REDSTONE);
        updateWaterColor();
        return true;
    }

    private boolean attune(@Nullable Imbuements.Attuner offered) {
        if (isSpoiled() || offered == null || offered.blank() || imbuement.clashing(offered)) return false;
        imbuement.add(offered);
        script.step(offered.step());
        return true;
    }

    public List<Imbuements.Attuner> getImbuements() {
        return imbuement.attuners();
    }

    public WorldEffects.Imbued attunedHolder() {
        return imbuement.holder(level);
    }

    private boolean prime() {
        if (isSpoiled() || imbuement.primed()) return false;

        imbuement.prime();
        script.modifier(Items.GLOWSTONE_DUST, BrewRecipe.STEP_GLOW);
        return true;
    }

    public boolean isPrimed() {
        return imbuement.primed();
    }

    public boolean isRadiant() {
        return imbuement.radiant();
    }

    public int glowBoost(PotionMap.PotionEffectPosition found) {
        if (!imbuement.primed() || found.getEffect() == null) return 0;
        int cap = levelCap(found.getRecipe());
        if (cap != Integer.MAX_VALUE && found.getStrength() + 1 > cap - 1) return 0;
        return 1;
    }

    private boolean makeSplash() {
        if (isSpoiled() || splash) return false;

        splash = true;
        script.modifier(Items.GUNPOWDER, BrewRecipe.STEP_SPLASH);
        return true;
    }

    public static void message(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(key), true);
        }
    }

    public boolean isUntouched() {
        return script.onlyAdds() && potency.effects().isEmpty() && stability.instability() == 0;
    }

    public static boolean isBrewingInput(ItemStack stack) {
        return stack.getItem() instanceof ItemIngredient || VanillaIngredients.isVanillaIngredient(stack);
    }

    public static boolean isAddable(ItemStack stack) {
        return isBrewingInput(stack) || BrewModifiers.isModifier(stack);
    }

    public int redstoneCharges() {
        return potency.redstoneCharges();
    }

    public boolean isSplash() {
        return splash;
    }

    public int getDurationBonus() {
        return potency.durationBonus();
    }

    public boolean isSpoiled() {
        return stability.spoiled();
    }

    public BrewBase getBase() {
        return base;
    }

    private PotionMap map() {
        return PotionMap.get(base);
    }

    public int getInstability() {
        return stability.instability();
    }

    public double getBrewHealth() {
        return stability.health();
    }

    public int getInstabilityCap() {
        return stability.cap(base);
    }

    public boolean hasBonusInstability() {
        return stability.hasBonus();
    }

    public boolean addRubedo(Player player) {
        return CauldronRuin.rubedo(this, player);
    }

    public boolean destabilise() {
        return CauldronRuin.destabilise(this);
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
                    case 3 -> (int) Math.round(TileEntityAlchemyCauldron.this.boiler.heat() * CauldronHeat.SYNC_SCALE);
                    case 4 -> TileEntityAlchemyCauldron.this.boiler.boilTicks();
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
                return DATA_COUNT;
            }
        };
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AlchemicalCauldronMenu(id, playerInventory, this, alchemicalCauldronData);
    }

    public static int maxPotency() {
        return BrewPotency.max();
    }

    public int totalPotency() {
        return potency.total();
    }

    public Collection<MobEffectInstance> getEffectList() {
        return potency.effects();
    }

    public void resetEffectList() {
        List<ItemStack> kept = course.unstarted();

        script.clear();
        stability.reset();
        splash = false;
        imbuement.reset();
        potency.clear();
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
        potency.add(mobEffectInstance);
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public boolean checkForEffect(MobEffect effect, int duration) {
        return potency.lacks(effect, duration);
    }

    public void ensureStrength(MobEffect effect, int effectStrength) {
        potency.ensureStrength(effect, effectStrength);
    }

    public ItemStack getPotion() {
        return CauldronInteraction.potion(this);
    }

    private void markUpdated() {
        this.setChanged();
        if (this.getLevel() != null) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void dropContents(BlockPos pos) {
        if (level == null) return;

        NonNullList<ItemStack> contents = NonNullList.create();
        contents.addAll(inventory.contents());
        contents.addAll(course.unstarted());
        Containers.dropContents(level, pos, contents);
    }

    public void tick() {
        if (level == null) return;
        BlockPos pos = getBlockPos();

        if (level.isClientSide()) ambience.tick(level, pos);

        stirrer.regain();
        if (!level.isClientSide()) tickHeat();

        if (!level.isClientSide() && inventory.returnRetired(level, pos)) setChanged();

        if (!level.isClientSide() && stirrer.teleporting() && stirrer.stirOnce(false)) {
            refreshAlignment();
            if (!stirrer.teleporting() && stirrer.hasStirrer()) {
                Player stirredBy = stirrer.lastStirrer(level);
                journal.reached(stirredBy);
                journal.reveal(stirredBy);
            }
            watch.chimeIfLinedUp();
            sync();
        }

        if (!level.isClientSide() && level.getGameTime() % CONDITION_CHECK_INTERVAL == 0) watch.watch();

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

    public void updateWaterColor() {
        colour.update(this);
    }

    public void empty() {
        if (level == null) return;
        BlockPos pos = getBlockPos();
        ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
        waterLevel = 0;
        brewName = "";
        base = BrewBases.defaultBase();
        stability.calm();
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
        return colour.rgb();
    }
}
