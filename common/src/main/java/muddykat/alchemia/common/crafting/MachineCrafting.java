package muddykat.alchemia.common.crafting;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.blocks.helper.MachineRepair;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.guide.GuideMastery;
import muddykat.alchemia.common.guide.GuideShelf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
#if MC_VER >= 26100
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

import java.util.List;
import java.util.function.Predicate;

public final class MachineCrafting {

    private MachineCrafting() {
    }

    public static @Nullable MachineOwner partOf(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return null;
        if (item.getBlock() instanceof BlockAlchemyMachineCore) return MachineOwner.CORE;
        if (item.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade) return MachineOwner.of(upgrade.getSide());
        return null;
    }

#if MC_VER >= 26100
    public static MagnumOpus.@Nullable Stage stageFor(MachineOwner part) {
#else
    public static @Nullable MagnumOpus.Stage stageFor(MachineOwner part) {
#endif
        List<MagnumOpus.Stage> stages = MagnumOpus.stages();
        if (stages.isEmpty()) return null;
        if (part == MachineOwner.CORE) return stages.get(0);

        int before = 0;
        for (MachineOwner earlier : MachineRepair.ORDER) {
            if (earlier == part) break;
            UpgradeSide side = MachineRepair.sideOf(earlier);
            if (side != null) before += side.potionSlots();
        }
        for (MagnumOpus.Stage stage : stages) {
            if (stage.potionCount() > before) return stage;
        }
        return null;
    }

    public static boolean visible(MachineOwner part, Predicate<MagnumOpus.Stage> knows) {
        if (!Configuration.lockMachineRecipes()) return true;
        MagnumOpus.Stage stage = stageFor(part);
        return stage == null || knows.test(stage);
    }

    public static boolean craftable(MachineOwner part, Predicate<MagnumOpus.Stage> knows, boolean restored) {
        return visible(part, knows) && (!Configuration.requireMachineRepair() || restored);
    }

    public static @Nullable Component refusal(ServerPlayer player, ItemStack result) {
        MachineOwner part = partOf(result);
        if (part == null) return null;
        List<ItemStack> guides = GuideShelf.readable(player, player.level(), player.blockPosition());
        Predicate<MagnumOpus.Stage> knows = stage -> guides.stream().anyMatch(guide -> GreatWork.knows(guide, stage));
        if (!visible(part, knows)) return Component.translatable("alchemia.machine.craft.locked");
        if (Configuration.requireMachineRepair() && !GuideMastery.hasRestored(player, part)) {
            return Component.translatable("alchemia.machine.craft.unrestored", Component.translatable("block.alchemia." + part.registryName()));
        }
        return null;
    }
}
