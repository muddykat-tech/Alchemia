package muddykat.alchemia.compat.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.crafting.MachineCrafting;
import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class JeiGreatWork {

    private static final int CHECK_TICKS = 10;

    private static @Nullable IJeiRuntime runtime;
    private static boolean listening;
    private static final Set<AlchemyMachineRecipeCategory.Entry> added = new HashSet<>();
    private static final Set<AlchemyMachineRecipeCategory.Entry> shown = new HashSet<>();
    private static int generation = -1;
    private static @Nullable Set<String> known;
    private static @Nullable List<CraftingRecipe> machine;
    private static final Set<MachineOwner> hiddenParts = EnumSet.noneOf(MachineOwner.class);

    private JeiGreatWork() {
    }

    static void attach(IJeiRuntime available) {
        runtime = available;
        added.clear();
        shown.clear();
        generation = -1;
        known = null;
        machine = null;
        hiddenParts.clear();
        if (!listening) {
            MinecraftForge.EVENT_BUS.addListener(JeiGreatWork::onTick);
            listening = true;
        }
        refresh();
    }

    static void detach() {
        runtime = null;
        added.clear();
        shown.clear();
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = Minecraft.getInstance().player;
        if (runtime == null || player == null || player.tickCount % CHECK_TICKS != 0) return;
        refresh();
    }

    private static Set<String> discovered(@Nullable Player player) {
        Set<String> stages = new HashSet<>();
        for (ItemStack guide : RecipeDiscovery.guidesOf(player)) stages.addAll(GreatWork.stored(guide));
        return stages;
    }

    private static void refresh() {
        IJeiRuntime jei = runtime;
        if (jei == null) return;
        Set<String> now = discovered(Minecraft.getInstance().player);
        parts(jei, now);
        int current = MagnumOpus.generation();
        if (current == generation && now.equals(known)) return;
        generation = current;
        known = now;

        List<AlchemyMachineRecipeCategory.Entry> wanted = new ArrayList<>();
        List<String> hidden = new ArrayList<>();
        for (MagnumOpus.Stage stage : MagnumOpus.stages()) {
            if (now.contains(stage.result())) wanted.add(AlchemyMachineRecipeCategory.Entry.known(stage));
            else hidden.add(stage.result());
        }
        if (!hidden.isEmpty()) wanted.add(AlchemyMachineRecipeCategory.Entry.undiscovered(hidden));

        IRecipeManager manager = jei.getRecipeManager();
        List<AlchemyMachineRecipeCategory.Entry> hide = new ArrayList<>();
        for (AlchemyMachineRecipeCategory.Entry stage : shown) {
            if (!wanted.contains(stage)) hide.add(stage);
        }
        if (!hide.isEmpty()) {
            manager.hideRecipes(AlchemyMachineRecipeCategory.TYPE, hide);
            hide.forEach(shown::remove);
        }

        List<AlchemyMachineRecipeCategory.Entry> fresh = new ArrayList<>();
        List<AlchemyMachineRecipeCategory.Entry> back = new ArrayList<>();
        for (AlchemyMachineRecipeCategory.Entry stage : wanted) {
            if (!added.contains(stage)) fresh.add(stage);
            else if (!shown.contains(stage)) back.add(stage);
        }
        if (!fresh.isEmpty()) {
            manager.addRecipes(AlchemyMachineRecipeCategory.TYPE, fresh);
            added.addAll(fresh);
        }
        if (!back.isEmpty()) manager.unhideRecipes(AlchemyMachineRecipeCategory.TYPE, back);
        shown.addAll(fresh);
        shown.addAll(back);
    }

    private static void parts(IJeiRuntime jei, Set<String> stages) {
        IRecipeManager manager = jei.getRecipeManager();
        if (machine == null) {
            machine = manager.createRecipeLookup(RecipeTypes.CRAFTING).includeHidden().get()
                    .filter(holder -> partOf(holder) != null).toList();
        }
        for (MachineOwner part : MachineOwner.values()) {
            boolean visible = MachineCrafting.visible(part, stage -> stages.contains(stage.result()));
            if (visible != hiddenParts.contains(part)) continue;
            List<CraftingRecipe> recipes = machine.stream().filter(holder -> partOf(holder) == part).toList();
            if (recipes.isEmpty()) continue;
            if (visible) {
                manager.unhideRecipes(RecipeTypes.CRAFTING, recipes);
                hiddenParts.remove(part);
            } else {
                manager.hideRecipes(RecipeTypes.CRAFTING, recipes);
                hiddenParts.add(part);
            }
        }
    }

    private static @Nullable MachineOwner partOf(CraftingRecipe holder) {
        ResourceLocation id = holder.getId();
        if (!id.getNamespace().equals(Alchemia.MODID)) return null;
        for (MachineOwner owner : MachineOwner.values()) {
            if (owner.registryName().equals(id.getPath())) return owner;
        }
        return null;
    }
}
