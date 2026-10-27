package muddykat.alchemia.mixin;

import muddykat.alchemia.common.crafting.MachineCrafting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
    private static void alchemia$gateMachineParts(AbstractContainerMenu menu, ServerLevel level, Player player, CraftingContainer container,
                                                  ResultContainer result, RecipeHolder<CraftingRecipe> recipe, CallbackInfo info) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack crafted = result.getItem(0);
        if (crafted.isEmpty()) return;
        Component refusal = MachineCrafting.refusal(serverPlayer, crafted);
        if (refusal == null) return;

        result.setItem(0, ItemStack.EMPTY);
        menu.setRemoteSlot(0, ItemStack.EMPTY);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, ItemStack.EMPTY));
        serverPlayer.sendOverlayMessage(refusal.copy().withStyle(ChatFormatting.GOLD));
    }
}
