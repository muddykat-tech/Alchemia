package muddykat.alchemia.client.effect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class SenseTargets {

    public static final int NO_COLOUR = -1;

    private record BlockRule(Block block, TagKey<Block> tag, int colour) {

        boolean matches(BlockState state) {
            return block != null ? state.is(block) : state.is(tag);
        }
    }

    private final Supplier<List<? extends String>> blockSource;
    private final Supplier<List<? extends String>> entitySource;
    private List<String> blockEntries = List.of();
    private List<String> entityEntries = List.of();
    private List<BlockRule> blockRules = List.of();
    private Set<EntityType<?>> entities = Set.of();
    private List<TagKey<EntityType<?>>> entityTags = List.of();
    private boolean hostile;
    private boolean aggressive;

    public SenseTargets(Supplier<List<? extends String>> blockSource, Supplier<List<? extends String>> entitySource) {
        this.blockSource = blockSource;
        this.entitySource = entitySource;
    }

    public void refresh() {
        List<String> nextBlocks = List.copyOf(blockSource.get());
        if (!nextBlocks.equals(blockEntries)) {
            blockEntries = nextBlocks;
            List<BlockRule> rules = new ArrayList<>();
            for (String entry : nextBlocks) {
                int split = entry.indexOf('=');
                String text = (split < 0 ? entry : entry.substring(0, split)).trim();
                int colour = split < 0 ? NO_COLOUR : colour(entry.substring(split + 1));
                if (text.startsWith("#")) {
                    ResourceLocation id = ResourceLocation.tryParse(text.substring(1));
                    if (id != null) rules.add(new BlockRule(null, TagKey.create(Registries.BLOCK, id), colour));
                } else {
                    ResourceLocation id = ResourceLocation.tryParse(text);
                    if (id != null) BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> rules.add(new BlockRule(block, null, colour)));
                }
            }
            blockRules = List.copyOf(rules);
        }

        List<String> nextEntities = List.copyOf(entitySource.get());
        if (!nextEntities.equals(entityEntries)) {
            entityEntries = nextEntities;
            Set<EntityType<?>> ids = new HashSet<>();
            List<TagKey<EntityType<?>>> tags = new ArrayList<>();
            boolean anyHostile = false;
            boolean anyAggressive = false;
            for (String entry : nextEntities) {
                int split = entry.indexOf('=');
                String text = (split < 0 ? entry : entry.substring(0, split)).trim();
                if (text.equalsIgnoreCase("hostile")) {
                    anyHostile = true;
                } else if (text.equalsIgnoreCase("aggressive")) {
                    anyAggressive = true;
                } else if (text.startsWith("#")) {
                    ResourceLocation id = ResourceLocation.tryParse(text.substring(1));
                    if (id != null) tags.add(TagKey.create(Registries.ENTITY_TYPE, id));
                } else {
                    ResourceLocation id = ResourceLocation.tryParse(text);
                    if (id != null) BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(ids::add);
                }
            }
            entities = Set.copyOf(ids);
            entityTags = List.copyOf(tags);
            hostile = anyHostile;
            aggressive = anyAggressive;
        }
    }

    public boolean matches(BlockState state) {
        for (BlockRule rule : blockRules) {
            if (rule.matches(state)) return true;
        }
        return false;
    }

    public int colour(BlockState state) {
        for (BlockRule rule : blockRules) {
            if (rule.matches(state) && rule.colour() != NO_COLOUR) return rule.colour();
        }
        return NO_COLOUR;
    }

    private static int colour(String text) {
        String hex = text.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
        try {
            return hex.length() == 6 ? Integer.parseInt(hex, 16) : NO_COLOUR;
        } catch (NumberFormatException exception) {
            return NO_COLOUR;
        }
    }

    public boolean matches(Entity entity) {
        if (hostile && entity instanceof Enemy) return true;
        if (aggressive && entity instanceof Mob mob && mob.isAggressive()) return true;
        if (entities.contains(entity.getType())) return true;
        for (TagKey<EntityType<?>> tag : entityTags) {
            if (entity.getType().is(tag)) return true;
        }
        return false;
    }

    public boolean anyEntities() {
        return hostile || aggressive || !entities.isEmpty() || !entityTags.isEmpty();
    }
}
