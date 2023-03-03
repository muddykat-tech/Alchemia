package muddykat.alchemia.common.items.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;

import static muddykat.alchemia.common.items.helper.IngredientType.*;

public enum Ingredients {
    Firebell(Flower, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE),
    Waterbloom(Flower, IngredientAlignment.Water, MobEffects.WATER_BREATHING),
    Windbloom(Flower, IngredientAlignment.Air, MobEffects.MOVEMENT_SPEED),
    Featherbloom(Flower, IngredientAlignment.Air, MobEffects.LEVITATION),
    Fluffbloom(Flower, IngredientAlignment.Air, MobEffects.SLOW_FALLING),
    Spellbloom(Flower, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.ABSORPTION),
    Boombloom(Flower, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.JUMP),

    Bloodthorn(Herb, IngredientAlignment.Fire, IngredientAlignment.Air, MobEffects.WEAKNESS),
    Coldleaf(Herb, IngredientAlignment.Water, MobEffects.MOVEMENT_SLOWDOWN),
    Dragon_Pepper(Herb, IngredientAlignment.Fire, MobEffects.MOVEMENT_SPEED),
    Druids_Rosemary(Herb, IngredientAlignment.Fire, MobEffects.HEAL),
    Evergreen_Fern(Herb, IngredientAlignment.Air, IngredientAlignment.Fire, MobEffects.CONFUSION),
    Lifeleaf(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEAL),
    Goldthorn(Herb, IngredientAlignment.Earth, MobEffects.WITHER),
    Icefruit(Herb, IngredientAlignment.Water, MobEffects.MOVEMENT_SLOWDOWN),
    Tangleweed(Herb, IngredientAlignment.Water, MobEffects.CONFUSION),
    Goodberry(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEALTH_BOOST),
    Thunder_Thistle(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.GLOWING),
    Flameweed(Herb, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE),
    Mageberry(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.CONDUIT_POWER),
    Healers_Heather(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.REGENERATION),
    Terrorbud(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.BLINDNESS),
    Thornstick(Herb, IngredientAlignment.Fire, IngredientAlignment.Earth, MobEffects.POISON),
    Lava_Root(Root, IngredientAlignment.Fire, MobEffects.HARM),
    Terraria(Root, IngredientAlignment.Earth, MobEffects.DIG_SPEED),
    Dreambeet(Root, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.LUCK),
    Marshroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.HUNGER),
    Sulphur_Shelf(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.POISON),
    Witch_Mushroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.WITHER),
    Magma_Morel(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE),
    Rainbow_Cap(IngredientType.Mushroom, IngredientAlignment.Air, IngredientAlignment.Earth, MobEffects.UNLUCK),

    Cloud_Crystal(IngredientType.Mineral, IngredientAlignment.Air, MobEffects.LEVITATION),
    Earth_Pyrite(IngredientType.Mineral, IngredientAlignment.Earth, MobEffects.DAMAGE_BOOST),
    Frost_Sapphire(IngredientType.Mineral, IngredientAlignment.Water, MobEffects.SLOW_FALLING),
    Fire_Citrine(IngredientType.Mineral, IngredientAlignment.Fire, MobEffects.SATURATION),
    Arcane_Crystal(IngredientType.Mineral, IngredientAlignment.Earth, IngredientAlignment.Air, MobEffects.GLOWING),
    Fable_Bismuth(IngredientType.Mineral, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.DAMAGE_RESISTANCE);

    final IngredientType type;
    final IngredientAlignment primaryAlignment;

    IngredientAlignment secondaryAlignment;

    final MobEffect ingredientEffect;

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, MobEffect effect){
        this.type = type;
        this.primaryAlignment = primaryAlignment;
        this.ingredientEffect = effect;
    }

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment, MobEffect effect){
        this(type, primaryAlignment, effect);
        this.secondaryAlignment = secondaryAlignment;
    }

    public String getRegistryName() {
        return type.name().toLowerCase() +"_"+ name().toLowerCase();
    }

    public String getSeedRegistryName() {
        return type.name().toLowerCase() +"_"+ name().toLowerCase() + "_seed";
    }

    public void register(){
        BlockRegister.registerBlock(getSeedRegistryName(), () -> new BlockIngredient(this, type));
        ItemRegister.registerItem(getSeedRegistryName(), () -> new ItemIngredientSeed(BlockRegister.BLOCK_REGISTRY.get(getSeedRegistryName()).get(), this, type, primaryAlignment));
        ItemRegister.registerItem(getRegistryName(), () -> new ItemIngredient(this, type, primaryAlignment));
    }

    public FoodProperties getFoodProperties() {
        return new FoodProperties.Builder().nutrition(4).alwaysEat().saturationMod(2)
                .effect(() -> new MobEffectInstance(ingredientEffect, 40, 0), 0.75f)
                .build();
    }
}
