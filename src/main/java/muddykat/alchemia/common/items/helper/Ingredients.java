package muddykat.alchemia.common.items.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientCrushed;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import static muddykat.alchemia.common.items.helper.IngredientType.*;

public enum Ingredients {
    Firebell(Flower, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1),
    Waterbloom(Flower, IngredientAlignment.Water, MobEffects.WATER_BREATHING, 1),
    Windbloom(Flower, IngredientAlignment.Air, MobEffects.MOVEMENT_SPEED, 1),
    Featherbloom(Flower, IngredientAlignment.Air, MobEffects.LEVITATION, 1),
    Fluffbloom(Flower, IngredientAlignment.Air, MobEffects.SLOW_FALLING, 1),
    Spellbloom(Flower, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.ABSORPTION, 1),
    Boombloom(Flower, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.JUMP, 1),

    Bloodthorn(Herb, IngredientAlignment.Fire, IngredientAlignment.Air, MobEffects.WEAKNESS, 1),
    Coldleaf(Herb, IngredientAlignment.Water, MobEffects.MOVEMENT_SLOWDOWN, 1),
    Dragon_Pepper(Herb, IngredientAlignment.Fire, MobEffects.MOVEMENT_SPEED, 1),
    Druids_Rosemary(Herb, IngredientAlignment.Fire, MobEffects.HEAL, 1),
    Evergreen_Fern(Herb, IngredientAlignment.Air, IngredientAlignment.Fire, MobEffects.CONFUSION, 1),
    Lifeleaf(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEAL, 1),
    Goldthorn(Herb, IngredientAlignment.Earth, MobEffects.WITHER, 1),
    Icefruit(Herb, IngredientAlignment.Water, MobEffects.MOVEMENT_SLOWDOWN, 1),
    Tangleweed(Herb, IngredientAlignment.Water, MobEffects.CONFUSION, 1),
    Whirlweed(Herb, IngredientAlignment.Air, MobEffects.CONFUSION, 1),
    Goodberry(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEALTH_BOOST, 1),
    Thunder_Thistle(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.GLOWING, 1),
    Flameweed(Herb, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1),
    Mageberry(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.CONDUIT_POWER, 1),
    Healers_Heather(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.REGENERATION, 1),
    Terrorbud(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.BLINDNESS, 1),
    Thornstick(Herb, IngredientAlignment.Fire, IngredientAlignment.Earth, MobEffects.POISON, 1),
    Lava_Root(Root, IngredientAlignment.Fire, MobEffects.HARM, 1),
    Terraria(Root, IngredientAlignment.Earth, MobEffects.DIG_SPEED, 1),
    Dreambeet(Root, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.LUCK, 1),
    Marshroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.HUNGER, 1),
    Sulphur_Shelf(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.POISON, 1),
    Witch_Mushroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.WITHER, 1),
    Magma_Morel(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1),
    Rainbow_Cap(IngredientType.Mushroom, IngredientAlignment.Air, IngredientAlignment.Earth, MobEffects.UNLUCK, 1),

    Cloud_Crystal(IngredientType.Mineral, IngredientAlignment.Air, MobEffects.LEVITATION, 1),
    Earth_Pyrite(IngredientType.Mineral, IngredientAlignment.Earth, MobEffects.DAMAGE_BOOST, 1),
    Frost_Sapphire(IngredientType.Mineral, IngredientAlignment.Water, MobEffects.SLOW_FALLING, 1),
    Fire_Citrine(IngredientType.Mineral, IngredientAlignment.Fire, MobEffects.SATURATION, 1),
    Arcane_Crystal(IngredientType.Mineral, IngredientAlignment.Earth, IngredientAlignment.Air, MobEffects.GLOWING, 1),
    Fable_Bismuth(IngredientType.Mineral, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.DAMAGE_RESISTANCE, 1);

    final IngredientType type;
    final IngredientAlignment primaryAlignment;

    IngredientAlignment secondaryAlignment;

    final MobEffect ingredientEffect;
    int ingredientStrength;

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, MobEffect effect, int strength){
        this.type = type;
        this.primaryAlignment = primaryAlignment;
        this.ingredientEffect = effect;
        this.ingredientStrength = strength;
    }

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment, MobEffect effect, int strength){
        this(type, primaryAlignment, effect, strength);
        this.secondaryAlignment = secondaryAlignment;
    }
    public String getRegistryName() {
        return type.name().toLowerCase() +"_"+ name().toLowerCase();
    }

    public String getCrushedRegistryName()
    {
        return type.name().toLowerCase() + "_" + name().toLowerCase() + "_crushed";
    }

    public String getSeedRegistryName() {
        return type.name().toLowerCase() +"_"+ name().toLowerCase() + "_seed";
    }

    public void register(){
        BlockRegister.registerBlock(getSeedRegistryName(), () -> new BlockIngredient(this, type));
        ItemRegister.registerItem(getSeedRegistryName(), () -> new ItemIngredientSeed(BlockRegister.BLOCK_REGISTRY.get(getSeedRegistryName()).get(), this, type, primaryAlignment));

        ItemRegister.registerItem(getRegistryName(), () -> new ItemIngredient(this, type, primaryAlignment));

        ItemRegister.registerItem(getCrushedRegistryName(), () -> new ItemIngredientCrushed(this, type, primaryAlignment));
    }

    public FoodProperties getFoodProperties() {
        return new FoodProperties.Builder().nutrition(4).alwaysEat().saturationMod(2)
                .effect(() -> new MobEffectInstance(ingredientEffect, 40, 0), 0.75f)
                .build();
    }

    public IngredientAlignment getPrimaryAlignment() {
        return primaryAlignment;
    }

    public IngredientAlignment getSecondaryAlignment() {
        return secondaryAlignment == null ? IngredientAlignment.Void : secondaryAlignment;
    }

    public int getIngredientStrength() {
        return ingredientStrength;
    }

    public int getCrushedPotency(){
        return 4;
    }
}
