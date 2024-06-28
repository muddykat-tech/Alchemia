package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientCrushed;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.utility.ParticleUtils;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;

import java.util.*;

import static muddykat.alchemia.Alchemia.proxy;

public class TileEntityAlchemyCauldron extends SyncedBlockEntity implements MenuProvider, Nameable {

    private int waterLevel;
    private final int maxWaterLevel = 4;
    private final ItemStackHandler inventory;

    private int xAlignment;
    private int yAlignment;

    private final int maxAlignment;
    private final int balanceAlignment;
    private Item potion_type = Items.POTION;

    public TileEntityAlchemyCauldron(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get(), pPos, pBlockState);
        alchemicalCauldronData = createIntArray();
        inventory = createHandler();
        waterLevel = 0;
        this.balanceAlignment = PotionMap.INSTANCE.getMiddlePosition();
        this.xAlignment = PotionMap.INSTANCE.getMiddlePosition();
        this.yAlignment = PotionMap.INSTANCE.getMiddlePosition();
        this.maxAlignment = PotionMap.INSTANCE.getMaxAlignment();
    }

    private Random random = new Random();

    @Override
    public void load(@NotNull CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Inventory")) {
            inventory.deserializeNBT(compound.getCompound("Inventory"));
        } else {
            inventory.deserializeNBT(compound);
        }
        waterLevel = compound.getInt("waterLevel");

        xAlignment = compound.getInt("xAlignment");
        yAlignment = compound.getInt("yAlignment");
        needsUpdate = compound.getBoolean("needsUpdate");
        updateEffectList();
        // TODO: Update Effect List!!!!
        //updateWaterColor();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        writeItems(compound);
        compound.putInt("waterLevel", waterLevel);
        compound.putInt("xAlignment", xAlignment);
        compound.putInt("yAlignment", yAlignment);
        compound.putBoolean("needsUpdate", needsUpdate);
    }

    private CompoundTag writeItems(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Inventory", inventory.serializeNBT());
        return compound;
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public void setSplashResult()
    {
        potion_type = Items.SPLASH_POTION;
        BlockPos pos = getBlockPos();
        Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5d, pos.getY() + 1, pos.getZ() + 0.5d, 0, 0.1, 0);
        if(!level.isClientSide) sync();
    }

    public void setLingeringResult()
    {
        potion_type = Items.LINGERING_POTION;
    }

    public void setDefaultResult()
    {
        potion_type = Items.POTION;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return writeItems(new CompoundTag());
    }

    private static final int INVENTORY_SLOT_COUNT = 6;

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(INVENTORY_SLOT_COUNT)
        {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    public void setFullWater() {
        if(waterLevel <= 1){
            resetEffectList();
        }

        this.waterLevel = this.maxWaterLevel;
        int oldY = this.yAlignment;
        int oldX = this.xAlignment;
        int newX = (int) (oldX + (balanceAlignment - oldX) * 0.2);
        int newY = (int) (oldY + (balanceAlignment - oldY) * 0.2);

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);

        potion_color = BiomeColors.getAverageWaterColor(getLevel(), getBlockPos());
        needsUpdate = true;
        markUpdated();
        if(!level.isClientSide) sync();
    }

    public boolean shouldRenderFace(Direction pFace) {
        return pFace.getAxis() == Direction.Axis.Y;
    }

    public void addIngredient(ItemIngredient itemIngredient) {
        Ingredients ingredient = itemIngredient.getIngredient();
        this.xAlignment += (ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX()) * (ingredient.getIngredientStrength() * (itemIngredient instanceof ItemIngredientCrushed ? ingredient.getCrushedPotency() : 1));
        this.yAlignment += (ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY()) * (ingredient.getIngredientStrength() * (itemIngredient instanceof ItemIngredientCrushed ? ingredient.getCrushedPotency() : 1));

        if(xAlignment < -maxAlignment) {
            xAlignment = -maxAlignment;
        }

        if(xAlignment > maxAlignment){
            xAlignment = maxAlignment;
        }

        if(yAlignment < -maxAlignment) {
            yAlignment = -maxAlignment;
        }

        if(yAlignment > maxAlignment){
            yAlignment = maxAlignment;
        }

        updateEffectList();
        BlockPos pos = getBlockPos();

        if(getEffectList().size() > 3)
        {
            empty();
            if(!level.isClientSide)
            {
                level.playSound((Player) null, pos, SoundEvents.VEX_HURT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        updateWaterColor();
        if (!level.isClientSide) {
            level.playSound((Player) null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent((Entity) null, GameEvent.SPLASH, pos);
            sync();
        }
    }

    private void updateEffectList()
    {
        if(level == null) level = proxy.getWorld();

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);

        int[] alignment = new int[2];
        alignment[0] = getXAlignment();
        alignment[1] = getYAlignment();

        PotionMap.PotionEffectPosition potionEffectPos = PotionMap.INSTANCE.getEffectPotion(alignment);
        MobEffect mobEffect = potionEffectPos.getEffect();
        BlockPos pos = getBlockPos();

        if(mobEffect != null){
            int effectDuration = potionEffectPos.getDuration();
            int effectStrength = potionEffectPos.getStrength();
            boolean perfect = potionEffectPos.isPerfect();
            boolean useSmoke = false;
            if(effectList.stream().map(MobEffectInstance::getEffect).toList().contains(mobEffect))
            {
                effectList.removeIf((mobEffectInstance -> (mobEffectInstance.getEffect().equals(mobEffect))));
                if(perfect)
                {
                    if(!level.isClientSide())
                    {
                        level.playSound((Player) null, pos, SoundEvents.GLOW_SQUID_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
                }
            } else {
                if(!level.isClientSide())
                {
                    level.playSound((Player) null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                useSmoke = true;
            }

            addEffect(new MobEffectInstance(mobEffect, effectDuration, effectStrength));

            if(useSmoke)
            {
                ParticleUtils.generateEvaporationParticles(level, pos, getPotionColor());
            }
        }
        updateWaterColor();
    }

    public int getXAlignment() {
        return xAlignment;
    }

    public int getYAlignment(){
        return yAlignment;
    }

    public boolean canFill() {
        return waterLevel == 0;
    }

    public void takeWaterPortion() {
        this.waterLevel = this.waterLevel - 1;
        potion_color = BiomeColors.getAverageWaterColor(getLevel(), getBlockPos());
        if(!level.isClientSide) sync();
    }

    private Component customName;

    @Override
    public Component getName() {
        return customName != null ? customName : TextUtils.getTranslation("container.alchemical_cauldron");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    protected ContainerData alchemicalCauldronData;

    private ContainerData createIntArray() {
        return new ContainerData(
        )
        {
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

    public int getMaxAlignment() {
        return maxAlignment;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory player, @NotNull Player entity) {
        return new AlchemicalCauldronMenu(id, player, this, alchemicalCauldronData);
    }

    private Set<MobEffectInstance> effectList = new HashSet<>();

    public Collection<MobEffectInstance> getEffectList() {
        return effectList;
    }

    public void resetEffectList(){
        effectList.clear();
        effectList = new HashSet<>();
        xAlignment = PotionMap.INSTANCE.getMiddlePosition();
        yAlignment = PotionMap.INSTANCE.getMiddlePosition();

        int oldY = this.yAlignment;
        int oldX = this.xAlignment;
        int newX = (int) (oldX + (balanceAlignment - oldX) * 0.2);
        int newY = (int) (oldY + (balanceAlignment - oldY) * 0.2);

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);
        markUpdated();
        if(!level.isClientSide) sync();
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        effectList.add(mobEffectInstance);
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public boolean checkForEffect(MobEffect effect, int duration) {
        return effectList.stream().noneMatch((instance) -> {
            return instance.getEffect().equals(effect) && instance.getDuration() == duration;
        });
    }

    public void ensureStrength(MobEffect effect, int effectStrength) {
        boolean replace = false;
        for(MobEffectInstance instance : effectList)  {
            if(instance.getEffect().equals(effect)){
                if(instance.getAmplifier() < effectStrength){
                     replace = true;
                }
            }
        };

        if(replace) {
            effectList.removeIf((instance -> {
                return instance.getEffect().equals(effect);
            }));
            effectList.add(new MobEffectInstance(effect, 1200, effectStrength));
        }
    }

    public ItemStack getPotion() {
        ItemStack customPotion = new ItemStack(potion_type);
        if(getEffectList().isEmpty())
        {
            PotionUtils.setPotion(customPotion, Potions.WATER);
            return customPotion;
        }
        PotionUtils.setCustomEffects(customPotion, getEffectList());
        customPotion.setHoverName(new TextComponent("Alchemical Potion"));

        return customPotion;
    }

    private void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    int additionTimer = 20;
    boolean needsUpdate = false;
    public void tick() {
        BlockPos pos = getBlockPos();
        if(level.isClientSide)
        {
            if (random.nextFloat() < 0.2F && waterLevel > 0 && !getEffectList().isEmpty()) {
                double x = (double) pos.getX() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
                double y = (double) pos.getY() + .1D;
                double z = (double) pos.getZ() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);

                int color = (getEffectList().stream().findFirst().isEmpty()) ? MobEffects.WATER_BREATHING.getColor() : getEffectList().stream().findFirst().get().getEffect().getColor();

                ParticleUtils.generatePotionParticles(level, pos, color, false);
            }

            boolean heating = true;
            if(heating && random.nextFloat() < 0.2f)
            {
                double x = (double) pos.getX() + (random.nextDouble());
                double y = (double) pos.getY() + .1D;
                double z = (double) pos.getZ() + (random.nextDouble());
                ParticleOptions options = ParticleTypes.FLAME;
                level.addParticle(options, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition).inflate(1.125));
        List<ItemIngredient> contents = new ArrayList<>();

        for (ItemEntity entity : items)
        {
            ItemStack stack = entity.getItem().copy();
            if(stack.getItem() instanceof ItemIngredient ingredient)
            {
                for(int i = 0; i < stack.getCount(); i++)
                {
                    stack.setCount(1);
                    if(additionTimer < 1)
                    {
                        contents.add(ingredient);
                    }
                }

                if(additionTimer < 1)
                {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    additionTimer = 20;
                }
            }
        }
        if(!items.isEmpty())
        {
            if(additionTimer > 0) additionTimer--;

            if(!level.isClientSide) sync();
        }

        Iterator<ItemIngredient> iterator = contents.iterator();
        while (iterator.hasNext()) {
            ItemIngredient ingredient = iterator.next();
            addIngredient(ingredient);
            iterator.remove(); // Remove the current element safely
        }

        updateWaterColor();

        //Very hacky system to avoid needing to send constant update packets...

        // For some reason a single sync doesn't work when filling the cauldron with water...
        // And it's in a limbo until a second bucket of water is used on it.
        if(!level.isClientSide && needsUpdate)
        {
            sync();
            needsUpdate = false;
        }
    }

    private void updateWaterColor(){
        int red = 0;
        int green = 0;
        int blue = 0;
        int alpha = 190;
        List<MobEffectInstance> effectList = getEffectList().stream().toList();
        // Ensure we have at least one effect in the list
        if (!effectList.isEmpty()) {
            // Limit to the first three effects or fewer if the list is smaller
            int count = Math.min(effectList.size(), 3);

            // Accumulate RGB values from the first three effects
            for (int i = 0; i < count; i++) {
                MobEffectInstance effectInstance = effectList.get(i);
                int color = effectInstance.getEffect().getColor();
                red += (color >> 16) & 255;
                green += (color >> 8) & 255;
                blue += color & 255;
            }

            // Calculate average RGB values
            red /= count;
            green /= count;
            blue /= count;

            // Ensure RGB values are within valid range (0-255)
            red = Math.max(0, Math.min(255, red));
            green = Math.max(0, Math.min(255, green));
            blue = Math.max(0, Math.min(255, blue));
            potion_color = (red << 16) | (green << 8) | blue;
        }
    }

    public void empty() {
        BlockPos pos = getBlockPos();
        ParticleUtils.generateEvaporationParticles(getLevel(), pos, getPotionColor());
        resetEffectList();
        waterLevel = 0;
        setDefaultResult();
        resetEffectList();
        potion_color = BiomeColors.getAverageWaterColor(getLevel(), getBlockPos());
        needsUpdate = true;

        if (!level.isClientSide) {
            level.playSound((Player) null, pos, SoundEvents.SHROOMLIGHT_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent((Entity) null, GameEvent.SPLASH, pos);
            sync();
        }
    }
    private int potion_color = 0;

    public int getPotionColor(){
        return potion_color;
    }
}