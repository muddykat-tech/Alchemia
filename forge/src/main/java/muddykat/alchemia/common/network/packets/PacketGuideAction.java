package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.utility.Handover;
import muddykat.alchemia.common.guide.GuideResearch;
import muddykat.alchemia.common.guide.GuideSource;
import muddykat.alchemia.common.items.ItemRecipePage;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.utility.PotionStacks;
import muddykat.alchemia.registration.registers.PotionRegistry;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketGuideAction(GuideSource source, Action action, String argument) {

    public static final int MAX_ARGUMENT = 512;
    public static final int MAX_NOTE = 256;
    public static final char NAME_SEPARATOR = '\n';

    public static String renaming(String id, String name) {
        return id + NAME_SEPARATOR + name;
    }

    private static final int FLASK_TICKS = 3600;

    public enum Action {
        SELECT,
        FORGET,
        LEARN_EFFECT,
        LEARN_INGREDIENT,
        COPY,
        RENAME,
        NOTE,
        NOTE_INGREDIENT,
        SUMMON_FLASK;

        static Action byId(int id) {
            Action[] all = values();
            return id >= 0 && id < all.length ? all[id] : SELECT;
        }
    }

    public static void encode(PacketGuideAction msg, FriendlyByteBuf buf) {
        msg.source().write(buf);
        buf.writeVarInt(msg.action().ordinal());
        buf.writeUtf(msg.argument(), MAX_ARGUMENT);
    }

    public static PacketGuideAction decode(FriendlyByteBuf buf) {
        return new PacketGuideAction(GuideSource.read(buf), Action.byId(buf.readVarInt()), buf.readUtf(MAX_ARGUMENT));
    }

    public static void handle(PacketGuideAction payload, Supplier<NetworkEvent.Context> context) {
        Player player = context.get().getSender();
        if (player == null) return;
        ItemStack guide = payload.source().guide(player);
        if (guide.isEmpty()) return;

        if (apply(player, guide, payload.action(), payload.argument())) payload.source().changed(player);
    }

    public static String flask(String stage, int index) {
        return stage + "#" + index;
    }

    private static void summonFlask(Player player, ItemStack guide, String argument) {
        if (!player.getAbilities().instabuild) return;
        int split = argument.lastIndexOf('#');
        if (split < 0) return;
        MagnumOpus.Stage stage = GreatWork.byName(argument.substring(0, split));
        if (stage == null || !GreatWork.knows(guide, stage)) return;
        int index;
        try {
            index = Integer.parseInt(argument.substring(split + 1));
        } catch (NumberFormatException error) {
            return;
        }
        if (index < 0 || index >= stage.potionCount()) return;

        List<MobEffectInstance> effects = new ArrayList<>();
        for (PotionEffectIngredient.Requirement requirement : stage.potions().get(index).required()) {
            effects.add(new MobEffectInstance(requirement.effect(), FLASK_TICKS, requirement.amplifier()));
        }
        ItemStack potion = new ItemStack(Items.POTION);
        PotionStacks.apply(potion, PotionRegistry.ALCHEMICAL.get(), effects, null);
        net.minecraft.world.entity.item.ItemEntity dropped = new net.minecraft.world.entity.item.ItemEntity(player.level(),
                player.getX(), player.getEyeY() - 0.3, player.getZ(), potion);
        dropped.setDefaultPickUpDelay();
        player.level().addFreshEntity(dropped);
    }

    public static boolean apply(Player player, ItemStack guide, Action action, String argument) {
        return switch (action) {
            case SELECT -> {
                if (!argument.isEmpty() && BrewRecipeBook.find(guide, argument) == null) yield false;
                BrewRecipeBook.select(guide, argument.isEmpty() ? null : argument);
                yield true;
            }
            case FORGET -> {
                if (BrewRecipeBook.find(guide, argument) == null) yield false;
                BrewRecipeBook.forget(guide, argument);
                yield true;
            }
            case SUMMON_FLASK -> {
                summonFlask(player, guide, argument);
                yield false;
            }
            case LEARN_EFFECT -> GuideResearch.linksEffect(guide, argument)
                    && RecipeDiscovery.learn(List.of(guide), List.of(argument)) > 0;
            case LEARN_INGREDIENT -> {
                boolean linked = IngredientDiscovery.byName(argument) != null
                        ? GuideResearch.linksIngredient(guide, argument)
                        : GuideResearch.linksReagent(guide, argument);
                yield linked && IngredientDiscovery.learnKeys(guide, List.of(argument)) > 0;
            }
            case RENAME -> {
                int split = argument.indexOf(NAME_SEPARATOR);
                if (split < 0) yield false;
                String name = argument.substring(split + 1);
                yield name.length() <= PacketNameBrew.MAX_LENGTH && BrewRecipeBook.rename(guide, argument.substring(0, split), name);
            }
            case NOTE -> {
                int split = argument.indexOf(NAME_SEPARATOR);
                if (split < 0) yield false;
                String text = argument.substring(split + 1).strip();
                yield text.length() <= MAX_NOTE && BrewRecipeBook.annotate(guide, argument.substring(0, split), text);
            }
            case NOTE_INGREDIENT -> {
                int split = argument.indexOf(NAME_SEPARATOR);
                if (split < 0) yield false;
                String key = argument.substring(0, split);
                String text = argument.substring(split + 1);
                if (text.length() > MAX_NOTE) yield false;
                Ingredients ingredient = IngredientDiscovery.byName(key);
                if (ingredient != null) {
                    yield IngredientDiscovery.knownTo(guide).contains(ingredient) && BrewRecipeBook.annotateIngredient(guide, key, text);
                }
                yield IngredientDiscovery.reagentByKey(key) != null && IngredientDiscovery.knows(guide, key)
                        && BrewRecipeBook.annotateIngredient(guide, key, text);
            }
            case COPY -> {
                copy(player, guide, argument);
                yield false;
            }
        };
    }

    private static void copy(Player player, ItemStack guide, String id) {
        BrewRecipe recipe = BrewRecipeBook.find(guide, id);
        if (recipe == null) return;

        if (!player.getAbilities().instabuild && !takePaper(player)) {
            notify(player, Component.translatable("alchemia.guide.copy_needs_paper"));
            return;
        }

        ItemStack page = ItemRecipePage.written(recipe);
        Handover.give(player, page);
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.PLAYERS, 0.8F, 1.1F);
        notify(player, Component.translatable("alchemia.guide.copied", recipe.displayName()));
    }

    private static boolean takePaper(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void notify(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.displayClientMessage(message, true);
    }
}
