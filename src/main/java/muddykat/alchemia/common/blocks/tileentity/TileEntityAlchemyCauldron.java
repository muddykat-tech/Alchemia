package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewBase;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static net.minecraft.world.InteractionResult.FAIL;
import static net.minecraft.world.InteractionResult.SUCCESS;

public class TileEntityAlchemyCauldron extends SyncedBlockEntity implements MenuProvider, Nameable {

    private static final int INGREDIENT_SLOT_COUNT = 6;
    public static final int REDSTONE_SLOT = 6;
    public static final int GUNPOWDER_SLOT = 7;
    private static final int INVENTORY_SLOT_COUNT = 8;
    public static final int REDSTONE_DURATION_BONUS = 1200;

    private int waterLevel;
    private final int maxWaterLevel = 4;
    private ItemStacksResourceHandler inventory;

    private int xAlignment;
    private int yAlignment;

    private final int balanceAlignment;
    private Item potion_type = Items.POTION;

    private final Random random = new Random();
    private int cooldown = 0;

    private final List<ItemStack> contents = new ArrayList<>();
    private int additionTimer = 20;
    private boolean needsUpdate = false;
    private final List<String> addedIngredients = new ArrayList<>();
    private String brewName = "";
    private boolean spoiled = false;
    private BrewBase base = BrewBase.WATER;
    private int instability = 0;
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Inventory").ifPresent(saved -> {
            inventory.deserialize(saved);
            inventory = resizeInventory(inventory);
        });
        waterLevel = input.getIntOr("waterLevel", 0);
        xAlignment = input.getIntOr("xAlignment", balanceAlignment);
        yAlignment = input.getIntOr("yAlignment", balanceAlignment);
        needsUpdate = input.getBooleanOr("needsUpdate", false);
        cooldown = input.getIntOr("cooldown", 0);

        addedIngredients.clear();
        addedIngredients.addAll(input.read("addedIngredients", Codec.STRING.listOf()).orElse(List.of()));

        effectList.clear();
        effectList.addAll(input.read("effects", MobEffectInstance.CODEC.listOf()).orElse(List.of()));
        brewName = input.getString("brewName").orElse("");
        spoiled = input.getBooleanOr("spoiled", false);
        base = BrewBase.byName(input.getString("base").orElse(BrewBase.WATER.getSerializedName()));
        instability = input.getIntOr("instability", 0);
        durationBonus = input.getIntOr("durationBonus", 0);
        updateWaterColor();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
        output.putInt("waterLevel", waterLevel);
        output.putInt("xAlignment", xAlignment);
        output.putInt("yAlignment", yAlignment);
        output.putBoolean("needsUpdate", needsUpdate);
        output.putInt("cooldown", cooldown);
        output.store("addedIngredients", Codec.STRING.listOf(), List.copyOf(addedIngredients));
        output.store("effects", MobEffectInstance.CODEC.listOf(), List.copyOf(effectList));
        output.putString("brewName", brewName);
        output.putBoolean("spoiled", spoiled);
        output.putString("base", base.getSerializedName());
        output.putInt("instability", instability);
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

        BrewBase fullBase = BrewBase.byFullFill(heldStack);
        if (fullBase != null) {
            if (canFill()) {
                Item remainder = fullBase.fullFillRemainder();
                if (!player.isCreative()) {
                    if (remainder != null) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(remainder)));
                    } else {
                        heldStack.shrink(1);
                        player.setItemInHand(hand, heldStack);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(heldItem));
                setFullBase(fullBase);
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                }

                return SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        BrewBase portionBase = BrewBase.byPortionFill(heldStack);
        if (portionBase != null) {
            if (!canAddPortion(portionBase)) {
                if (waterLevel > 0 && portionBase != base) message(player, "alchemia.brew.wrong_base");
                return InteractionResult.FAIL;
            }

            Item remainder = portionBase.portionRemainder();
            if (!player.isCreative()) {
                if (remainder != null) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(remainder)));
                } else {
                    heldStack.shrink(1);
                    player.setItemInHand(hand, heldStack);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(heldItem));
            addBasePortion(portionBase);
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return SUCCESS;
        }

        if (heldItem instanceof ItemIngredient) {
            if (getWaterLevel() > 0) {
                if (!level.isClientSide()) IngredientDiscovery.record(player, heldStack);
                addIngredient(heldStack.copyWithCount(1));
                heldStack.shrink(1);
                player.setItemInHand(hand, heldStack);

                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        if (heldItem.equals(Items.DRAGON_BREATH)) {
            if (getWaterLevel() <= 0) return InteractionResult.FAIL;

            if (!hasGunpowder()) {
                message(player, "alchemia.brew.needs_gunpowder");
                return InteractionResult.FAIL;
            }
            lingeringRequested = true;
        }

        if (heldItem.equals(Items.GLASS_BOTTLE) || heldItem.equals(Items.DRAGON_BREATH)) {
            if (getWaterLevel() > 0) {
                takeWaterPortion();
                ItemStack potion = getPotion();
                Collection<MobEffectInstance> brewed = List.copyOf(getEffectList());
                List<String> used = getAddedIngredients();
                heldStack.shrink(1);
                player.getInventory().add(potion);

                if (hasGunpowder() && !level.isClientSide()) {
                    inventory.set(GUNPOWDER_SLOT, ItemResource.EMPTY, 0);
                }
                lingeringRequested = false;

                if (!level.isClientSide() && !spoiled) BrewRecipeBook.record(player, base, brewed, used, getBrewName());

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

        if (recipe.brewBase() != base) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.wrong_recipe_base",
                    Component.translatable(recipe.brewBase().translationKey())));
            return InteractionResult.FAIL;
        }

        if (!isUntouched()) {
            serverPlayer.sendOverlayMessage(Component.translatable("alchemia.brew.not_clean"));
            return InteractionResult.FAIL;
        }

        if (recipe.requiresGrinding() && !hasMortar(player)) {
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

        for (ItemStack recipeStack : recipe.resolve()) {
            if (!level.isClientSide()) IngredientDiscovery.record(player, recipeStack);
            addIngredient(recipeStack);
        }

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

    private static boolean hasMortar(Player player) {
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

    private ItemStacksResourceHandler resizeInventory(ItemStacksResourceHandler saved) {
        if (saved.size() >= INVENTORY_SLOT_COUNT) return saved;

        ItemStacksResourceHandler resized = createHandler();
        for (int slot = 0; slot < saved.size(); slot++) {
            resized.set(slot, saved.getResource(slot), saved.getAmountAsInt(slot));
        }
        return resized;
    }

    private ItemStacksResourceHandler createHandler() {
        return new ItemStacksResourceHandler(INVENTORY_SLOT_COUNT) {
            @Override
            protected int getCapacity(int index, ItemResource resource) {
                return index == REDSTONE_SLOT ? 64 : 1;
            }
        };
    }

    public void setFullWater() {
        setFullBase(BrewBase.WATER);
    }

    public void setFullBase(BrewBase filled) {
        resetEffectList();
        this.base = filled;
        this.waterLevel = this.maxWaterLevel;

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);

        refreshBaseColor();

        needsUpdate = true;
        markUpdated();

        if (level != null && !level.isClientSide()) sync();
    }

    public void addBasePortion(BrewBase filled) {
        if (waterLevel == 0) {
            resetEffectList();
            this.base = filled;
            alchemicalCauldronData.set(1, xAlignment);
            alchemicalCauldronData.set(2, yAlignment);
        }

        this.waterLevel = Math.min(this.maxWaterLevel, this.waterLevel + 1);
        refreshBaseColor();

        needsUpdate = true;
        markUpdated();

        if (level != null && !level.isClientSide()) sync();
    }

    public boolean canAddPortion(BrewBase filled) {
        if (waterLevel >= maxWaterLevel) return false;
        return waterLevel == 0 || (base == filled && isUntouched());
    }

    private void refreshBaseColor() {
        if (level == null || !level.isClientSide()) return;
        potion_color = base.usesBiomeColor()
                ? BiomeColors.getAverageWaterColor((BlockAndTintGetter) level, getBlockPos())
                : base.fluidColor();
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
        List<ItemStack> queued = new ArrayList<>();
        for (int slot = 0; slot < INGREDIENT_SLOT_COUNT; slot++) {
            int amount = inventory.getAmountAsInt(slot);
            if (amount <= 0) continue;
            ItemResource resource = inventory.getResource(slot);
            if (isBrewingInput(resource.toStack(1))) {
                for (int i = 0; i < amount; i++) queued.add(resource.toStack(1));
            }
        }
        return queued;
    }

    public InteractionResult commitQueue(Player player) {
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

        List<ItemStack> queued = getQueuedIngredients();
        if (queued.isEmpty() && !hasRedstone()) {
            message(player, "alchemia.brew.empty_queue");
            return InteractionResult.FAIL;
        }
        if (waterLevel <= 0) {
            message(player, "alchemia.brew.no_water");
            return InteractionResult.FAIL;
        }

        for (int slot = 0; slot < INGREDIENT_SLOT_COUNT; slot++) {
            inventory.set(slot, ItemResource.EMPTY, 0);
        }

        for (ItemStack queuedStack : queued) {
            if (!level.isClientSide()) IngredientDiscovery.record(player, queuedStack);
            addIngredient(queuedStack);
        }

        applyRedstone();

        BlockPos pos = getBlockPos();
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.1F);
        level.gameEvent(null, GameEvent.SPLASH, pos);
        setChanged();
        sync();

        return InteractionResult.SUCCESS;
    }

    public void addRedstoneCharge() {
        if (spoiled || totalPotency() >= maxPotency()) return;

        durationBonus += REDSTONE_DURATION_BONUS;
        addedIngredients.add(BrewRecipe.encode(BuiltInRegistries.ITEM.getKey(Items.REDSTONE).toString(), 0));
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

    public int[] previewAlignment(List<ItemStack> queued) {
        int x = xAlignment;
        int y = yAlignment;
        for (ItemStack queuedStack : queued) {
            int[] landing = map().step(x, y, queuedStack);
            x = landing[0];
            y = landing[1];
        }
        return new int[]{x, y};
    }

    public List<String> getAddedIngredients() {
        return List.copyOf(addedIngredients);
    }

    public boolean isUntouched() {
        return addedIngredients.isEmpty();
    }

    public boolean wouldStall(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient)) return false;
        if (!PotionMap.isReady()) return false;

        int[] landing = map().step(xAlignment, yAlignment, stack);
        return landing[0] == xAlignment && landing[1] == yAlignment;
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

    public void addIngredient(ItemStack stack) {
        if (stack.is(Items.REDSTONE)) {
            addRedstoneCharge();
            return;
        }

        if (!isBrewingInput(stack)) return;

        BlockPos here = getBlockPos();
        PotionMap.PotionPath path = map().walk(this.xAlignment, this.yAlignment, stack);

        if (path.dead()) {
            String deadEntry = encodeOf(stack);
            if (deadEntry != null) addedIngredients.add(deadEntry);

            if (!destabilise()) return;

            int[] stranded = path.cells().get(0);
            this.xAlignment = stranded[0];
            this.yAlignment = stranded[1];
            alchemicalCauldronData.set(1, xAlignment);
            alchemicalCauldronData.set(2, yAlignment);

            markUpdated();
            if (level != null && !level.isClientSide()) sync();
            return;
        }

        if (path.stalled()) {
            if (level != null && !level.isClientSide()) {
                level.playSound(null, here, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.7F, 0.6F);
                sync();
            }
            return;
        }

        String entry = encodeOf(stack);
        if (entry != null) addedIngredients.add(entry);

        List<int[]> cells = path.cells();
        for (int i = 0; i < cells.size() - 1; i++) {
            PotionMap.PotionEffectPosition passed = map().getEffectAt(cells.get(i)[0], cells.get(i)[1]);
            if (passed != null) applyEffectPosition(passed);
        }

        int[] landing = cells.get(cells.size() - 1);
        this.xAlignment = landing[0];
        this.yAlignment = landing[1];

        updateEffectList();
        BlockPos pos = getBlockPos();

        if (totalPotency() > maxPotency()) {
            spoil();
            return;
        }

        updateWaterColor();
        if (level != null && !level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.SPLASH, pos);

            sync();
        }
    }

    private void updateEffectList() {
        if (level == null) return;

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);

        int[] alignment = {getXAlignment(), getYAlignment()};

        applyEffectPosition(map().getEffectPotion(alignment));

        BlockPos pos = getBlockPos();
        if (level.isClientSide() && additionTimer < 1) {
            for (int i = 0; i < 10; i++) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SPLASH, pos.getX() + .5d, pos.getY() + 1D, pos.getZ() + .5d, 0, 0.1, 0);
            }
            additionTimer = 20;
        }

        updateWaterColor();
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
        return base.instabilityCap();
    }

    public Map<Holder<MobEffect>, Integer> previewPotencies(List<ItemStack> queued) {
        Map<Holder<MobEffect>, Integer> potencies = new LinkedHashMap<>();
        for (MobEffectInstance instance : effectList) {
            potencies.put(instance.getEffect(), instance.getAmplifier() + 1);
        }
        if (!PotionMap.isReady()) return potencies;

        int x = xAlignment;
        int y = yAlignment;
        int simulated = instability;

        for (ItemStack stack : queued) {
            if (stack.is(Items.REDSTONE)) continue;

            PotionMap.PotionPath path = map().walk(x, y, stack);
            if (path.dead()) {
                simulated++;
                if (simulated >= base.instabilityCap()) return null;

                int[] stranded = path.cells().get(0);
                x = stranded[0];
                y = stranded[1];
                continue;
            }
            if (path.stalled()) continue;

            List<int[]> cells = path.cells();
            for (int i = 0; i < cells.size() - 1; i++) {
                PotionMap.PotionEffectPosition passed = map().getEffectAt(cells.get(i)[0], cells.get(i)[1]);
                if (passed != null && passed.getEffect() != null) {
                    potencies.put(passed.getEffect(), passed.getStrength() + 1);
                }
            }

            int[] end = path.end(x, y);
            x = end[0];
            y = end[1];

            PotionMap.PotionEffectPosition landing = map().getEffectPotion(new int[]{x, y});
            if (landing.getEffect() != null) {
                potencies.put(landing.getEffect(), landing.getStrength() + 1);
            }
        }
        return potencies;
    }

    public int previewPotency(List<ItemStack> queued) {
        Map<Holder<MobEffect>, Integer> potencies = previewPotencies(queued);
        if (potencies == null) return -1;

        int total = redstoneCharges();
        for (int potency : potencies.values()) total += potency;
        return total;
    }

    private boolean destabilise() {
        instability++;
        if (instability >= base.instabilityCap()) {
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
        instability = base.instabilityCap();
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
        addedIngredients.clear();
        spoiled = false;
        instability = 0;
        durationBonus = 0;
        effectList.clear();
        effectList = new HashSet<>();
        xAlignment = map().getMiddlePosition();
        yAlignment = map().getMiddlePosition();

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);
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
            ItemStack unbrewed = base.unbrewedResult();
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

        if (additionTimer > 0) additionTimer--;
        if (waterLevel > 0) {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition).inflate(1.125));
            if (items.isEmpty()) cooldown = 20;
            for (ItemEntity entity : items) {
                ItemStack stack = entity.getItem().copy();
                int stack_size = stack.getCount();
                if (stack.getItem() instanceof ItemIngredient && cooldown < 1) {
                    for (int i = 0; i < stack_size; i++) {
                        contents.add(stack.copyWithCount(1));
                    }

                    cooldown = 20;
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    needsUpdate = true;
                }
            }

            Iterator<ItemStack> iterator = contents.iterator();
            while (iterator.hasNext()) {
                addIngredient(iterator.next());
                iterator.remove();
                if (!level.isClientSide()) sync();
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
        base = BrewBase.WATER;
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
