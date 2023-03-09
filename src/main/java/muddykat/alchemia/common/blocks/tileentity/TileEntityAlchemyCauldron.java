package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.client.render.AlchemicalCauldronRenderer;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.AlchemiaRegistry;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TileEntityAlchemyCauldron extends BlockEntity implements MenuProvider, Nameable {

    private int waterLevel;
    private final int maxWaterLevel = 4;
    private final ItemStackHandler inventory;

    private int xAlignment;
    private int yAlignment;

    private final int maxAlignment;
    private final int balanceAlignment;

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

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Inventory")) {
            inventory.deserializeNBT(compound.getCompound("Inventory"));
        } else {
            inventory.deserializeNBT(compound);
        }
        waterLevel = compound.getInt("waterLevel");

        xAlignment = compound.getInt("xAlignment");
        yAlignment = compound.getInt("yAlignment");

    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        writeItems(compound);
        compound.putInt("waterLevel", waterLevel);
        compound.putInt("xAlignment", xAlignment);
        compound.putInt("yAlignment", yAlignment);
    }

    private CompoundTag writeItems(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Inventory", inventory.serializeNBT());
        return compound;
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
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
        if(waterLevel < 1){
            resetEffectList();
        }

        this.waterLevel = this.maxWaterLevel;
        int oldY = this.yAlignment;
        int oldX = this.xAlignment;
        int newX = (int) (oldX + (balanceAlignment - oldX) * 0.2);
        int newY = (int) (oldY + (balanceAlignment - oldY) * 0.2);


        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);


    }



    public void addIngredient(Ingredients ingredient) {
        this.xAlignment += ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX();
        this.yAlignment += ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY();

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

        alchemicalCauldronData.set(1, xAlignment);
        alchemicalCauldronData.set(2, yAlignment);
    }


    public int getXAlignment() {
        return xAlignment;
    }

    public int getYAlignment(){
        return yAlignment;
    }

    public boolean canFill() {
        return waterLevel < maxWaterLevel;
    }

    public boolean filledPotion() {
        this.waterLevel = this.waterLevel - 1;
        return waterLevel > -1;
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

    protected final ContainerData alchemicalCauldronData;

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
    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        return new AlchemicalCauldronMenu(id, player, this, alchemicalCauldronData);
    }

    private Set<MobEffectInstance> effectList = new HashSet<>();

    public Collection<MobEffectInstance> getEffectList() {
        return effectList;
    }

    public void resetEffectList(){
        effectList.clear();
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
}