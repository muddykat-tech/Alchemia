package muddykat.alchemia.common.items.helper;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import static muddykat.alchemia.common.items.helper.IngredientType.*;

public enum Ingredients {
    Firebell(Flower, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1, Rarity.COMMON),
    Waterbloom(Flower, IngredientAlignment.Water, MobEffects.WATER_BREATHING, 1, Rarity.COMMON),
    Windbloom(Flower, IngredientAlignment.Air, MobEffects.SPEED, 1, Rarity.COMMON),
    Featherbloom(Flower, IngredientAlignment.Air, MobEffects.LEVITATION, 1, Rarity.UNCOMMON),
    Fluffbloom(Flower, IngredientAlignment.Air, MobEffects.SLOW_FALLING, 1, Rarity.UNCOMMON),
    Spellbloom(Flower, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.ABSORPTION, 1, Rarity.COMMON),
    Boombloom(Flower, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.JUMP_BOOST, 1, Rarity.COMMON),

    Bloodthorn(Herb, IngredientAlignment.Fire, IngredientAlignment.Air, MobEffects.WEAKNESS, 1, Rarity.COMMON),
    Coldleaf(Herb, IngredientAlignment.Water, MobEffects.SLOWNESS, 1, Rarity.COMMON),
    Dragon_Pepper(Herb, IngredientAlignment.Fire, MobEffects.SPEED, 1, Rarity.COMMON),
    Druids_Rosemary(Herb, IngredientAlignment.Fire, MobEffects.INSTANT_HEALTH, 1, Rarity.COMMON),
    Evergreen_Fern(Herb, IngredientAlignment.Air, IngredientAlignment.Fire, MobEffects.NAUSEA, 1, Rarity.COMMON),
    Lifeleaf(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.INSTANT_HEALTH, 1, Rarity.COMMON),
    Goldthorn(Herb, IngredientAlignment.Earth, MobEffects.WITHER, 1, Rarity.COMMON),
    Icefruit(Herb, IngredientAlignment.Water, MobEffects.SLOWNESS, 1, Rarity.COMMON),
    Tangleweed(Herb, IngredientAlignment.Water, MobEffects.NAUSEA, 1, Rarity.COMMON),
    Whirlweed(Herb, IngredientAlignment.Air, MobEffects.NAUSEA, 1, Rarity.COMMON),
    Goodberry(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.HEALTH_BOOST, 1, Rarity.COMMON),
    Thunder_Thistle(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.GLOWING, 1, Rarity.COMMON),
    Flameweed(Herb, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1, Rarity.COMMON),
    Mageberry(Herb, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.CONDUIT_POWER, 1, Rarity.COMMON),
    Healers_Heather(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.REGENERATION, 1, Rarity.COMMON),
    Terrorbud(Herb, IngredientAlignment.Earth, IngredientAlignment.Water, MobEffects.BLINDNESS, 1, Rarity.COMMON),
    Thornstick(Herb, IngredientAlignment.Fire, IngredientAlignment.Earth, MobEffects.POISON, 1, Rarity.COMMON),
    Lava_Root(Root, IngredientAlignment.Fire, MobEffects.INSTANT_DAMAGE, 1, Rarity.COMMON),
    Terraria(Root, IngredientAlignment.Earth, MobEffects.HASTE, 1, Rarity.COMMON),
    Dreambeet(Root, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.LUCK, 1, Rarity.COMMON),
    Marshroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.HUNGER, 1, Rarity.COMMON),
    Sulphur_Shelf(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.POISON, 1, Rarity.COMMON),
    Witch_Mushroom(IngredientType.Mushroom, IngredientAlignment.Earth, IngredientAlignment.Fire, MobEffects.WITHER, 1, Rarity.COMMON),
    Magma_Morel(IngredientType.Mushroom, IngredientAlignment.Fire, MobEffects.FIRE_RESISTANCE, 1, Rarity.COMMON),
    Rainbow_Cap(IngredientType.Mushroom, IngredientAlignment.Air, IngredientAlignment.Earth, MobEffects.UNLUCK, 1, Rarity.COMMON),

    Cloud_Crystal(IngredientType.Mineral, IngredientAlignment.Air, MobEffects.LEVITATION, 1, Rarity.COMMON),
    Earth_Pyrite(IngredientType.Mineral, IngredientAlignment.Earth, MobEffects.STRENGTH, 1, Rarity.COMMON),
    Frost_Sapphire(IngredientType.Mineral, IngredientAlignment.Water, MobEffects.SLOW_FALLING, 1, Rarity.COMMON),
    Fire_Citrine(IngredientType.Mineral, IngredientAlignment.Fire, MobEffects.SATURATION, 1, Rarity.COMMON),
    Arcane_Crystal(IngredientType.Mineral, IngredientAlignment.Air, IngredientAlignment.Water, MobEffects.GLOWING, 1, Rarity.RARE),
    Fable_Bismuth(IngredientType.Mineral, IngredientAlignment.Water, IngredientAlignment.Air, MobEffects.RESISTANCE, 1, Rarity.EPIC);

    public static final int MAX_CRUSH = 3;
    public static final int CRUSH_UNITS_PER_LEVEL = 4;
    public static final int MAX_CRUSH_UNITS = MAX_CRUSH * CRUSH_UNITS_PER_LEVEL;

    final IngredientType type;
    final IngredientAlignment primaryAlignment;
    final Rarity rarity;

    IngredientAlignment secondaryAlignment;

    final Holder<MobEffect> ingredientEffect;
    final int ingredientStrength;

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, Holder<MobEffect> effect, int strength, Rarity rarity) {
        this.type = type;
        this.primaryAlignment = primaryAlignment;
        this.ingredientEffect = effect;
        this.ingredientStrength = strength;
        this.rarity = rarity;
    }

    Ingredients(IngredientType type, IngredientAlignment primaryAlignment, IngredientAlignment secondaryAlignment, Holder<MobEffect> effect, int strength, Rarity rarity) {
        this(type, primaryAlignment, effect, strength, rarity);
        this.secondaryAlignment = secondaryAlignment;
    }

    public String getRegistryName() {
        return type.name().toLowerCase() + "_" + name().toLowerCase();
    }

    public String getSeedRegistryName() {
        return type.name().toLowerCase() + "_" + name().toLowerCase() + "_seed";
    }

    public void register() {
        if (!type.equals(Mineral)) {
            var block = BlockRegistry.registerBlock(getSeedRegistryName(), properties -> new BlockIngredient(this, type, properties),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollision()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.CROP)
                            .pushReaction(PushReaction.DESTROY));
            ItemRegistry.registerItem(getSeedRegistryName(), properties -> new ItemIngredientSeed(block.get(), this, type, primaryAlignment, properties));
        }
        ItemRegistry.registerItem(getRegistryName(), properties -> new ItemIngredient(this, type, primaryAlignment, properties));
    }

    public Item.Properties applyFoodProperties(Item.Properties properties) {
        return properties.food(
                new FoodProperties.Builder().nutrition(4).alwaysEdible().saturationModifier(2f).build(),
                Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ingredientEffect, 40, 0), 0.75f))
                        .build());
    }

    public IngredientAlignment getPrimaryAlignment() {
        return primaryAlignment;
    }

    public IngredientAlignment getSecondaryAlignment() {
        return secondaryAlignment == null ? IngredientAlignment.Void : secondaryAlignment;
    }

    public Holder<MobEffect> getIngredientEffect() {
        return ingredientEffect;
    }

    public int getIngredientStrength() {
        return ingredientStrength;
    }

    public double getPotency(double crushLevel) {
        return ingredientStrength * (1 + Math.max(0.0, Math.min(MAX_CRUSH, crushLevel)));
    }

    public double getPotencyForUnits(int units) {
        return getPotency((double) units / CRUSH_UNITS_PER_LEVEL);
    }

    public int getPatchChance() {
        var chance = Configuration.INGREDIENT_CONFIG.get(this);
        return Configuration.COMMON_CONFIG.isLoaded() ? chance.get() : chance.getDefault();
    }

    public IngredientType getType() {
        return type;
    }

    public Rarity getRarity() {
        return rarity;
    }
}
