package muddykat.alchemia.common.items.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientCrushed;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Rarity;
import org.lwjgl.system.CallbackI;

import static muddykat.alchemia.common.items.helper.IngredientType.*;

public enum Ingredients {
    Firebell(Flower, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1, Rarity.COMMON),
    Waterbloom(Flower, IngredientAlignment.Water, MobEffects.WATER_BREATHING, 1, Rarity.COMMON),
    Windbloom(Flower, IngredientAlignment.Air, MobEffects.MOVEMENT_SPEED, 1, Rarity.COMMON),
    Featherbloom(Flower, IngredientAlignment.Air, MobEffects.LEVITATION, 1, Rarity.UNCOMMON),
    Fluffbloom(Flower, IngredientAlignment.Air, MobEffects.SLOW_FALLING, 1, Rarity.UNCOMMON),
    Spellbloom(Flower, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.ABSORPTION, 1, Rarity.COMMON),
    Boombloom(Flower, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.JUMP, 1, Rarity.COMMON),

    Bloodthorn(Herb, IngredientAlignment.Fire, IngredientAlignment.Air, MobEffects.WEAKNESS, 1, Rarity.COMMON),
    Coldleaf(Herb, IngredientAlignment.Water, MobEffects.MOVEMENT_SLOWDOWN, 1, Rarity.COMMON),
    Dragon_Pepper(Herb, IngredientAlignment.Fire, MobEffects.MOVEMENT_SPEED, 1, Rarity.COMMON),
    Druids_Rosemary(Herb, IngredientAlignment.Fire, MobEffects.HEAL, 1, Rarity.COMMON),
    Evergreen_Fern(Herb, IngredientAlignment.Air, IngredientAlignment.Fire, MobEffects.CONFUSION, 1, Rarity.COMMON),
    Lifeleaf(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEAL, 1, Rarity.COMMON),
    Goldthorn(Herb, IngredientAlignment.Earth, MobEffects.WITHER, 1, Rarity.COMMON),
    Icefruit(Herb, IngredientAlignment.Water, MobEffects.MOVEMENT_SLOWDOWN, 1, Rarity.COMMON),
    Tangleweed(Herb, IngredientAlignment.Water, MobEffects.CONFUSION, 1, Rarity.COMMON),
    Whirlweed(Herb, IngredientAlignment.Air, MobEffects.CONFUSION, 1, Rarity.COMMON),
    Goodberry(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEALTH_BOOST, 1, Rarity.COMMON),
    Thunder_Thistle(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.GLOWING, 1, Rarity.COMMON),
    Flameweed(Herb, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1, Rarity.COMMON),
    Mageberry(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.CONDUIT_POWER, 1, Rarity.COMMON),
    Healers_Heather(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.REGENERATION, 1, Rarity.COMMON),
    Terrorbud(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.BLINDNESS, 1, Rarity.COMMON),
    Thornstick(Herb, IngredientAlignment.Fire, IngredientAlignment.Earth, MobEffects.POISON, 1, Rarity.COMMON),
    Lava_Root(Root, IngredientAlignment.Fire, MobEffects.HARM, 1, Rarity.COMMON),
    Terraria(Root, IngredientAlignment.Earth, MobEffects.DIG_SPEED, 1, Rarity.COMMON),
    Dreambeet(Root, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.LUCK, 1, Rarity.COMMON),
    Marshroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.HUNGER, 1, Rarity.COMMON),
    Sulphur_Shelf(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.POISON, 1, Rarity.COMMON),
    Witch_Mushroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.WITHER, 1, Rarity.COMMON),
    Magma_Morel(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1, Rarity.COMMON),
    Rainbow_Cap(IngredientType.Mushroom, IngredientAlignment.Air, IngredientAlignment.Earth, MobEffects.UNLUCK, 1, Rarity.COMMON),

    Cloud_Crystal(IngredientType.Mineral, IngredientAlignment.Air, MobEffects.LEVITATION, 1, Rarity.COMMON),
    Earth_Pyrite(IngredientType.Mineral, IngredientAlignment.Earth, MobEffects.DAMAGE_BOOST, 1, Rarity.COMMON),
    Frost_Sapphire(IngredientType.Mineral, IngredientAlignment.Water, MobEffects.SLOW_FALLING, 1, Rarity.COMMON),
    Fire_Citrine(IngredientType.Mineral, IngredientAlignment.Fire, MobEffects.SATURATION, 1, Rarity.COMMON),
    Arcane_Crystal(IngredientType.Mineral, IngredientAlignment.Earth, IngredientAlignment.Air, MobEffects.GLOWING, 1, Rarity.RARE),
    Fable_Bismuth(IngredientType.Mineral, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.DAMAGE_RESISTANCE, 1, Rarity.EPIC);

    final IngredientType type;
    final IngredientAlignment primaryAlignment;
    final Rarity rarity;

    IngredientAlignment secondaryAlignment;

    final MobEffect ingredientEffect;
    int ingredientStrength;

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, MobEffect effect, int strength, Rarity rarity){
        this.type = type;
        this.primaryAlignment = primaryAlignment;
        this.ingredientEffect = effect;
        this.ingredientStrength = strength;
        this.rarity = rarity;
    }

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment, MobEffect effect, int strength, Rarity rarity){
        this(type, primaryAlignment, effect, strength, rarity);
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
        // We handle Minerals through Geode method (See BlockRegistry)
        if(!type.equals(Mineral)) {
            BlockRegistry.registerBlock(getSeedRegistryName(), () -> new BlockIngredient(this, type));
            ItemRegistry.registerItem(getSeedRegistryName(), () -> new ItemIngredientSeed(BlockRegistry.BLOCK_REGISTRY.get(getSeedRegistryName()).get(), this, type, primaryAlignment));
        }
        ItemRegistry.registerItem(getRegistryName(), () -> new ItemIngredient(this, type, primaryAlignment));
        ItemRegistry.registerItem(getCrushedRegistryName(), () -> new ItemIngredientCrushed(this, type, primaryAlignment));
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

    public int getPatchChance()
    {
        return Configuration.INGREDIENT_CONFIG.get(this).get();
    }

    public IngredientType getType() {
        return type;
    }
}
