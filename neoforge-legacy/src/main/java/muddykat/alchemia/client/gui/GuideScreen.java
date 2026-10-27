package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.client.gui.guide.BookFrame;
import muddykat.alchemia.client.gui.guide.ContentsPage;
import muddykat.alchemia.client.gui.guide.EffectPages;
import muddykat.alchemia.client.gui.guide.GrindSliders;
import muddykat.alchemia.client.gui.guide.GuideCanvas;
import muddykat.alchemia.client.gui.guide.GuideChapter;
import muddykat.alchemia.client.gui.guide.GuideContext;
import muddykat.alchemia.client.gui.guide.GuideItems;
import muddykat.alchemia.client.gui.guide.GuideKnowledge;
import muddykat.alchemia.client.gui.guide.GuideScroll;
import muddykat.alchemia.client.gui.guide.IngredientPages;
import muddykat.alchemia.client.gui.guide.NoteEditor;
import muddykat.alchemia.client.gui.guide.OpusPages;
import muddykat.alchemia.client.gui.guide.PrimerLayout;
import muddykat.alchemia.client.gui.guide.PrimerPages;
import muddykat.alchemia.client.gui.guide.RecipeIndex;
import muddykat.alchemia.client.gui.guide.RecipePage;
import muddykat.alchemia.common.blocks.tileentity.TileEntityResearchDesk;
import muddykat.alchemia.common.guide.GuideSource;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.registration.registers.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public class GuideScreen extends Screen implements GuideContext {

    private record Place(@Nullable String openRecipe, double scroll, double ingredientScroll) {
    }

    private static GuideChapter lastChapter = GuideChapter.CONTENTS;

    private final GuideSource source;
    private final boolean pageMode;
    private final Map<GuideChapter, Place> places = new EnumMap<>(GuideChapter.class);
    private GuideChapter chapter;
    private @Nullable String openRecipe;
    private @Nullable String returnRecipe;
    private double returnScroll;

    private final GuideScroll scroller = new GuideScroll();
    private final GuideCanvas canvas = new GuideCanvas(scroller);
    private final GuideKnowledge book = new GuideKnowledge();
    private final NoteEditor editor = new NoteEditor(this);
    private final PrimerLayout primerLayout = new PrimerLayout(this);
    private final GrindSliders grinds = new GrindSliders(this);
    private final PrimerPages primer = new PrimerPages(this, primerLayout, grinds);
    private final RecipeIndex index = new RecipeIndex(this);
    private final RecipePage recipePage = new RecipePage(this, index);
    private final ContentsPage contents = new ContentsPage(this, index);
    private final EffectPages effects = new EffectPages(this);
    private final IngredientPages ingredients = new IngredientPages(this, grinds);
    private final OpusPages opus = new OpusPages(this);
    private final BookFrame frame = new BookFrame(this, primer, index, effects, ingredients, opus);

    private GuideScreen(GuideSource source, boolean pageMode) {
        super(Component.translatable("alchemia.guide.title"));
        this.source = source;
        this.pageMode = pageMode;
        this.chapter = pageMode ? GuideChapter.RECIPES : lastChapter.shown() ? lastChapter : GuideChapter.CONTENTS;
    }

    public static GuideScreen guide(GuideSource source) {
        return new GuideScreen(source, false);
    }

    public static GuideScreen page(GuideSource source) {
        return new GuideScreen(source, true);
    }

    @Override
    protected void init() {
        int totalW = PAGE_RIGHT_EDGE + 1 + TAB_W;
        int totalH = BOOK_H + RIBBON_SHOWN + SEAL_RISE;
        float scale = Math.min(1.0F, Math.min((this.width - 8) / (float) totalW, (this.height - 8) / (float) totalH));
        int left = Math.round((this.width - totalW * scale) / 2.0F);
        int top = Math.round((this.height - totalH * scale) / 2.0F + SEAL_RISE * scale);
        canvas.place(left, top, scale);

        EditBox nameField = new EditBox(this.font, 0, 0, PAGE_W - 8, LINE, Component.translatable("alchemia.guide.rename")) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
            }
        };
        nameField.setBordered(false);
        nameField.setMaxLength(PacketNameBrew.MAX_LENGTH);
        editor.attach(nameField);
        addWidget(nameField);
        primerLayout.invalidate();
        refresh();
    }

    @Override
    public void onClose() {
        editor.commit();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public GuideCanvas canvas() {
        return canvas;
    }

    @Override
    public GuideScroll scroller() {
        return scroller;
    }

    @Override
    public GuideKnowledge book() {
        return book;
    }

    @Override
    public NoteEditor editor() {
        return editor;
    }

    @Override
    public GuideSource source() {
        return source;
    }

    @Override
    public boolean pageMode() {
        return pageMode;
    }

    @Override
    public @Nullable Minecraft client() {
        return this.minecraft;
    }

    @Override
    public GuideChapter chapter() {
        return chapter;
    }

    @Override
    public @Nullable String openRecipeId() {
        return openRecipe;
    }

    @Override
    public boolean refresh() {
        ItemStack held = this.minecraft == null ? ItemStack.EMPTY
                : pageMode ? source.page(this.minecraft.player) : source.guide(this.minecraft.player);
        if (pageMode) {
            boolean found = book.readPage(held, this.minecraft == null ? null : this.minecraft.player);
            if (held.isEmpty()) return false;
            openRecipe = found ? book.recipes().get(0).id() : null;
            return found;
        }

        if (!book.readGuide(held)) return false;
        effects.refresh();
        if (openRecipe != null && book.find(openRecipe) == null) openRecipe = null;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        recipePage.tick();
        if (!refresh()) onClose();
    }

    @Override
    public void send(PacketGuideAction.Action action, String argument) {
        PacketDistributor.sendToServer(new PacketGuideAction(source, action, argument));
    }

    @Override
    public void turnPage() {
        if (this.minecraft == null) return;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

    @Override
    public void goTo(GuideChapter next) {
        if (!next.shown()) return;
        editor.commit();
        returnRecipe = null;
        if (next == chapter) {
            if (openRecipe != null || primer.spread() >= 0) turnPage();
            openRecipe = null;
            primer.close();
            resetScroll();
            return;
        }
        remember();
        turnPage();
        chapter = next;
        if (!pageMode) lastChapter = next;
        Place saved = places.get(next);
        openRecipe = saved == null ? null : saved.openRecipe();
        resetScroll();
        if (saved != null) {
            scroller.set(saved.scroll());
            recipePage.setIngredientScroll(saved.ingredientScroll());
        }
    }

    private void remember() {
        places.put(chapter, new Place(chapter == GuideChapter.RECIPES ? openRecipe : null, scroller.offset(), recipePage.ingredientScroll()));
    }

    @Override
    public void openRecipe(BrewRecipe recipe) {
        editor.commit();
        if (chapter != GuideChapter.RECIPES) remember();
        chapter = GuideChapter.RECIPES;
        openRecipe = recipe.id();
        recipePage.disarm();
        resetScroll();
        turnPage();
    }

    @Override
    public void closeRecipe() {
        openRecipe = null;
    }

    @Override
    public void openPrimer(int spread) {
        primer.open(spread);
    }

    @Override
    public void resetScroll() {
        scroller.reset();
        recipePage.setIngredientScroll(0);
    }

    @Override
    public @Nullable BrewRecipe currentRecipe() {
        if (openRecipe == null) return null;
        for (BrewRecipe recipe : book.recipes()) {
            if (recipe.id().equals(openRecipe)) return recipe;
        }
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        if (book.stack().isEmpty()) return;

        canvas.toBook(mouseX, mouseY);
        canvas.begin();
        recipePage.beginFrame();

        graphics.pose().pushPose();
        graphics.pose().translate(canvas.left(), canvas.top(), 0.0F);
        graphics.pose().scale(canvas.scale(), canvas.scale(), 1.0F);

        frame.draw(graphics);
        BrewRecipe recipe = chapter == GuideChapter.RECIPES ? currentRecipe() : null;
        if (recipe != null) {
            recipePage.draw(graphics, recipe);
        } else {
            switch (chapter) {
                case CONTENTS -> {
                    if (primer.spread() >= 0) {
                        primer.draw(graphics);
                    } else {
                        contents.draw(graphics);
                    }
                }
                case RECIPES -> index.draw(graphics);
                case EFFECTS -> effects.draw(graphics);
                case INGREDIENTS -> ingredients.draw(graphics);
                case OPUS -> opus.draw(graphics);
            }
        }
        canvas.end();

        graphics.pose().popPose();

        List<Component> lines = canvas.tooltip();
        if (!lines.isEmpty()) GuiDraw.tooltip(graphics, this.font, lines, mouseX, mouseY);
    }

    @Override
    public void followEffect(AlchemiaEffect potion) {
        if (!book.discovered().contains(potion)) send(PacketGuideAction.Action.LEARN_EFFECT, potion.id());
        effects.select(potion);
        goTo(GuideChapter.EFFECTS);
    }

    @Override
    public void followIngredient(Ingredients ingredient) {
        if (!book.knownIngredients().contains(ingredient)) send(PacketGuideAction.Action.LEARN_INGREDIENT, ingredient.name());
        ingredients.select(ingredient);
        goTo(GuideChapter.INGREDIENTS);
        if (ingredient.getType() == IngredientType.Mineral) {
            ingredients.showReagents(0);
        } else {
            ingredients.showIngredients();
        }
    }

    @Override
    public void followReagent(Item item) {
        if (!book.knownReagents().contains(item)) send(PacketGuideAction.Action.LEARN_INGREDIENT, GuideItems.reagentKey(item));
        ingredients.select(item);
        goTo(GuideChapter.INGREDIENTS);
        ingredients.showReagents(VanillaIngredients.MODIFIERS.contains(item) || Imbuements.isAttuner(new ItemStack(item)) ? 1 : 0);
    }

    @Override
    public @Nullable Runnable followFromRecipe(ItemStack item) {
        Runnable follow = followItem(item);
        String from = openRecipe;
        if (follow == null || from == null) return follow;
        double at = scroller.offset();
        return () -> {
            follow.run();
            returnRecipe = from;
            returnScroll = at;
        };
    }

    @Override
    public boolean returning() {
        return returnRecipe != null;
    }

    @Override
    public void backToRecipe() {
        String target = returnRecipe;
        returnRecipe = null;
        for (BrewRecipe recipe : book.recipes()) {
            if (!recipe.id().equals(target)) continue;
            openRecipe(recipe);
            if (!pageMode) lastChapter = GuideChapter.RECIPES;
            scroller.set(returnScroll);
            return;
        }
        goTo(GuideChapter.RECIPES);
    }

    private @Nullable Runnable followItem(ItemStack item) {
        if (item.getItem() instanceof ItemIngredient ingredient) return () -> followIngredient(ingredient.getIngredient());
        if (GuideItems.isReagent(item.getItem())) return () -> followReagent(item.getItem());
        return null;
    }

    private String deskHint() {
        if (this.minecraft != null && this.minecraft.level != null
                && this.minecraft.level.getBlockEntity(source.pos()) instanceof TileEntityResearchDesk) {
            return "alchemia.guide.desk_hint";
        }
        return "alchemia.guide.tome_hint";
    }

    @Override
    public boolean canPrepare() {
        return !pageMode && (!source.block() || "alchemia.guide.desk_hint".equals(deskHint()));
    }

    @Override
    public void prepare(BrewRecipe recipe) {
        String next = recipe.id().equals(book.selected()) ? "" : recipe.id();
        BrewRecipeBook.select(book.stack(), next.isEmpty() ? null : next);
        book.select(next.isEmpty() ? null : next);
        send(PacketGuideAction.Action.SELECT, next);
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(next.isEmpty()
                    ? SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F)
                    : SimpleSoundInstance.forUI(SoundRegistry.GUIDE_STAMP.get(), 0.95F + (float) Math.random() * 0.1F, 0.9F));
        }
    }

    private boolean scrollable() {
        return scroller.overflows() && (openRecipe != null || chapter == GuideChapter.EFFECTS || chapter == GuideChapter.INGREDIENTS
                || chapter == GuideChapter.OPUS);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        canvas.toBook(mouseX, mouseY);
        if (editor.open()) {
            if (editor.covers(canvas.bookX(), canvas.bookY())) return true;
            editor.commit();
        }
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (scrollable() && scroller.startDrag(canvas.bookX(), canvas.bookY())) return true;
            if (canvas.click()) return true;
        }
        if (button == InputConstants.MOUSE_BUTTON_RIGHT && canvas.rightClick()) return true;
        if (button == InputConstants.MOUSE_BUTTON_RIGHT && openRecipe != null && !pageMode) {
            openRecipe = null;
            resetScroll();
            turnPage();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (grinds.dragging()) {
            canvas.toBook(mouseX, mouseY);
            grinds.drag();
            return true;
        }
        if (!scroller.dragging()) return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        canvas.toBook(mouseX, mouseY);
        scroller.drag(canvas.bookY());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scroller.release();
        grinds.release();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        canvas.toBook(mouseX, mouseY);
        if (recipePage.scrollIngredients(scrollY)) return true;
        boolean overRight = canvas.bookX() >= RIGHT_X - 4 && canvas.bookX() < RIGHT_X + PAGE_W + 6;
        if (scrollable() && overRight) {
            scroller.wheel(scrollY);
            return true;
        }
        if (chapter == GuideChapter.RECIPES && openRecipe == null) {
            index.flip(scrollY > 0 ? -1 : 1);
            return true;
        }
        if (chapter == GuideChapter.CONTENTS && primer.spread() >= 0) {
            primer.flip(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public net.minecraft.client.gui.Font getFont() {
        return this.font;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int key = keyCode;
        if (editor.active()) {
            if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
                editor.commit();
                return true;
            }
            if ((keyCode == InputConstants.KEY_ESCAPE)) {
                editor.stop();
                return true;
            }
            editor.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (key == InputConstants.KEY_LEFT || key == InputConstants.KEY_RIGHT) {
            int delta = key == InputConstants.KEY_LEFT ? -1 : 1;
            if (chapter == GuideChapter.CONTENTS && primer.spread() >= 0) {
                primer.flip(delta);
            } else if (openRecipe != null && !pageMode) {
                index.flipRecipe(delta);
            } else if (chapter == GuideChapter.RECIPES) {
                index.flip(delta);
            } else if (!pageMode) {
                GuideChapter[] all = GuideChapter.values();
                GuideChapter target = chapter;
                do {
                    target = all[Math.floorMod(target.ordinal() + delta, all.length)];
                } while (!target.shown());
                goTo(target);
            }
            return true;
        }
        if (key == InputConstants.KEY_BACKSPACE && openRecipe != null && !pageMode) {
            openRecipe = null;
            resetScroll();
            turnPage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
