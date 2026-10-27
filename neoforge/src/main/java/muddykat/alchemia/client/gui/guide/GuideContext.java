package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.guide.GuideSource;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface GuideContext {

    GuideCanvas canvas();

    GuideScroll scroller();

    GuideKnowledge book();

    NoteEditor editor();

    GuideSource source();

    boolean pageMode();

    @Nullable Minecraft client();

    Font getFont();

    GuideChapter chapter();

    @Nullable String openRecipeId();

    @Nullable BrewRecipe currentRecipe();

    boolean refresh();

    void send(PacketGuideAction.Action action, String argument);

    void turnPage();

    void resetScroll();

    void goTo(GuideChapter next);

    void openRecipe(BrewRecipe recipe);

    void closeRecipe();

    void openPrimer(int spread);

    void followEffect(AlchemiaEffect potion);

    void followIngredient(Ingredients ingredient);

    void followReagent(Item item);

    @Nullable Runnable followFromRecipe(ItemStack item);

    boolean returning();

    void backToRecipe();

    boolean canPrepare();

    void prepare(BrewRecipe recipe);

    void onClose();

    void setFocused(@Nullable GuiEventListener listener);
}
