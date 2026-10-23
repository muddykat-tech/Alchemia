package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.common.potion.Imbuements;
import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.crafting.MachineBlueprints;
import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.guide.GuideNews;
import muddykat.alchemia.common.guide.GuideSource;
import muddykat.alchemia.common.items.ItemAlbedo;
import muddykat.alchemia.common.items.ItemCitrinitas;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemNigredo;
import muddykat.alchemia.common.items.ItemRecipePage;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.registration.registers.ItemRegistry;
import muddykat.alchemia.registration.registers.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import muddykat.alchemia.common.blocks.tileentity.TileEntityResearchDesk;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.systems.RenderSystem;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.utility.PotionStacks;
import muddykat.alchemia.registration.registers.PotionRegistry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.effect.MobEffect;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GuideScreen extends Screen {

    private static final ResourceLocation BOOK = new ResourceLocation(Alchemia.MODID, "textures/gui/guide/book.png");
    private static final ResourceLocation WIDGETS = new ResourceLocation(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int BOOK_SHEET_W = 512;
    private static final int BOOK_SHEET_H = 256;
    private static final int WIDGET_SHEET = 256;

    private static final int BOOK_W = 376;
    private static final int BOOK_H = 232;
    private static final int RIBBON_U = 380;
    private static final int RIBBON_W = 9;
    private static final int RIBBON_BODY_H = 24;
    private static final int RIBBON_TAIL_V = 26;
    private static final int RIBBON_TAIL_H = 5;
    private static final int RIBBON_SHOWN = 15;
    private static final int RIBBON_X = (BOOK_W - 1) / 2 - RIBBON_W / 2;
    private static final int PAGE_LEFT_EDGE = 12;
    private static final int PAGE_RIGHT_EDGE = 363;
    private static final int PAGE_TOP_EDGE = 7;
    private static final int PAGE_BOTTOM_EDGE = 218;
    private static final int PAGE_STACK = 3;
    private static final int RIBBON_OFFSET = 20;

    private static final int LEFT_X = 22;
    private static final int RIGHT_X = 199;
    private static final int PAGE_W = 155;
    private static final int TOP = 17;
    private static final int BOTTOM = 211;

    private static final int TAB_W = 48;
    private static final int TAB_STRAIGHT_U = 204;
    private static final int TAB_FOLDED_U = 154;
    private static final int TAB_ROW_V = 92;
    private static final int TAB_ROW_STEP = 26;
    private static final int TAB_DROP = 3;
    private static final int TAB_PATCH_X = 23;
    private static final int TAB_PATCH_Y = 2;
    private static final int TAB_REACH = 4;
    private static final int TAB_H = 22;
    private static final int TAB_TOP = 20;
    private static final int TAB_SPACING = 26;

    private static final int ICON = 12;
    private static final int ICON_STEP = 13;
    private static final int BUTTON_U = 40;
    private static final int BUTTON_V = 28;
    private static final int BUTTON_W = 48;
    private static final int BUTTON_H = 16;
    private static final int BUTTON_CAP = 4;
    private static final int SMALL_U = 92;
    private static final int SMALL_V = 28;
    private static final int SMALL = 16;
    private static final int TRACK_U = 146;
    private static final int TRACK_V = 28;
    private static final int TRACK_W = 7;
    private static final int TRACK_H = 24;
    private static final int THUMB_U = 155;
    private static final int THUMB_H = 15;
    private static final int SCROLL_CAP = 3;
    private static final int THUMB_CAP = 4;
    private static final int THUMB_FILL_V = 4;
    private static final int GRIP_V = 6;
    private static final int GRIP_H = 5;
    private static final int CELL = 18;
    private static final int CELL_U = 176;
    private static final int CELL_V = 28;
    private static final int CORNER_U = 176;
    private static final int CORNER_V = 48;
    private static final int MEDAL_U = 40;
    private static final int MEDAL_V = 80;
    private static final int MEDAL = 48;
    private static final int DIVIDER_U = 90;
    private static final int DIVIDER_V = 80;
    private static final int DIVIDER_W = 150;
    private static final int DIVIDER_H = 7;
    private static final int BULLET_U = 90;
    private static final int BULLET_V = 90;
    private static final int ROSETTE_U = 0;
    private static final int ROSETTE_V = 140;
    private static final int ROSETTE = 12;
    private static final int VINE_U = 14;
    private static final int VINE_RIGHT_U = 50;
    private static final int VINE_V = 140;
    private static final int VINE_W = 34;
    private static final int VINE_H = 9;
    private static final int DIAMOND_U = 86;
    private static final int DIAMOND_V = 140;
    private static final int DIAMOND = 5;
    private static final int JOINT_U = 92;
    private static final int JOINT_V = 140;
    private static final int JOINT = 7;
    private static final int RING_U = 132;
    private static final int RING_V = 140;
    private static final int RING = 22;
    private static final int INGREDIENT_ROW = 20;
    private static final int INGREDIENT_NOTE_LINES = 4;
    private static final int INGREDIENT_ROWS_SHOWN = 3;
    private static final int DISC_U = 0;
    private static final int DISC_V = 160;
    private static final int DISC = 52;
    private static final int SEAL_U = 54;
    private static final int SEAL_V = 160;
    private static final int SEAL = 18;
    private static final int SEAL_RISE = 5;
    private static final int TITLE_ART_U = 54;
    private static final int TITLE_ART_V = 214;
    private static final int TITLE_ART_W = 66;
    private static final int TITLE_ART_H = 12;
    private static final int PREPARED_U = 54;
    private static final int PREPARED_V = 180;
    private static final int EMPTY_SEAL_U = 73;
    private static final int MARKER_COLUMN = SEAL + 3;
    private static final int TITLE_W = 98;
    private static final int TITLE_LINE = 15;
    private static final int TITLE_CAP = 11;
    private static final int TITLE_RULE = 14;
    private static final float[] TITLE_SCALES = {0.875F, 0.75F, 0.625F, 0.5F};
    private static final float EFFECT_TITLE_SCALE = 1.0F;
    private static final float SUBTITLE_SCALE = 0.75F;
    private static final int FRAME_U = 0;
    private static final int FRAME_V = 28;
    private static final int FRAME = 26;
    private static final int FRAME_INSET = 4;
    private static final int FRAME_RULE = 12;
    private static final int EFFECT_SLOTS = 5;
    private static final int EFFECT_SLOT = 13;
    private static final int PANEL_H = 56;
    private static final int PANEL_DIVIDER = 56;
    private static final int PANEL_ROW = 10;
    private static final int INGREDIENT_PITCH = 18;
    private static final int RULE = 0xFFB8A27A;

    private static final int GRID_COLUMNS = 8;
    private static final int GLOWING_SIZE = 18;
    private static final float GLOWING_ART = 16.0F;
    private static final float GLOWING_CENTRE = 9.0F;
    private static final int GRID_ROWS = 6;
    private static final int EFFECTS_ROWS = 7;
    private static final int INDEX_ROWS = 7;
    private static final int INDEX_ROW_H = 24;
    private static final int RECENT_ROW_H = 22;
    private static final int CHAPTER_ROW_H = 21;
    private static final int STAGE_ROW = 24;
    private static final int FLASK_COLUMNS = 6;
    private static final long MARQUEE_PAUSE_MS = 900L;
    private static final double MARQUEE_MS_PER_PIXEL = 32.0;
    private static final int STAGE_INDENT = 16;
    private static final int RECIPE_BAR_Y = 182;
    private static final int PREPARE_W = 100;
    private static final int LINE = 10;
    private static final int STEP_ROW = 18;
    private static final int RECENT = 3;
    private static final int DELETE_CONFIRM_TICKS = 60;
    private static final double STIRS_PER_SECOND = 20.0;
    private static final double FINE_STIR = 0.25;

    private static final int INK = 0xFF3B2A1E;
    private static final int INK_HEAD = 0xFF2A1D13;
    private static final int INK_SOFT = 0xFF604A34;
    private static final int INK_FAINT = 0xFF967E5C;
    private static final int INK_RED = 0xFF8E2A22;
    private static final int INK_SHADOW = 0xFFB7A27B;
    private static final int INK_LINK = 0xFF2C5F63;
    private static final int WASH = 0x223B2A1E;
    private static final int WASH_STRONG = 0x333B2A1E;

    private enum Icon {
        CONTENTS, RECIPES, EFFECTS, INGREDIENTS, BACK, COPY, DELETE, SEAL,
        UNKNOWN, PAGE, EYE, STIR, BREW, REDSTONE, SPLASH, TOOL, EYE_CLOSED, OPUS
    }

    private enum Chapter {
        CONTENTS(Icon.SEAL),
        RECIPES(Icon.RECIPES),
        EFFECTS(Icon.EFFECTS),
        INGREDIENTS(Icon.INGREDIENTS),
        OPUS(Icon.OPUS);

        private final Icon icon;

        Chapter(Icon icon) {
            this.icon = icon;
        }

        Component title() {
            return Component.translatable("alchemia.guide.chapter." + name().toLowerCase());
        }

        Component blurb() {
            return Component.translatable("alchemia.guide.chapter." + name().toLowerCase() + ".blurb");
        }
    }

    private record Hotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        boolean covers(double bx, double by) {
            return bx >= x && bx < x + w && by >= y && by < y + h;
        }
    }

    private record Step(Component text, ItemStack item, @Nullable Icon icon, List<Component> tooltip, @Nullable Runnable action) {
        Step(Component text, ItemStack item, @Nullable Icon icon, List<Component> tooltip) {
            this(text, item, icon, tooltip, null);
        }
    }

    private static Chapter lastChapter = Chapter.CONTENTS;

    private final GuideSource source;
    private final boolean pageMode;
    private final List<Hotspot> hotspots = new ArrayList<>();
    private final List<Hotspot> rightHotspots = new ArrayList<>();

    private Chapter chapter;
    private @Nullable String openRecipe;
    private int indexSpread;
    private @Nullable PotionEnum selectedEffect;
    private @Nullable Ingredients selectedIngredient;
    private @Nullable String selectedStage;
    private Set<String> greatWork = Set.of();
    private boolean machinePage;
    private @Nullable String flaskStage;
    private int flaskFocus;
    private @Nullable Object marqueeKey;
    private long marqueeStart;
    private boolean hoveredMarquee;
    private boolean reagentPage;
    private int reagentSpread;
    private @Nullable Item selectedReagent;
    private double scroll;
    private double ingredientScroll;
    private int ingredientMax;
    private int ingredientPaneX;
    private int ingredientPaneY;
    private int ingredientPaneW;
    private int ingredientPaneH;
    private int contentHeight;
    private int viewTop;
    private int viewBottom;
    private boolean dragging;
    private double dragOffset;
    private @Nullable String pendingDelete;
    private int pendingDeleteTicks;
    private @Nullable EditBox nameField;
    private enum Editor {
        TITLE, RECIPE_NOTES, INGREDIENT_NOTES
    }

    private @Nullable Editor editor;
    private @Nullable String editKey;
    private int editX;
    private int editY;
    private int editW;
    private int editH;
    private Predicate<String> editFits = text -> true;
    private String editValid = "";

    private ItemStack stack = ItemStack.EMPTY;
    private List<BrewRecipe> recipes = List.of();
    private Set<PotionEnum> discovered = Set.of();
    private Set<String> notes = Set.of();
    private List<Ingredients> knownIngredients = List.of();
    private Set<Item> knownReagents = Set.of();
    private @Nullable String selected;

    private float scale = 1.0F;
    private int left;
    private int top;
    private double bookMouseX;
    private double bookMouseY;

    private GuideScreen(GuideSource source, boolean pageMode) {
        super(Component.translatable("alchemia.guide.title"));
        this.source = source;
        this.pageMode = pageMode;
        this.chapter = pageMode ? Chapter.RECIPES : lastChapter;
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
        scale = Math.min(1.0F, Math.min((this.width - 8) / (float) totalW, (this.height - 8) / (float) totalH));
        left = Math.round((this.width - totalW * scale) / 2.0F);
        top = Math.round((this.height - totalH * scale) / 2.0F + SEAL_RISE * scale);

        nameField = new EditBox(this.font, 0, 0, PAGE_W - 8, LINE, Component.translatable("alchemia.guide.rename")) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
            }
        };
        nameField.setBordered(false);
        nameField.setMaxLength(PacketNameBrew.MAX_LENGTH);
        nameField.setResponder(value -> {
            if (editFits.test(value)) {
                editValid = value;
            } else if (!value.equals(editValid)) {
                nameField.setValue(editValid);
            }
        });
        addWidget(nameField);
        refresh();
    }

    private boolean editing(Editor kind, String key) {
        return editor == kind && key.equals(editKey) && nameField != null;
    }

    private void startEdit(Editor kind, String key, String value, int maxLength, int width, int lines) {
        if (nameField == null) return;
        commitEdit();
        editor = kind;
        editKey = key;
        editFits = text -> true;
        nameField.setMaxLength(maxLength);
        nameField.setValue(value);
        editValid = value;
        if (lines > 0) editFits = text -> wrapLines(text, width).size() <= lines;
        nameField.setCursorPosition(value.length());
        nameField.setHighlightPos(value.length());
        setFocused(nameField);
        nameField.setFocused(true);
    }

    private void markEditor(int x, int y, int w, int h) {
        editX = x;
        editY = y;
        editW = w;
        editH = h;
    }

    private void commitEdit() {
        if (editor == null || editKey == null || nameField == null) return;
        Editor kind = editor;
        String key = editKey;
        String value = nameField.getValue().strip();
        stopEdit();
        switch (kind) {
            case TITLE -> {
                if (BrewRecipeBook.rename(stack, key, value)) send(PacketGuideAction.Action.RENAME, PacketGuideAction.renaming(key, value));
            }
            case RECIPE_NOTES -> {
                if (BrewRecipeBook.annotate(stack, key, value)) send(PacketGuideAction.Action.NOTE, PacketGuideAction.renaming(key, value));
            }
            case INGREDIENT_NOTES -> {
                if (BrewRecipeBook.annotateIngredient(stack, key, value)) {
                    send(PacketGuideAction.Action.NOTE_INGREDIENT, PacketGuideAction.renaming(key, value));
                }
            }
        }
        refresh();
    }

    private void stopEdit() {
        editor = null;
        editKey = null;
        if (nameField != null) nameField.setFocused(false);
        setFocused(null);
    }

    private List<int[]> wrapLines(String text, int width) {
        List<int[]> lines = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = start;
            int lastBreak = -1;
            while (end < text.length() && this.font.width(text.substring(start, end + 1)) <= width) {
                if (text.charAt(end) == ' ') lastBreak = end;
                end++;
            }
            if (end < text.length() && lastBreak >= start) end = lastBreak + 1;
            if (end == start) end = start + 1;
            lines.add(new int[]{start, end});
            start = end;
        }
        return lines;
    }

    private void noteHint(GuiGraphics graphics, int x, int y, int width, int lines) {
        List<FormattedCharSequence> parts = this.font.split(Component.translatable("alchemia.guide.notes.hint"), width);
        for (int index = 0; index < Math.min(lines, parts.size()); index++) {
            graphics.drawString(this.font, parts.get(index), x, y + index * LINE, INK_FAINT, false);
        }
    }

    private void drawNote(GuiGraphics graphics, String text, int x, int y, int width, int lines, boolean active) {
        List<int[]> wrapped = wrapLines(text, width);
        for (int index = 0; index < Math.min(lines, wrapped.size()); index++) {
            int[] line = wrapped.get(index);
            text(graphics, Component.literal(text.substring(line[0], line[1]).stripTrailing()), x, y + index * LINE, INK_SOFT);
        }
        if (!active || nameField == null || (System.currentTimeMillis() / 400L) % 2L != 0L) return;

        int cursor = Mth.clamp(nameField.getCursorPosition(), 0, text.length());
        int row = 0;
        int caretX = x;
        for (int index = 0; index < wrapped.size(); index++) {
            int[] line = wrapped.get(index);
            if (cursor >= line[0] && (cursor < line[1] || index == wrapped.size() - 1)) {
                row = index;
                caretX = x + this.font.width(text.substring(line[0], Math.min(cursor, line[1])));
                break;
            }
        }
        graphics.fill(caretX, y + row * LINE - 1, caretX + 1, y + row * LINE + LINE - 2, INK);
    }

    @Override
    public void onClose() {
        commitEdit();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean refresh() {
        stack = this.minecraft == null ? ItemStack.EMPTY
                : pageMode ? source.page(this.minecraft.player) : source.guide(this.minecraft.player);
        if (stack.isEmpty()) return false;

        if (pageMode) {
            BrewRecipe recipe = ItemRecipePage.recipe(stack);
            recipes = recipe == null ? List.of() : List.of(recipe);
            openRecipe = recipe == null ? null : recipe.id();
            discovered = RecipeDiscovery.knownTo(this.minecraft.player);
            notes = WorldEffects.notesOf(this.minecraft.player);
            knownIngredients = List.of();
            knownReagents = Set.of();
            greatWork = Set.of();
            selected = null;
            return recipe != null;
        }

        recipes = BrewRecipeBook.recipes(stack);
        discovered = RecipeDiscovery.knownTo(stack);
        notes = WorldEffects.notesIn(stack);
        selected = BrewRecipeBook.selected(stack);
        knownReagents = IngredientDiscovery.knownReagents(stack);
        greatWork = Set.copyOf(GreatWork.stored(stack));
        knownIngredients = new ArrayList<>(IngredientDiscovery.knownTo(stack));
        knownIngredients.sort(Comparator.comparing(ingredient -> ingredientStack(ingredient).getHoverName().getString()));
        if (openRecipe != null && BrewRecipeBook.find(stack, openRecipe) == null) openRecipe = null;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingDeleteTicks > 0 && --pendingDeleteTicks == 0) pendingDelete = null;
        if (!refresh()) onClose();
    }

    private void send(PacketGuideAction.Action action, String argument) {
        NetworkHandler.sendToServer(new PacketGuideAction(source, action, argument));
    }

    private void turnPage() {
        if (this.minecraft == null) return;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

    private void goTo(Chapter next) {
        commitEdit();
        if (next != chapter || openRecipe != null) turnPage();
        chapter = next;
        if (!pageMode) lastChapter = next;
        openRecipe = null;
        resetScroll();
    }

    private void openRecipe(BrewRecipe recipe) {
        commitEdit();
        chapter = Chapter.RECIPES;
        openRecipe = recipe.id();
        pendingDelete = null;
        resetScroll();
        turnPage();
    }

    private void resetScroll() {
        scroll = 0;
        ingredientScroll = 0;
        dragging = false;
    }

    private @Nullable BrewRecipe currentRecipe() {
        if (openRecipe == null) return null;
        for (BrewRecipe recipe : recipes) {
            if (recipe.id().equals(openRecipe)) return recipe;
        }
        return null;
    }

    private void toBook(double mouseX, double mouseY) {
        bookMouseX = (mouseX - left) / scale;
        bookMouseY = (mouseY - top) / scale;
    }

    private boolean hovered(int x, int y, int w, int h) {
        return bookMouseX >= x && bookMouseX < x + w && bookMouseY >= y && bookMouseY < y + h;
    }

    private void hotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        hotspots.add(new Hotspot(x, y, w, h, action, tooltip));
    }

    private void hotspot(int x, int y, int w, int h, @Nullable Runnable action) {
        hotspot(x, y, w, h, action, List::of);
    }

    private void clippedRightClick(int x, int y, int w, int h, Runnable action) {
        int y0 = Math.max(y, viewTop);
        int y1 = Math.min(y + h, viewBottom);
        if (y1 > y0) rightHotspots.add(new Hotspot(x, y0, w, y1 - y0, action, List::of));
    }

    private void clippedHotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        int y0 = Math.max(y, viewTop);
        int y1 = Math.min(y + h, viewBottom);
        if (y1 > y0) hotspot(x, y0, w, y1 - y0, action, tooltip);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, a);
        if (stack.isEmpty()) return;
        RenderSystem.enableBlend();

        toBook(mouseX, mouseY);
        hotspots.clear();
        rightHotspots.clear();
        ingredientMax = 0;
        hoveredMarquee = false;

        graphics.pose().pushPose();
        graphics.pose().translate(left, top, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        graphics.blit(BOOK, 0, 0, (float) (0), (float) (0), BOOK_W, BOOK_H, BOOK_SHEET_W, BOOK_SHEET_H);
        if (!pageMode) extractTabs(graphics);
        ribbon(graphics);
        seal(graphics, BOOK_W - 13, -SEAL_RISE, false, this::onClose, Component.translatable("alchemia.guide.close"));

        if (chapter == Chapter.RECIPES && currentRecipe() != null) {
            extractRecipe(graphics, currentRecipe());
        } else {
            switch (chapter) {
                case CONTENTS -> extractContents(graphics);
                case RECIPES -> extractIndex(graphics);
                case EFFECTS -> extractEffects(graphics);
                case INGREDIENTS -> extractIngredients(graphics);
                case OPUS -> extractOpus(graphics);
            }
        }
        if (!hoveredMarquee) marqueeKey = null;

        graphics.pose().popPose();

        for (int index = hotspots.size() - 1; index >= 0; index--) {
            Hotspot spot = hotspots.get(index);
            if (!spot.covers(bookMouseX, bookMouseY)) continue;
            List<Component> lines = spot.tooltip().get();
            if (!lines.isEmpty()) graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            break;
        }
    }

    private void widget(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(WIDGETS, x, y, (float) (u), (float) (v), w, h, WIDGET_SHEET, WIDGET_SHEET);
    }

    private void bookPart(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(BOOK, x, y, (float) (u), (float) (v), w, h, BOOK_SHEET_W, BOOK_SHEET_H);
    }

    private void seal(GuiGraphics graphics, int x, int y, boolean back, Runnable action, Component tooltip) {
        boolean over = hovered(x + 1, y + 1, SEAL - 2, SEAL - 2);
        int index = (back ? 2 : 0) + (over ? 1 : 0);
        widget(graphics, x, y, SEAL_U + index * (SEAL + 1), SEAL_V, SEAL, SEAL);
        hotspot(x + 1, y + 1, SEAL - 2, SEAL - 2, action, () -> List.of(tooltip));
    }

    private void ribbon(GuiGraphics graphics) {
        if (pageMode || selected == null) return;
        BrewRecipe marked = BrewRecipeBook.find(stack, selected);
        if (marked == null) return;

        BrewRecipe shown = chapter == Chapter.RECIPES ? currentRecipe() : null;
        if (shown != null && shown.id().equals(marked.id())) {
            hangRibbon(graphics, RIBBON_X, PAGE_TOP_EDGE + 1);
            graphics.fill(RIBBON_X + RIBBON_W, PAGE_TOP_EDGE, RIBBON_X + RIBBON_W + 1, PAGE_BOTTOM_EDGE + 1, 0x30000000);
            return;
        }

        boolean ahead = viewPosition() < 2 + recipes.indexOf(marked);
        int x = RIBBON_X + (ahead ? RIBBON_OFFSET : -RIBBON_OFFSET);
        int top = PAGE_BOTTOM_EDGE + 1;
        hangRibbon(graphics, x, top);
        bookPart(graphics, x, top, x, top, RIBBON_W, PAGE_STACK);
        graphics.fill(x, top + PAGE_STACK, x + RIBBON_W, top + PAGE_STACK + 1, 0x60000000);
        graphics.fill(x, top + PAGE_STACK + 1, x + RIBBON_W, top + PAGE_STACK + 2, 0x28000000);
    }

    private void hangRibbon(GuiGraphics graphics, int x, int from) {
        int tailY = BOOK_H + RIBBON_SHOWN - RIBBON_TAIL_H;
        for (int y = from; y < tailY; y += RIBBON_BODY_H) {
            bookPart(graphics, x, y, RIBBON_U, 0, RIBBON_W, Math.min(RIBBON_BODY_H, tailY - y));
        }
        bookPart(graphics, x, tailY, RIBBON_U, RIBBON_TAIL_V, RIBBON_W, RIBBON_TAIL_H);
        graphics.fill(x, from, x + RIBBON_W, from + 1, 0x66000000);
        graphics.fill(x, from + 1, x + RIBBON_W, from + 2, 0x2A000000);
    }

    private int viewPosition() {
        return switch (chapter) {
            case CONTENTS -> 0;
            case RECIPES -> {
                BrewRecipe open = currentRecipe();
                yield open == null ? 1 : 2 + recipes.indexOf(open);
            }
            case EFFECTS -> Integer.MAX_VALUE - 2;
            case INGREDIENTS -> Integer.MAX_VALUE - 1;
            case OPUS -> Integer.MAX_VALUE;
        };
    }

    private void icon(GuiGraphics graphics, Icon icon, int x, int y, boolean faded) {
        widget(graphics, x, y, icon.ordinal() * ICON_STEP, faded ? ICON_STEP : 0, ICON, ICON);
    }

    private void divider(GuiGraphics graphics, int pageX, int y, int width) {
        int w = Math.min(width, DIVIDER_W);
        int u = DIVIDER_U + (DIVIDER_W - w) / 2;
        widget(graphics, pageX + (PAGE_W - w) / 2, y, u, DIVIDER_V, w, DIVIDER_H);
    }

    private void button(GuiGraphics graphics, int x, int y, int w, int state) {
        int v = BUTTON_V + state * (BUTTON_H + 1);
        widget(graphics, x, y, BUTTON_U, v, BUTTON_CAP, BUTTON_H);
        int middle = BUTTON_W - BUTTON_CAP * 2;
        for (int drawn = BUTTON_CAP; drawn < w - BUTTON_CAP; drawn += middle) {
            widget(graphics, x + drawn, y, BUTTON_U + BUTTON_CAP, v, Math.min(middle, w - BUTTON_CAP - drawn), BUTTON_H);
        }
        widget(graphics, x + w - BUTTON_CAP, y, BUTTON_U + BUTTON_W - BUTTON_CAP, v, BUTTON_CAP, BUTTON_H);
    }

    private void smallButton(GuiGraphics graphics, int x, int y, int state, Icon icon) {
        widget(graphics, x, y, SMALL_U + state * (SMALL + 1), SMALL_V, SMALL, SMALL);
        icon(graphics, icon, x + 2, y + 2, state == 2);
    }

    private void cell(GuiGraphics graphics, int x, int y, int state) {
        widget(graphics, x, y, CELL_U + state * (CELL + 1), CELL_V, CELL, CELL);
    }

    private void text(GuiGraphics graphics, Component text, int x, int y, int colour) {
        graphics.drawString(this.font, text, x, y, colour, false);
    }

    private void centred(GuiGraphics graphics, Component text, int pageX, int y, int colour) {
        text(graphics, text, pageX + (PAGE_W - this.font.width(text)) / 2, y, colour);
    }

    private int wrapped(GuiGraphics graphics, Component text, int x, int y, int width, int colour) {
        for (FormattedCharSequence line : this.font.split(text, width)) {
            graphics.drawString(this.font, line, x, y, colour, false);
            y += LINE;
        }
        return y;
    }

    private int wrappedCentred(GuiGraphics graphics, Component text, int pageX, int y, int colour) {
        for (FormattedCharSequence line : this.font.split(text, PAGE_W - 8)) {
            graphics.drawString(this.font, line, pageX + (PAGE_W - this.font.width(line)) / 2, y, colour, false);
            y += LINE;
        }
        return y;
    }

    private String clip(Component text, int width) {
        String plain = text.getString();
        if (this.font.width(plain) <= width) return plain;
        return this.font.plainSubstrByWidth(plain, width - this.font.width("…")) + "…";
    }

    private int heading(GuiGraphics graphics, Component title, int pageX, int y) {
        centred(graphics, title, pageX, y, INK_HEAD);
        divider(graphics, pageX, y + LINE + 1, PAGE_W - 10);
        return y + LINE + DIVIDER_H + 4;
    }

    private int chapterHeading(GuiGraphics graphics, Component title, int pageX, int y) {
        initialled(graphics, title.getString(), pageX + PAGE_W / 2.0F, y, 1.0F);
        divider(graphics, pageX, y + TITLE_RULE, PAGE_W - 10);
        return y + TITLE_RULE + DIVIDER_H + 4;
    }

    private void initialled(GuiGraphics graphics, String text, float centreX, float y, float scale) {
        inked(graphics, text, centreX - letteredWidth(text) * scale / 2.0F, y, scale, INK, true);
    }

    private void inked(GuiGraphics graphics, String text, float x, float y, float scale, int colour, boolean capital) {
        String initial = !capital || text.isEmpty() ? "" : text.substring(0, text.offsetByCodePoints(0, 1));
        String rest = text.substring(initial.length());
        lettered(graphics, text, x + scale, y + scale, scale, INK_SHADOW);
        lettered(graphics, initial, x, y, scale, INK_RED);
        lettered(graphics, rest, x + letteredWidth(initial) * scale, y, scale, colour);
    }

    private void wash(GuiGraphics graphics, int x, int y, int w, int h, boolean strong) {
        graphics.fill(x, y, x + w, y + h, strong ? WASH_STRONG : WASH);
    }

    private void extractTabs(GuiGraphics graphics) {
        for (Chapter tab : Chapter.values()) {
            int y = TAB_TOP + tab.ordinal() * TAB_SPACING;
            boolean active = tab == chapter;
            int drop = active ? 0 : TAB_DROP;
            boolean over = hovered(PAGE_RIGHT_EDGE + 1, y, TAB_W - TAB_REACH, TAB_H + drop);
            int reach = active ? TAB_REACH : over ? TAB_REACH / 2 : 0;
            int x = PAGE_RIGHT_EDGE + 1 - TAB_REACH + reach;
            int root = PAGE_RIGHT_EDGE + 1 + PAGE_STACK;

            int v = TAB_ROW_V + tab.ordinal() * TAB_ROW_STEP;
            if (active) {
                widget(graphics, x, y, TAB_STRAIGHT_U, v, TAB_W, TAB_H);
            } else {
                widget(graphics, x, y, TAB_FOLDED_U, v, TAB_W, TAB_H + TAB_DROP);
            }
            int patchX = x + TAB_PATCH_X;
            int patchY = y + TAB_PATCH_Y + drop;
            if (tab == Chapter.RECIPES) {
                graphics.renderItem(healingPotion(), patchX, patchY);
            } else if (tab == Chapter.OPUS) {
                graphics.renderItem(tabletIcon(), patchX + 1, patchY);
            } else if (tab == Chapter.EFFECTS) {
                glowing(graphics, patchX + 8.5F, patchY + 8.5F, ICON);
            } else if (tab.icon == Icon.SEAL) {
                icon(graphics, tab.icon, patchX + 2, patchY + 3, false);
            } else {
                icon(graphics, tab.icon, patchX + 3, patchY + 3, false);
            }
            bookPart(graphics, x, y, x, y, root - x, TAB_H + drop);
            graphics.fill(root, y + 1, root + 1, y + TAB_H + drop - 1, 0x55000000);
            hotspot(PAGE_RIGHT_EDGE + 1, y, TAB_W - TAB_REACH, TAB_H + drop, () -> {
                if (tab != chapter) {
                    reagentPage = false;
                    reagentSpread = 0;
                    machinePage = false;
                }
                goTo(tab);
            }, () -> List.of(tab.title()));
        }
    }

    private void corner(GuiGraphics graphics, boolean next, Runnable action, Component tooltip) {
        int x = next ? PAGE_RIGHT_EDGE - CELL + 1 : PAGE_LEFT_EDGE;
        int y = PAGE_BOTTOM_EDGE - CELL + 1;
        boolean over = hovered(x, y, CELL, CELL);
        int index = (next ? 0 : 2) + (over ? 1 : 0);
        widget(graphics, x, y, CORNER_U + index * (CELL + 1), CORNER_V, CELL, CELL);
        hotspot(x, y, CELL, CELL, action, () -> List.of(tooltip));
    }

    private void extractContents(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(LEFT_X + (PAGE_W - TITLE_ART_W * 2) / 2.0F, TOP, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        widget(graphics, 0, 0, TITLE_ART_U, TITLE_ART_V, TITLE_ART_W, TITLE_ART_H);
        graphics.pose().popPose();

        int y = wrappedCentred(graphics, Component.translatable("alchemia.guide.cover_subtitle"), LEFT_X, TOP + 27, INK_FAINT);
        divider(graphics, LEFT_X, y + 1, PAGE_W - 20);
        y += DIVIDER_H + 7;

        int used = recipes.size();
        int pages = BrewRecipeBook.pages(stack);
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(new ItemStack(Items.WRITABLE_BOOK), gx, gy), "alchemia.guide.stat.pages",
                Component.translatable("alchemia.guide.stat.of", used, pages));
        y = stat(graphics, y, (gx, gy) -> glowing(graphics, gx + 8.5F, gy + 8.5F, GLOWING_ART), "alchemia.guide.stat.effects",
                Component.translatable("alchemia.guide.stat.of", discovered.size(), RecipeDiscovery.total()));
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(new ItemStack(Items.BRUSH), gx, gy), "alchemia.guide.stat.secrets",
                Component.literal(String.valueOf(notes.size())));
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(ingredientStack(Ingredients.Druids_Rosemary), gx, gy), "alchemia.guide.stat.ingredients",
                Component.translatable("alchemia.guide.stat.of", IngredientDiscovery.found(stack), IngredientDiscovery.totalAll()));

        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(tabletIcon(), gx, gy), "alchemia.guide.stat.opus",
                Component.translatable("alchemia.guide.stat.of", greatWork.size(), GreatWork.total()));

        BrewRecipe prepared = selected == null ? null : BrewRecipeBook.find(stack, selected);
        Component preparedName = prepared == null ? Component.translatable("alchemia.guide.stat.none") : prepared.displayName();
        int preparedY = y;
        y = stat(graphics, y, (gx, gy) -> icon(graphics, Icon.SEAL, gx + 2, gy + 2, false), "alchemia.guide.stat.prepared", Component.literal(clip(preparedName, 70)));
        if (prepared != null) hotspot(LEFT_X, preparedY - 2, PAGE_W, 14, () -> openRecipe(prepared));


        int ry = scrolledHeading(graphics, Component.translatable("alchemia.guide.chapters"), RIGHT_X, TOP + 1, PAGE_W);
        for (Chapter entry : new Chapter[]{Chapter.RECIPES, Chapter.EFFECTS, Chapter.INGREDIENTS, Chapter.OPUS}) {
            boolean over = hovered(RIGHT_X - 2, ry - 2, PAGE_W + 4, CHAPTER_ROW_H);
            if (over) wash(graphics, RIGHT_X - 2, ry - 2, PAGE_W + 4, CHAPTER_ROW_H + 1, false);
            cell(graphics, RIGHT_X, ry, over ? 1 : 0);
            if (entry == Chapter.RECIPES) {
                graphics.renderItem(healingPotion(), RIGHT_X, ry + 1);
            } else if (entry == Chapter.OPUS) {
                graphics.renderItem(tabletIcon(), RIGHT_X + 1, ry + 1);
            } else if (entry == Chapter.EFFECTS) {
                glowing(graphics, RIGHT_X + 9.0F, ry + 9.0F, ICON);
            } else {
                icon(graphics, entry.icon, RIGHT_X + 3, ry + 3, false);
            }
            text(graphics, entry.title(), RIGHT_X + 24, ry + 1, over ? INK_LINK : INK_HEAD);
            marquee(graphics, entry.blurb(), RIGHT_X + 24, ry + 11, PAGE_W - 26, INK_FAINT, over ? entry : null);
            hotspot(RIGHT_X - 2, ry - 2, PAGE_W + 4, CHAPTER_ROW_H, () -> goTo(entry));
            ry += CHAPTER_ROW_H;
        }

        ry = scrolledHeading(graphics, Component.translatable("alchemia.guide.recent"), RIGHT_X, ry + 1, PAGE_W);
        if (recipes.isEmpty()) {
            wrappedCentred(graphics, Component.translatable("alchemia.guide.recipes.none"), RIGHT_X, ry, INK_FAINT);
            return;
        }
        for (int index = recipes.size() - 1; index >= Math.max(0, recipes.size() - RECENT); index--) {
            BrewRecipe recipe = recipes.get(index);
            recipeRow(graphics, recipe, RIGHT_X, ry, PAGE_W, RECENT_ROW_H);
            ry += RECENT_ROW_H;
        }
    }

    private void marquee(GuiGraphics graphics, Component text, int x, int y, int width, int colour, @Nullable Object hover) {
        int full = this.font.width(text);
        if (full <= width) {
            text(graphics, text, x, y, colour);
            return;
        }
        if (hover == null) {
            text(graphics, Component.literal(clip(text, width)), x, y, colour);
            return;
        }
        hoveredMarquee = true;
        long now = Util.getMillis();
        if (!hover.equals(marqueeKey)) {
            marqueeKey = hover;
            marqueeStart = now;
        }
        int travel = full - width;
        long glide = (long) (travel * MARQUEE_MS_PER_PIXEL);
        long cycle = 2 * (MARQUEE_PAUSE_MS + glide);
        long at = (now - marqueeStart) % cycle;
        double offset;
        if (at < MARQUEE_PAUSE_MS) offset = 0;
        else if (at < MARQUEE_PAUSE_MS + glide) offset = (at - MARQUEE_PAUSE_MS) / MARQUEE_MS_PER_PIXEL;
        else if (at < 2 * MARQUEE_PAUSE_MS + glide) offset = travel;
        else offset = travel - (at - 2 * MARQUEE_PAUSE_MS - glide) / MARQUEE_MS_PER_PIXEL;

        scissor(graphics, x, y - 1, x + width, y + LINE);
        graphics.pose().pushPose();
        graphics.pose().translate((float) -Math.round(offset), 0.0F, 0.0F);
        text(graphics, text, x, y, colour);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private String deskHint() {
        if (this.minecraft != null && this.minecraft.level != null
                && this.minecraft.level.getBlockEntity(source.pos()) instanceof TileEntityResearchDesk) {
            return "alchemia.guide.desk_hint";
        }
        return "alchemia.guide.tome_hint";
    }

    private interface Glyph {
        void draw(int x, int y);
    }

    private int stat(GuiGraphics graphics, int y, Glyph glyph, String key, Component value) {
        glyph.draw(LEFT_X, y - 4);
        text(graphics, Component.translatable(key), LEFT_X + 20, y, INK);
        text(graphics, value, LEFT_X + PAGE_W - 2 - this.font.width(value), y, INK_HEAD);
        graphics.fill(LEFT_X + 20, y + LINE, LEFT_X + PAGE_W - 2, y + LINE + 1, 0x22604A34);
        return y + 17;
    }

    private static ItemStack potionFor(BrewRecipe recipe) {
        return GuideNews.potionIcon(recipe);
    }

    private Component effectSummary(BrewRecipe recipe) {
        List<String> parts = new ArrayList<>();
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            parts.add(effectLabel(effect).getString());
        }
        return Component.literal(String.join(", ", parts));
    }

    private Component effectLabel(BrewRecipe.BrewEffect effect) {
        PotionEnum potion = effect.asPotion();
        Component name = potion != null && !discovered.contains(potion)
                ? Component.translatable("alchemia.guide.effect.unknown") : effect.displayName();
        return Component.translatable("alchemia.guide.effect.line", name, Component.translatable("enchantment.level." + effect.potency()));
    }

    private void recipeRow(GuiGraphics graphics, BrewRecipe recipe, int x, int y, int width, int rowH) {
        boolean over = hovered(x - 2, y - 2, width + 4, rowH - 2);
        boolean prepared = recipe.id().equals(selected);
        if (over) wash(graphics, x - 2, y - 2, width + 4, rowH - 2, false);

        graphics.renderItem(potionFor(recipe), x, y + 1);
        int textW = width - 20 - MARKER_COLUMN;
        text(graphics, Component.literal(clip(recipe.displayName(), textW)), x + 20, y + 1, over ? INK_LINK : INK_HEAD);
        Component detail = Component.translatable("alchemia.guide.recipe.entry", recipe.brewBase().displayName(), effectSummary(recipe));
        text(graphics, Component.literal(clip(detail, textW)), x + 20, y + 11, INK_FAINT);
        int sealX = x + width - SEAL;
        int sealY = y + (rowH - 2 - SEAL) / 2 - 1;
        boolean overSeal = hovered(sealX + 1, sealY + 1, SEAL - 2, SEAL - 2);
        if (prepared) {
            widget(graphics, sealX, sealY, PREPARED_U, PREPARED_V, SEAL, SEAL);
        } else if (canPrepare()) {
            widget(graphics, sealX, sealY, EMPTY_SEAL_U + (overSeal ? SEAL + 1 : 0), PREPARED_V, SEAL, SEAL);
        }

        hotspot(x - 2, y - 2, width + 4, rowH - 2, () -> openRecipe(recipe), () -> {
            List<Component> lines = new ArrayList<>();
            lines.add(recipe.displayName());
            lines.add(Component.translatable("alchemia.guide.recipe.base", recipe.brewBase().displayName()));
            for (BrewRecipe.BrewEffect effect : recipe.effects()) lines.add(effectLabel(effect));
            if (prepared) lines.add(Component.translatable("alchemia.guide.prepared"));
            return lines;
        });
        if (canPrepare()) {
            hotspot(sealX + 1, sealY + 1, SEAL - 2, SEAL - 2, () -> prepare(recipe), () -> List.of(Component.translatable(
                    prepared ? "alchemia.guide.prepared.clear" : "alchemia.guide.prepare.quick")));
        }
    }

    private void blankRow(GuiGraphics graphics, int x, int y, int width) {
        icon(graphics, Icon.PAGE, x + 2, y + 3, true);
        text(graphics, Component.translatable("alchemia.guide.blank_page"), x + 20, y + 5, INK_FAINT);
        graphics.fill(x + 20, y + 15, x + width - 4, y + 16, 0x18604A34);
    }

    private int indexSpreads() {
        int entries = Math.max(BrewRecipeBook.pages(stack), recipes.size());
        return Math.max(1, Mth.ceil(entries / (float) (INDEX_ROWS * 2)));
    }

    private void extractIndex(GuiGraphics graphics) {
        int spreads = indexSpreads();
        indexSpread = Mth.clamp(indexSpread, 0, spreads - 1);
        int pages = Math.max(BrewRecipeBook.pages(stack), recipes.size());

        chapterHeading(graphics, Component.translatable("alchemia.guide.recipes.title"), LEFT_X, TOP + 2);
        heading(graphics, Component.translatable("alchemia.guide.recipes.used", recipes.size(), pages), RIGHT_X, TOP + 5);

        int first = indexSpread * INDEX_ROWS * 2;
        for (int slot = 0; slot < INDEX_ROWS * 2; slot++) {
            int entry = first + slot;
            if (entry >= pages) break;
            int pageX = slot < INDEX_ROWS ? LEFT_X : RIGHT_X;
            int y = TOP + 26 + (slot % INDEX_ROWS) * INDEX_ROW_H;
            if (entry < recipes.size()) {
                recipeRow(graphics, recipes.get(entry), pageX, y, PAGE_W, INDEX_ROW_H);
            } else {
                blankRow(graphics, pageX, y, PAGE_W);
            }
        }

        if (recipes.isEmpty()) {
            wrappedCentred(graphics, Component.translatable("alchemia.guide.recipes.none"), LEFT_X, TOP + 30 + INDEX_ROWS * INDEX_ROW_H - 30, INK_FAINT);
        }

        Component folio = Component.translatable("alchemia.guide.page", indexSpread + 1, spreads);
        centred(graphics, folio, LEFT_X, BOTTOM - 7, INK_FAINT);
        centred(graphics, Component.translatable("alchemia.guide.bind_more"), RIGHT_X, BOTTOM - 7, INK_FAINT);

        if (indexSpread > 0) corner(graphics, false, () -> flipIndex(-1), Component.translatable("alchemia.guide.previous"));
        if (indexSpread < spreads - 1) corner(graphics, true, () -> flipIndex(1), Component.translatable("alchemia.guide.next"));
    }

    private void flipIndex(int delta) {
        int next = Mth.clamp(indexSpread + delta, 0, indexSpreads() - 1);
        if (next == indexSpread) return;
        indexSpread = next;
        turnPage();
    }

    private void flipRecipe(int delta) {
        BrewRecipe current = currentRecipe();
        if (current == null) return;
        int index = recipes.indexOf(current);
        int next = index + delta;
        if (next < 0 || next >= recipes.size()) return;
        openRecipe(recipes.get(next));
        indexSpread = next / (INDEX_ROWS * 2);
    }

    private void extractRecipe(GuiGraphics graphics, BrewRecipe recipe) {
        if (!pageMode) {
            seal(graphics, PAGE_LEFT_EDGE + 2, PAGE_TOP_EDGE + 2, true, () -> {
                commitEdit();
                openRecipe = null;
                resetScroll();
                turnPage();
            }, Component.translatable("alchemia.guide.back"));
        }

        int y = recipeTitle(graphics, recipe, TOP);

        y = effectRow(graphics, recipe, y + 4);
        y = potionPanel(graphics, recipe, y + 4);
        y = ingredientRow(graphics, recipe, y + 5);
        notesBox(graphics, recipe, y + 5, BOTTOM - 12);

        if (pageMode) {
            centred(graphics, Component.translatable("alchemia.page.loose"), LEFT_X, BOTTOM - 7, INK_FAINT);
        } else {
            int index = recipes.indexOf(recipe);
            centred(graphics, Component.translatable("alchemia.guide.folio", index + 1, BrewRecipeBook.pages(stack)), LEFT_X, BOTTOM - 7, INK_FAINT);
            if (index > 0) corner(graphics, false, () -> flipRecipe(-1), Component.translatable("alchemia.guide.previous_recipe"));
            if (index < recipes.size() - 1) corner(graphics, true, () -> flipRecipe(1), Component.translatable("alchemia.guide.next_recipe"));
        }

        scrollPanel(graphics, TOP, RECIPE_BAR_Y - 4, () -> recipeMethod(graphics, recipe));
        extractRecipeBar(graphics, recipe, RECIPE_BAR_Y);
    }

    private List<Component> effectTooltip(BrewRecipe.BrewEffect effect, @Nullable PotionEnum potion) {
        List<Component> lines = new ArrayList<>();
        lines.add(effectLabel(effect));
        if (potion != null) lines.addAll(WorldEffects.guideLines(notes, potion));
        return lines;
    }

    private void effectIcon(GuiGraphics graphics, @Nullable PotionEnum potion, int x, int y, int size) {
        if (potion != null && discovered.contains(potion)) {
            sprite(graphics, effectSprite(potion), x, y, size, size, -1);
        } else if (potion != null && WorldEffects.anyVisible(notes, potion)) {
            icon(graphics, Icon.EYE_CLOSED, x + (size - ICON) / 2, y + (size - ICON) / 2, false);
        } else {
            icon(graphics, Icon.UNKNOWN, x + (size - ICON) / 2, y + (size - ICON) / 2, true);
        }
    }

    private int effectRow(GuiGraphics graphics, BrewRecipe recipe, int y) {
        List<BrewRecipe.BrewEffect> levels = new ArrayList<>();
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            for (int level = 0; level < Math.max(1, effect.potency()); level++) levels.add(effect);
        }
        int slots = Math.max(EFFECT_SLOTS, levels.size());
        int available = PAGE_W - 2 * (VINE_W + 3);
        boolean vines = slots * 9 <= available;
        if (!vines) available = PAGE_W;
        int pitch = Math.max(6, Math.min(EFFECT_SLOT, available / slots));
        int size = Math.min(ICON, pitch - 1);
        int rowW = slots * pitch;
        int x0 = LEFT_X + (PAGE_W - rowW) / 2;
        if (vines) {
            widget(graphics, x0 - VINE_W - 2, y + 2, VINE_U, VINE_V, VINE_W, VINE_H);
            widget(graphics, x0 + rowW + 1, y + 2, VINE_RIGHT_U, VINE_V, VINE_W, VINE_H);
        }

        for (int slot = 0; slot < slots; slot++) {
            int x = x0 + slot * pitch;
            if (slot >= levels.size()) {
                widget(graphics, x + (pitch - DIAMOND) / 2, y + (ICON - DIAMOND) / 2 + 1, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND);
                continue;
            }
            BrewRecipe.BrewEffect effect = levels.get(slot);
            PotionEnum potion = effect.asPotion();
            effectIcon(graphics, potion, x + (pitch - size) / 2, y + (ICON - size) / 2, size);
            Runnable follow = potion == null || pageMode ? null : () -> followEffect(potion);
            hotspot(x, y, pitch, ICON, follow, () -> effectTooltip(effect, potion));
        }
        return y + ICON;
    }

    private int potionPanel(GuiGraphics graphics, BrewRecipe recipe, int y) {
        int bottom = y + PANEL_H;
        int dividerX = LEFT_X + PANEL_DIVIDER;
        graphics.fill(LEFT_X - 3, y, LEFT_X + PAGE_W + 3, y + 1, RULE);
        graphics.fill(LEFT_X - 3, bottom, LEFT_X + PAGE_W + 3, bottom + 1, RULE);
        graphics.fill(dividerX, y, dividerX + 1, bottom, RULE);
        widget(graphics, dividerX - JOINT / 2, y - JOINT / 2, JOINT_U, JOINT_V, JOINT, JOINT);
        widget(graphics, dividerX - JOINT / 2, bottom - JOINT / 2, JOINT_U, JOINT_V, JOINT, JOINT);

        int discX = LEFT_X + (PANEL_DIVIDER - DISC) / 2;
        int discY = y + (PANEL_H - DISC) / 2 + 1;
        widget(graphics, discX, discY, DISC_U, DISC_V, DISC, DISC);
        if (recipe.steps().contains(String.valueOf(BrewRecipe.STEP_GLOW))) {
            int glow = (int) Math.round(190 + 55 * Math.sin(Util.getMillis() / 260.0));
            sprite(graphics, AlchemiaSprites.RADIANT_HALO, discX, discY, DISC, DISC, ARGB.color(glow, 0xFFFFFF));
        }
        graphics.pose().pushPose();
        graphics.pose().translate(discX + (DISC - 32) / 2.0F, discY + (DISC - 32) / 2.0F, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.renderItem(potionFor(recipe), 0, 0);
        graphics.pose().popPose();

        int listX = dividerX + 5;
        int textW = LEFT_X + PAGE_W - listX - 12;
        int rowY = y + 4;
        List<BrewRecipe.BrewEffect> effects = recipe.effects();
        int rows = (PANEL_H - 6) / PANEL_ROW;
        for (int index = 0; index < rows; index++) {
            int lineY = rowY + index * PANEL_ROW + PANEL_ROW - 1;
            graphics.fill(listX, lineY, LEFT_X + PAGE_W + 1, lineY + 1, 0x40B8A27A);
            if (index >= effects.size()) continue;

            boolean overflow = index == rows - 1 && effects.size() > rows;
            int textY = rowY + index * PANEL_ROW;
            if (overflow) {
                text(graphics, Component.translatable("alchemia.guide.more_effects", effects.size() - index), listX + 11, textY, INK_FAINT);
                continue;
            }
            BrewRecipe.BrewEffect effect = effects.get(index);
            PotionEnum potion = effect.asPotion();
            boolean over = potion != null && !pageMode && hovered(listX, textY - 1, LEFT_X + PAGE_W - listX, PANEL_ROW);
            effectIcon(graphics, potion, listX, textY - 1, 9);
            text(graphics, Component.literal(clip(effectLabel(effect), textW)), listX + 11, textY, over ? INK_LINK : INK);
            Runnable follow = potion == null || pageMode ? null : () -> followEffect(potion);
            hotspot(listX, textY - 1, LEFT_X + PAGE_W - listX, PANEL_ROW, follow, () -> effectTooltip(effect, potion));
        }
        return bottom;
    }

    private int ingredientRow(GuiGraphics graphics, BrewRecipe recipe, int y) {
        centred(graphics, Component.translatable("alchemia.guide.ingredients"), LEFT_X, y, INK_HEAD);
        y += LINE + 2;

        Item baseItem = baseItem(recipe);
        widget(graphics, LEFT_X, y, RING_U, RING_V, RING, RING);
        if (baseItem != null) GuideInk.item(graphics, new ItemStack(baseItem), LEFT_X + 3, y + 3, GuideInk.PLAIN);
        hotspot(LEFT_X, y, RING, RING, null, () -> List.of(Component.translatable("alchemia.guide.recipe.base", recipe.brewBase().displayName())));

        Map<ItemStack, Integer> tally = new java.util.LinkedHashMap<>(recipe.tally());
        for (Imbuements.Attuner attuner : Imbuements.of(recipe)) tally.merge(attuner.stack(), 1, Integer::sum);
        int countW = 0;
        for (int needed : tally.values()) countW = Math.max(countW, this.font.width(String.valueOf(needed)));
        int pitch = 16 + countW + 3;
        int rowStart = LEFT_X + RING + 4;
        int perRow = Math.max(1, (LEFT_X + PAGE_W + 2 - 16 - countW - rowStart) / pitch + 1);
        int rows = Math.max(1, (tally.size() + perRow - 1) / perRow);
        int shown = Math.min(rows, INGREDIENT_ROWS_SHOWN);
        int paneTop = y + 2;
        int paneBottom = y + 3 + shown * INGREDIENT_ROW - 2;
        int maxScroll = (rows - shown) * INGREDIENT_ROW;
        ingredientScroll = Mth.clamp(ingredientScroll, 0, maxScroll);
        ingredientMax = maxScroll;
        ingredientPaneX = rowStart - 1;
        ingredientPaneY = paneTop;
        ingredientPaneW = LEFT_X + PAGE_W + 7 - ingredientPaneX;
        ingredientPaneH = paneBottom - paneTop;
        if (maxScroll > 0) scissor(graphics, rowStart - 1, paneTop, LEFT_X + PAGE_W + 3, paneBottom);
        int x = rowStart;
        int rowY = y + 3 - (int) ingredientScroll;

        for (Map.Entry<ItemStack, Integer> entry : tally.entrySet()) {
            if (x + 16 + countW > LEFT_X + PAGE_W + 2) {
                x = rowStart;
                rowY += INGREDIENT_ROW;
            }
            ItemStack item = entry.getKey();
            int needed = entry.getValue();
            int held = countHeld(item);
            boolean enough = held >= needed;
            GuideInk.item(graphics, item, x, rowY, enough ? GuideInk.PLAIN : GuideInk.SHORT);
            text(graphics, Component.literal(String.valueOf(needed)), x + 17, rowY + 8, enough ? INK : INK_RED);

            if (rowY + 8 >= paneTop && rowY + 8 <= paneBottom) {
                hotspot(x - 1, Math.max(rowY - 1, paneTop), pitch, Math.min(rowY + 17, paneBottom) - Math.max(rowY - 1, paneTop),
                        pageMode ? null : followItem(item), () -> ingredientTooltip(item, needed, held));
            }
            x += pitch;
        }
        if (maxScroll > 0) {
            graphics.disableScissor();
            int trackX = LEFT_X + PAGE_W + 4;
            int trackH = paneBottom - paneTop;
            int thumbH = Math.max(6, trackH * shown / rows);
            int thumbY = paneTop + (int) Math.round((trackH - thumbH) * ingredientScroll / maxScroll);
            graphics.fill(trackX, paneTop, trackX + 2, paneBottom, 0x33B8A27A);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, RULE);
        }
        return Math.max(y + RING, y + 3 + shown * INGREDIENT_ROW - 3);
    }

    private List<Component> ingredientTooltip(ItemStack item, int needed, int held) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("alchemia.guide.ingredient.count", needed, item.getHoverName()));
        Component treatment = treatment(item);
        if (treatment != null) lines.add(treatment);
        lines.add(Component.translatable("alchemia.guide.ingredient.carried", held));
        return lines;
    }

    private static @Nullable Item baseItem(BrewRecipe recipe) {
        for (muddykat.alchemia.common.potion.BrewBase.Fill fill : recipe.brewBase().fills()) {
            Item item = fill.itemValue();
            if (item != null) return item;
        }
        return null;
    }

    private void notesBox(GuiGraphics graphics, BrewRecipe recipe, int y, int bottom) {
        if (bottom - y < 2 * LINE + 2) return;
        graphics.fill(LEFT_X - 3, y, LEFT_X + PAGE_W + 3, y + 1, RULE);
        graphics.fill(LEFT_X - 3, bottom, LEFT_X + PAGE_W + 3, bottom + 1, RULE);
        graphics.fill(LEFT_X - 3, y, LEFT_X - 2, bottom, RULE);
        graphics.fill(LEFT_X + PAGE_W + 2, y, LEFT_X + PAGE_W + 3, bottom + 1, RULE);
        widget(graphics, LEFT_X + PAGE_W / 2 - JOINT / 2, y - JOINT / 2, JOINT_U, JOINT_V, JOINT, JOINT);

        int textX = LEFT_X + 1;
        int textY = y + 4;
        int textW = PAGE_W - 2;
        int lines = (bottom - textY) / LINE;
        for (int index = 0; index < lines; index++) {
            int ruleY = textY + index * LINE + LINE - 1;
            graphics.fill(LEFT_X, ruleY, LEFT_X + PAGE_W, ruleY + 1, 0x33B8A27A);
        }

        boolean active = editing(Editor.RECIPE_NOTES, recipe.id());
        String text = active ? nameField.getValue() : recipe.notes();
        if (active) markEditor(LEFT_X - 3, y, PAGE_W + 6, bottom - y);
        if (text.isEmpty() && !active && !pageMode) {
            noteHint(graphics, textX, textY, textW, lines);
        }
        drawNote(graphics, text, textX, textY, textW, lines, active);
        if (!pageMode && !active) {
            hotspot(LEFT_X - 3, y, PAGE_W + 6, bottom - y,
                    () -> startEdit(Editor.RECIPE_NOTES, recipe.id(), recipe.notes(), PacketGuideAction.MAX_NOTE, textW, lines),
                    () -> List.of(Component.translatable("alchemia.guide.notes.tip")));
        }
    }

    private int recipeTitle(GuiGraphics graphics, BrewRecipe recipe, int y) {
        boolean editing = editing(Editor.TITLE, recipe.id());
        String value = editing ? nameField.getValue() : recipe.displayName().getString();
        float scale = TITLE_SCALES[TITLE_SCALES.length - 1];
        List<int[]> lines = List.of();
        for (float option : TITLE_SCALES) {
            scale = option;
            lines = titleLines(value, scale);
            if (lines.size() <= 2) break;
        }
        float pitch = TITLE_LINE * scale;
        int height = Mth.ceil(lines.size() * pitch);

        int titleX = LEFT_X + (PAGE_W - TITLE_W) / 2;
        int rosetteY = y + Math.round(TITLE_CAP * scale / 2.0F) - ROSETTE / 2;
        widget(graphics, titleX - ROSETTE - 2, rosetteY, ROSETTE_U, ROSETTE_V, ROSETTE, ROSETTE);
        widget(graphics, titleX + TITLE_W + 2, rosetteY, ROSETTE_U, ROSETTE_V, ROSETTE, ROSETTE);

        if (editing) {
            markEditor(LEFT_X, y - 2, PAGE_W, height + 3);
            int cursor = Mth.clamp(nameField.getCursorPosition(), 0, value.length());
            boolean blink = (System.currentTimeMillis() / 400L) % 2L == 0L;
            for (int index = 0; index < lines.size(); index++) {
                int[] line = lines.get(index);
                float lineY = y + index * pitch;
                int ruleY = Math.round(lineY + TITLE_RULE * scale);
                graphics.fill(titleX, ruleY, titleX + TITLE_W, ruleY + 1, INK_FAINT);
                String text = value.substring(line[0], line[1]);
                float lineX = LEFT_X + (PAGE_W - letteredWidth(text) * scale) / 2.0F;
                if (value.isEmpty()) {
                    String hint = Component.translatable("alchemia.guide.rename.hint").getString();
                    lettered(graphics, hint, LEFT_X + (PAGE_W - letteredWidth(hint) * scale) / 2.0F, lineY, scale, INK_FAINT);
                }
                inked(graphics, text, lineX, lineY, scale, INK, index == 0);
                boolean here = cursor >= line[0] && (index == lines.size() - 1 || cursor < lines.get(index + 1)[0]);
                if (blink && here) {
                    int caret = Math.round(lineX + letteredWidth(value.substring(line[0], cursor)) * scale);
                    graphics.fill(caret, Math.round(lineY - scale), caret + 1, Math.round(lineY + TITLE_RULE * scale) - 1, INK);
                }
            }
            return y + height + 2;
        }

        int titleW = 0;
        for (int[] line : lines) titleW = Math.max(titleW, Mth.ceil(letteredWidth(value.substring(line[0], line[1])) * scale));
        boolean editable = !pageMode;
        boolean over = editable && hovered(LEFT_X + (PAGE_W - titleW) / 2 - 2, y - 2, titleW + 4, height + 2);
        for (int index = 0; index < lines.size(); index++) {
            String text = value.substring(lines.get(index)[0], lines.get(index)[1]);
            inked(graphics, text, LEFT_X + (PAGE_W - letteredWidth(text) * scale) / 2.0F, y + index * pitch, scale, over ? INK_LINK : INK, index == 0);
        }
        if (editable) {
            hotspot(LEFT_X + (PAGE_W - titleW) / 2 - 2, y - 2, titleW + 4, height + 2,
                    () -> startEdit(Editor.TITLE, recipe.id(), recipe.name(), PacketNameBrew.MAX_LENGTH, 0, 0),
                    () -> List.of(Component.translatable("alchemia.guide.rename.tip")));
        }
        return y + height;
    }

    private List<int[]> titleLines(String text, float scale) {
        return titleLines(text, scale, TITLE_W);
    }

    private List<int[]> titleLines(String text, float scale, int width) {
        int limit = (int) (width / scale);
        List<int[]> lines = new ArrayList<>();
        int start = 0;
        while (true) {
            int reach = start;
            while (reach < text.length() && letteredWidth(text.substring(start, reach + 1)) <= limit) reach++;
            if (reach >= text.length()) {
                lines.add(new int[]{start, text.length()});
                return lines;
            }
            int space = text.lastIndexOf(' ', reach);
            if (space > start) {
                lines.add(new int[]{start, space});
                start = space + 1;
            } else {
                int cut = Math.max(reach, start + 1);
                lines.add(new int[]{start, cut});
                start = cut;
            }
        }
    }

    private void lettered(GuiGraphics graphics, String text, float x, float capTop, float scale, int colour) {
        if (text.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x, capTop, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, Lettering.of(text), 0, Lettering.RISE, colour, false);
        graphics.pose().popPose();
    }

    private int letteredWidth(String text) {
        return text.isEmpty() ? 0 : this.font.width(Lettering.of(text));
    }

    private static boolean splash(BrewRecipe recipe) {
        return recipe.steps().contains(String.valueOf(BrewRecipe.STEP_SPLASH));
    }

    private static TextureAtlasSprite effectSprite(PotionEnum potion) {
        return mobSprite(potion.getEffect());
    }

    private void followEffect(PotionEnum potion) {
        if (!discovered.contains(potion)) send(PacketGuideAction.Action.LEARN_EFFECT, potion.name());
        selectedEffect = potion;
        goTo(Chapter.EFFECTS);
    }

    private void followIngredient(Ingredients ingredient) {
        if (!knownIngredients.contains(ingredient)) send(PacketGuideAction.Action.LEARN_INGREDIENT, ingredient.name());
        selectedIngredient = ingredient;
        selectedReagent = null;
        goTo(Chapter.INGREDIENTS);
        reagentPage = ingredient.getType() == IngredientType.Mineral;
        reagentSpread = 0;
    }

    private void followReagent(Item item) {
        if (!knownReagents.contains(item)) send(PacketGuideAction.Action.LEARN_INGREDIENT, reagentKey(item));
        selectedReagent = item;
        selectedIngredient = null;
        goTo(Chapter.INGREDIENTS);
        reagentPage = true;
        reagentSpread = VanillaIngredients.MODIFIERS.contains(item) || Imbuements.isAttuner(new ItemStack(item)) ? 1 : 0;
    }

    private @Nullable Runnable followItem(ItemStack item) {
        if (item.getItem() instanceof ItemIngredient ingredient) return () -> followIngredient(ingredient.getIngredient());
        if (isReagent(item.getItem())) return () -> followReagent(item.getItem());
        return null;
    }

    private static boolean isReagent(Item item) {
        return IngredientDiscovery.isReagent(new ItemStack(item));
    }

    private static List<Item> sortedReagents(boolean lures) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<Item, VanillaIngredients.Drift> entry : VanillaIngredients.drifts().entrySet()) {
            if (entry.getValue().lure() == lures) items.add(entry.getKey());
        }
        items.sort(Comparator.comparing(item -> new ItemStack(item).getHoverName().getString()));
        return items;
    }

    private static List<Item> directReagents() {
        return sortedReagents(false);
    }

    private static List<Item> lureReagents() {
        return sortedReagents(true);
    }

    private static List<Item> generalReagents() {
        return directReagents();
    }

    private static String reagentKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private void scrollPanel(GuiGraphics graphics, int from, int to, Supplier<Integer> body) {
        viewTop = from;
        viewBottom = to;
        int maxScroll = Math.max(0, contentHeight - (to - from));
        scroll = Mth.clamp(scroll, 0, maxScroll);

        scissor(graphics, RIGHT_X - 2, from, RIGHT_X + PAGE_W + 2, to);
        int end = body.get();
        graphics.disableScissor();
        contentHeight = end + (int) scroll - from;

        maxScroll = Math.max(0, contentHeight - (to - from));
        if (maxScroll > 0) extractScrollbar(graphics, from, to, maxScroll);
    }

    private int scrollX() {
        return RIGHT_X + PAGE_W - TRACK_W + 4;
    }

    private void extractScrollbar(GuiGraphics graphics, int from, int to, int maxScroll) {
        int x = scrollX();
        int height = to - from;
        widget(graphics, x, from, TRACK_U, TRACK_V, TRACK_W, SCROLL_CAP);
        for (int y = from + SCROLL_CAP; y < to - SCROLL_CAP; y += TRACK_H - SCROLL_CAP * 2) {
            widget(graphics, x, y, TRACK_U, TRACK_V + SCROLL_CAP, TRACK_W, Math.min(TRACK_H - SCROLL_CAP * 2, to - SCROLL_CAP - y));
        }
        widget(graphics, x, to - SCROLL_CAP, TRACK_U, TRACK_V + TRACK_H - SCROLL_CAP, TRACK_W, SCROLL_CAP);

        int thumbH = thumbHeight(height);
        int thumbY = from + (int) Math.round((height - thumbH) * scroll / maxScroll);
        boolean over = dragging || hovered(x, thumbY, TRACK_W, thumbH);
        int u = THUMB_U + (over ? 9 : 0);
        widget(graphics, x, thumbY, u, TRACK_V, TRACK_W, THUMB_CAP);
        for (int y = thumbY + THUMB_CAP; y < thumbY + thumbH - THUMB_CAP; y += 2) {
            widget(graphics, x, y, u, TRACK_V + THUMB_FILL_V, TRACK_W, Math.min(2, thumbY + thumbH - THUMB_CAP - y));
        }
        widget(graphics, x, thumbY + thumbH - THUMB_CAP, u, TRACK_V + THUMB_H - THUMB_CAP, TRACK_W, THUMB_CAP);
        widget(graphics, x, thumbY + (thumbH - GRIP_H) / 2, u, TRACK_V + GRIP_V, TRACK_W, GRIP_H);
    }

    private int thumbHeight(int height) {
        if (contentHeight <= 0) return height;
        return Mth.clamp(height * height / contentHeight, THUMB_H, height);
    }

    private int recipeMethod(GuiGraphics graphics, BrewRecipe recipe) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.method"), y, width);
        List<Step> steps = steps(recipe);
        int gutter = Math.max(15, this.font.width(steps.size() + "."));
        int number = 1;
        for (Step step : steps) {
            Component numeral = Component.literal(number + ".");
            text(graphics, numeral, RIGHT_X + gutter - this.font.width(numeral), y + 4, INK_RED);
            int iconX = RIGHT_X + gutter + 4;
            if (!step.item().isEmpty()) {
                GuideInk.item(graphics, step.item(), iconX - 2, y, GuideInk.PLAIN);
            } else if (step.icon() != null) {
                icon(graphics, step.icon(), iconX, y + 2, false);
            }
            int textX = iconX + 16;
            List<FormattedCharSequence> lines = this.font.split(step.text(), width - (textX - RIGHT_X));
            int rowH = Math.max(STEP_ROW, lines.size() * LINE + 6);
            int lineY = y + (rowH - lines.size() * LINE) / 2 + 1;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(this.font, line, textX, lineY, INK, false);
                lineY += LINE;
            }
            boolean linked = step.action() != null;
            if (linked && hovered(RIGHT_X - 2, y, width + 4, rowH) && inView()) wash(graphics, RIGHT_X - 2, y, width + 4, rowH, false);
            if (!step.tooltip().isEmpty() || linked) clippedHotspot(RIGHT_X - 2, y, width + 4, rowH, step.action(), step::tooltip);
            y += rowH;
            number++;
        }

        if (recipe.requiresGrinding() || recipe.requiresTrimming() || recipe.requiresTurning()) {
            y = scrolledHeading(graphics, Component.translatable("alchemia.guide.tools"), y + 4, width);
            y = requirement(graphics, recipe.requiresGrinding(), "alchemia.guide.needs_mortar", "mortar_and_pestle", y, width,
                    held -> held.getItem() instanceof ItemMortarPestle);
            y = requirement(graphics, recipe.requiresTrimming(), "alchemia.guide.needs_nigredo", "nigredo", y, width,
                    held -> held.getItem() instanceof ItemNigredo);
            y = requirement(graphics, recipe.requiresTurning(), "alchemia.guide.needs_turning", "albedo", y, width,
                    held -> held.getItem() instanceof ItemAlbedo || held.getItem() instanceof ItemCitrinitas);
        }
        return y + 4;
    }

    private boolean inView() {
        return bookMouseY >= viewTop && bookMouseY < viewBottom;
    }

    private int banner(GuiGraphics graphics, String name, int y, int width, Slot slot) {
        float centreX = RIGHT_X + width / 2.0F;
        for (int[] line : titleLines(name, EFFECT_TITLE_SCALE, width - 4)) {
            initialled(graphics, name.substring(line[0], line[1]), centreX, y + 2, EFFECT_TITLE_SCALE);
            y += Math.round(TITLE_LINE * EFFECT_TITLE_SCALE);
        }

        int frameX = RIGHT_X + (width - FRAME) / 2;
        int frameY = y + 2;
        int reach = (Math.min(width - 6, DIVIDER_W) - FRAME) / 2;
        int ruleY = frameY + FRAME_RULE - DIVIDER_H / 2;
        widget(graphics, frameX - reach, ruleY, DIVIDER_U + DIVIDER_W / 2 - FRAME / 2 - reach, DIVIDER_V, reach, DIVIDER_H);
        widget(graphics, frameX + FRAME, ruleY, DIVIDER_U + DIVIDER_W / 2 + FRAME / 2, DIVIDER_V, reach, DIVIDER_H);
        widget(graphics, frameX, frameY, FRAME_U, FRAME_V, FRAME, FRAME);
        slot.draw(frameX + FRAME_INSET, frameY + FRAME_INSET);
        return frameY + FRAME + 6;
    }

    private interface Slot {
        void draw(int x, int y);
    }

    private int scrolledHeading(GuiGraphics graphics, Component title, int y, int width) {
        return scrolledHeading(graphics, title, RIGHT_X, y, width);
    }

    private int scrolledHeading(GuiGraphics graphics, Component title, int x, int y, int width) {
        int ruleY = subtitle(graphics, title, x, y, width);
        int w = Math.min(width - 6, DIVIDER_W);
        widget(graphics, x + (width - w) / 2, ruleY, DIVIDER_U + (DIVIDER_W - w) / 2, DIVIDER_V, w, DIVIDER_H);
        return ruleY + DIVIDER_H + 4;
    }

    private int subtitle(GuiGraphics graphics, Component title, int y, int width) {
        return subtitle(graphics, title, RIGHT_X, y, width);
    }

    private int subtitle(GuiGraphics graphics, Component title, int x, int y, int width) {
        initialled(graphics, title.getString(), x + width / 2.0F, y, SUBTITLE_SCALE);
        return y + Math.round(TITLE_RULE * SUBTITLE_SCALE);
    }

    private int requirement(GuiGraphics graphics, boolean needed, String key, String toolId, int y, int width,
                            Predicate<ItemStack> tool) {
        if (!needed) return y;
        boolean carried = this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.getInventory().items.stream().anyMatch(tool);
        ItemStack sprite = new ItemStack(ItemRegistry.getItemFromRegistry(toolId));
        GuideInk.item(graphics, sprite, RIGHT_X, y, carried ? GuideInk.PLAIN : GuideInk.SHORT);
        List<FormattedCharSequence> lines = this.font.split(Component.translatable(key), width - 20);
        int textY = y + Math.max(0, (STEP_ROW - lines.size() * LINE) / 2) + 1;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, RIGHT_X + 20, textY, carried ? INK : INK_RED, false);
            textY += LINE;
        }
        clippedHotspot(RIGHT_X - 2, y, width + 4, Math.max(STEP_ROW, lines.size() * LINE + 4), null, () -> List.of(
                Component.translatable(carried ? "alchemia.guide.tool.carried" : "alchemia.guide.tool.missing")));
        return y + Math.max(STEP_ROW, lines.size() * LINE + 4);
    }

    private List<Step> steps(BrewRecipe recipe) {
        List<Step> steps = new ArrayList<>();
        for (String step : recipe.steps()) {
            if (step.isEmpty()) continue;
            char kind = step.charAt(0);
            String value = step.substring(1);
            switch (kind) {
                case BrewRecipe.STEP_ADD -> {
                    ItemStack item = BrewRecipe.stackFor(value);
                    if (item.isEmpty()) continue;
                    Component treatment = treatment(item);
                    Component text = treatment == null
                            ? Component.translatable("alchemia.guide.step.add", item.getHoverName())
                            : Component.translatable("alchemia.guide.step.add_treated", item.getHoverName(), treatment);
                    steps.add(new Step(text, item, null, List.of(item.getHoverName()), pageMode ? null : followItem(item)));
                }
                case BrewRecipe.STEP_STIR -> steps.add(stirStep(stirs(value), 1.0));
                case BrewRecipe.STEP_FINE -> steps.add(stirStep(stirs(value), FINE_STIR));
                case BrewRecipe.STEP_FINISH -> steps.add(new Step(Component.translatable("alchemia.guide.step.settle"), ItemStack.EMPTY, Icon.STIR, List.of()));
                case BrewRecipe.STEP_BREW -> steps.add(new Step(Component.translatable("alchemia.guide.step.brew"), ItemStack.EMPTY, Icon.BREW,
                        List.of(Component.translatable("alchemia.guide.step.brew.tip"))));
                case BrewRecipe.STEP_REDSTONE -> steps.add(new Step(Component.translatable("alchemia.guide.step.redstone"),
                        new ItemStack(Items.REDSTONE), null, List.of(Component.translatable("alchemia.guide.step.redstone.tip")),
                        pageMode ? null : () -> followReagent(Items.REDSTONE)));
                case BrewRecipe.STEP_BARK -> {
                    Imbuements.Attuner attuner = Imbuements.parse(step);
                    if (attuner == null) continue;
                    ItemStack shown = attuner.stack();
                    steps.add(new Step(Component.translatable("alchemia.guide.step.attune", shown.getHoverName()), shown, null,
                            List.of(Component.translatable("alchemia.guide.reagent.attunes", attuner.region()),
                                    Component.translatable("alchemia.guide.step.bark.tip")),
                            pageMode ? null : () -> followReagent(shown.getItem())));
                }
                case BrewRecipe.STEP_GLOW -> steps.add(new Step(Component.translatable("alchemia.guide.step.glow"),
                        new ItemStack(Items.GLOWSTONE_DUST), null, List.of(Component.translatable("alchemia.guide.step.glow.tip")),
                        pageMode ? null : () -> followReagent(Items.GLOWSTONE_DUST)));
                case BrewRecipe.STEP_BOTTLE -> {
                    muddykat.alchemia.common.potion.PotionGlass glass = muddykat.alchemia.common.potion.PotionGlass.byName(value);
                    if (!glass.hasEmptyItem()) continue;
                    ItemStack vessel = new ItemStack(muddykat.alchemia.common.potion.PotionGlasses.emptyItem(glass));
                    steps.add(new Step(Component.translatable("alchemia.guide.step.bottle", vessel.getHoverName()), vessel, null,
                            List.of(vessel.getHoverName())));
                }
                case BrewRecipe.STEP_SPLASH -> steps.add(new Step(Component.translatable("alchemia.guide.step.splash"),
                        new ItemStack(Items.GUNPOWDER), null, List.of(Component.translatable("alchemia.guide.step.splash.tip")),
                        pageMode ? null : () -> followReagent(Items.GUNPOWDER)));
                default -> {
                }
            }
        }
        return steps;
    }

    private static int stirs(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private Step stirStep(int count, double weight) {
        double seconds = count * weight / STIRS_PER_SECOND;
        String shown = seconds < 0.1 ? "0.1" : seconds % 1.0 < 0.05 ? String.valueOf(Math.round(seconds)) : String.format("%.1f", seconds);
        Component text = Component.translatable(shown.equals("1") ? "alchemia.guide.step.stir_one" : "alchemia.guide.step.stir", shown);
        List<Component> tooltip = List.of(
                Component.translatable("alchemia.guide.step.stir.count", count),
                Component.translatable("alchemia.guide.step.stir.tip"));
        return new Step(text, ItemStack.EMPTY, Icon.STIR, tooltip);
    }

    private static @Nullable Component treatment(ItemStack item) {
        List<Component> parts = new ArrayList<>();
        int crush = ItemIngredient.crushOf(item);
        if (crush > 0) parts.add(Component.translatable("alchemia.guide.step.ground", Math.round(100.0F * crush / Ingredients.MAX_CRUSH_UNITS)));
        if (ItemIngredient.trimOf(item) > 0) parts.add(Component.translatable("alchemia.guide.step.trimmed"));
        int rotation = ItemIngredient.rotationOf(item);
        if (rotation > 0) parts.add(Component.translatable("alchemia.guide.step.turned", rotation * 45));
        if (parts.isEmpty()) return null;

        Component joined = parts.get(0);
        for (int index = 1; index < parts.size(); index++) {
            joined = Component.translatable("alchemia.guide.step.join", joined, parts.get(index));
        }
        return joined;
    }

    private void extractRecipeBar(GuiGraphics graphics, BrewRecipe recipe, int y) {
        if (pageMode) {
            wrappedCentred(graphics, Component.translatable("alchemia.page.brew_hint"), RIGHT_X, y, INK_FAINT);
            return;
        }

        boolean prepared = recipe.id().equals(selected);
        boolean canPrepare = canPrepare();
        int prepareW = PREPARE_W;
        int barX = RIGHT_X + (PAGE_W - (PREPARE_W + (SMALL + 3) * 2)) / 2;
        boolean overPrepare = canPrepare && hovered(barX, y, prepareW, BUTTON_H);
        button(graphics, barX, y, prepareW, canPrepare ? (overPrepare ? 1 : 0) : 2);
        Component label = Component.translatable(prepared ? "alchemia.guide.prepared" : "alchemia.guide.prepare");
        int labelW = this.font.width(label) + (prepared ? ICON + 3 : 0);
        int labelX = barX + (prepareW - labelW) / 2;
        if (prepared) {
            icon(graphics, Icon.SEAL, labelX, y + 2, !canPrepare);
            labelX += ICON + 3;
        }
        text(graphics, label, labelX, y + 4, canPrepare ? INK_HEAD : INK_FAINT);
        hotspot(barX, y, prepareW, BUTTON_H, canPrepare ? () -> prepare(recipe) : null, () -> List.of(Component.translatable(
                !canPrepare ? "alchemia.guide.prepare.tome" : source.block() ? "alchemia.guide.prepare.desk" : "alchemia.guide.prepare.tip")));

        int copyX = barX + prepareW + 3;
        boolean overCopy = hovered(copyX, y, SMALL, SMALL);
        smallButton(graphics, copyX, y, overCopy ? 1 : 0, Icon.COPY);
        hotspot(copyX, y, SMALL, SMALL, () -> {
            send(PacketGuideAction.Action.COPY, recipe.id());
            turnPage();
        }, () -> List.of(Component.translatable("alchemia.guide.copy"), Component.translatable("alchemia.guide.copy.tip")));

        int deleteX = copyX + SMALL + 3;
        boolean armed = recipe.id().equals(pendingDelete);
        boolean overDelete = hovered(deleteX, y, SMALL, SMALL);
        smallButton(graphics, deleteX, y, overDelete || armed ? 1 : 0, Icon.DELETE);
        if (armed) wash(graphics, deleteX + 1, y + 1, SMALL - 2, SMALL - 2, true);
        hotspot(deleteX, y, SMALL, SMALL, () -> delete(recipe), () -> List.of(Component.translatable(armed
                ? "alchemia.guide.delete.confirm" : "alchemia.guide.delete.tip")));
    }

    private boolean canPrepare() {
        return !pageMode && (!source.block() || "alchemia.guide.desk_hint".equals(deskHint()));
    }

    private static ItemStack tabletIcon() {
        return new ItemStack(ItemRegistry.getItemFromRegistry(ItemAlchemyTablet.ID));
    }

    private ItemStack healingPotion() {
        ItemStack potion = new ItemStack(Items.POTION);
        PotionUtils.setPotion(potion, net.minecraft.world.item.alchemy.Potions.HEALING);
        return potion;
    }

    private static void glowing(GuiGraphics graphics, float centreX, float centreY, float art) {
        float scale = art / GLOWING_ART;
        graphics.pose().pushPose();
        graphics.pose().translate(centreX - GLOWING_CENTRE * scale, centreY - GLOWING_CENTRE * scale, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        sprite(graphics, glowing(), 0, 0, GLOWING_SIZE, GLOWING_SIZE, -1);
        graphics.pose().popPose();
    }

    private static TextureAtlasSprite glowing() {
        return mobSprite(net.minecraft.world.effect.MobEffects.GLOWING);
    }

    private static TextureAtlasSprite mobSprite(MobEffect effect) {
        return Minecraft.getInstance().getMobEffectTextures().get(effect);
    }

    private static void sprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int w, int h, int argb) {
        tinted(graphics, argb, () -> graphics.blit(x, y, 0, w, h, sprite));
    }

    private static void sprite(GuiGraphics graphics, AlchemiaSprites.Sprite sprite, int x, int y, int w, int h, int argb) {
        sprite.blit(graphics, x, y, w, h, argb);
    }

    private static void tinted(GuiGraphics graphics, int argb, Runnable draw) {
        RenderSystem.enableBlend();
        graphics.setColor((argb >> 16 & 255) / 255.0F, (argb >> 8 & 255) / 255.0F, (argb & 255) / 255.0F, (argb >>> 24) / 255.0F);
        draw.run();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void scissor(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
        Matrix4f pose = graphics.pose().last().pose();
        Vector4f from = pose.transform(new Vector4f(x0, y0, 0.0F, 1.0F));
        Vector4f to = pose.transform(new Vector4f(x1, y1, 0.0F, 1.0F));
        graphics.enableScissor(Mth.floor(from.x()), Mth.floor(from.y()), Mth.ceil(to.x()), Mth.ceil(to.y()));
    }

    private void prepare(BrewRecipe recipe) {
        String next = recipe.id().equals(selected) ? "" : recipe.id();
        BrewRecipeBook.select(stack, next.isEmpty() ? null : next);
        selected = next.isEmpty() ? null : next;
        send(PacketGuideAction.Action.SELECT, next);
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(next.isEmpty()
                    ? SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F)
                    : SimpleSoundInstance.forUI(SoundRegistry.GUIDE_STAMP.get(), 0.95F + (float) Math.random() * 0.1F, 0.9F));
        }
    }

    private void delete(BrewRecipe recipe) {
        if (!recipe.id().equals(pendingDelete)) {
            pendingDelete = recipe.id();
            pendingDeleteTicks = DELETE_CONFIRM_TICKS;
            return;
        }
        pendingDelete = null;
        BrewRecipeBook.forget(stack, recipe.id());
        send(PacketGuideAction.Action.FORGET, recipe.id());
        openRecipe = null;
        refresh();
        if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PUT, 0.8F));
    }

    private void extractEffects(GuiGraphics graphics) {
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.chapter.effects"), LEFT_X, TOP + 2);
        centred(graphics, Component.translatable("alchemia.guide.effects.known", discovered.size(), RecipeDiscovery.total()), LEFT_X, y, INK_FAINT);

        List<PotionEnum> known = new ArrayList<>();
        List<PotionEnum> noted = new ArrayList<>();
        for (PotionEnum potion : PotionEnum.values()) {
            if (discovered.contains(potion)) known.add(potion);
            else if (WorldEffects.anyVisible(notes, potion)) noted.add(potion);
        }
        known.sort(Comparator.comparing(potion -> potion.getEffect().getDisplayName().getString()));

        int gridX = LEFT_X + (PAGE_W - GRID_COLUMNS * CELL) / 2;
        int gridY = y + 14;
        int total = RecipeDiscovery.total();
        for (int slot = 0; slot < Math.min(total, GRID_COLUMNS * EFFECTS_ROWS); slot++) {
            int x = gridX + (slot % GRID_COLUMNS) * CELL;
            int cy = gridY + (slot / GRID_COLUMNS) * CELL;
            PotionEnum potion = slot < known.size() ? known.get(slot)
                    : slot < known.size() + noted.size() ? noted.get(slot - known.size()) : null;
            boolean isKnown = slot < known.size();
            boolean over = potion != null && hovered(x, cy, CELL, CELL);
            cell(graphics, x, cy, potion == null ? 3 : potion == selectedEffect ? 2 : over ? 1 : 0);

            if (potion == null) {
                icon(graphics, Icon.UNKNOWN, x + 3, cy + 3, true);
                hotspot(x, cy, CELL, CELL, null, () -> List.of(Component.translatable("alchemia.guide.effects.undiscovered")));
                continue;
            }
            if (isKnown) {
                sprite(graphics, effectSprite(potion), x + 1, cy + 1, 16, 16, -1);
            } else {
                icon(graphics, Icon.EYE_CLOSED, x + 3, cy + 3, false);
            }
            PotionEnum chosen = potion;
            hotspot(x, cy, CELL, CELL, () -> {
                selectedEffect = chosen;
                resetScroll();
            }, () -> List.of(effectName(chosen)));
        }

        int legendY = gridY + EFFECTS_ROWS * CELL + 8;
        wrappedCentred(graphics, Component.translatable("alchemia.guide.effects.legend"), LEFT_X, legendY, INK_FAINT);

        scrollPanel(graphics, TOP, BOTTOM, () -> effectDetail(graphics));
    }

    private Component effectName(PotionEnum potion) {
        return discovered.contains(potion) ? potion.getEffect().getDisplayName()
                : Component.translatable("alchemia.guide.effect.unknown");
    }

    private static Component alignmentName(IngredientAlignment alignment) {
        return Component.translatable("alchemia.alignment." + alignment.name().toLowerCase());
    }

    private int effectDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll;
        if (selectedEffect == null) {
            return wrapped(graphics, Component.translatable("alchemia.guide.effects.select"), RIGHT_X, y + 30, width, INK_FAINT);
        }

        boolean known = discovered.contains(selectedEffect);
        PotionEnum shown = selectedEffect;
        y = banner(graphics, effectName(shown).getString(), y, width, (x, iy) -> {
            if (known) {
                sprite(graphics, effectSprite(shown), x, iy, 18, 18, -1);
            } else {
                icon(graphics, Icon.EYE_CLOSED, x + 3, iy + 3, false);
            }
        });

        if (known) {
            y = wrapped(graphics, Component.translatable("alchemia.guide.effect.rarity",
                    Component.translatable("alchemia.guide.rarity." + selectedEffect.getRarity().name().toLowerCase())), RIGHT_X, y, width, INK_SOFT);
            y = wrapped(graphics, Component.translatable("alchemia.guide.effect.alignment",
                    alignmentName(selectedEffect.getPrimaryAlignment()), alignmentName(selectedEffect.getSecondaryAlignment())),
                    RIGHT_X, y + 2, width, INK_SOFT) + 4;
        } else {
            y = wrapped(graphics, Component.translatable("alchemia.guide.effect.unbrewed"), RIGHT_X, y, width, INK_FAINT) + 4;
        }

        List<Component> lines = WorldEffects.guideLines(notes, selectedEffect);
        if (!lines.isEmpty()) {
            y = scrolledHeading(graphics, Component.translatable("alchemia.guide.effect.conditions"), y + 2, width);
            for (Component line : lines) {
                List<FormattedCharSequence> parts = this.font.split(line, width - SEAL - 4);
                int rowH = Math.max(SEAL + 2, parts.size() * LINE + 4);
                widget(graphics, RIGHT_X, y + (rowH - SEAL) / 2, EMPTY_SEAL_U, PREPARED_V, SEAL, SEAL);
                icon(graphics, Icon.EYE, RIGHT_X + 3, y + (rowH - SEAL) / 2 + 3, false);
                int textY = y + (rowH - parts.size() * LINE) / 2 + 1;
                for (FormattedCharSequence part : parts) {
                    graphics.drawString(this.font, part, RIGHT_X + SEAL + 4, textY, INK, false);
                    textY += LINE;
                }
                y += rowH;
            }
        }

        List<BrewRecipe> using = new ArrayList<>();
        for (BrewRecipe recipe : recipes) {
            for (BrewRecipe.BrewEffect effect : recipe.effects()) {
                if (effect.recipe().equals(selectedEffect.name())) {
                    using.add(recipe);
                    break;
                }
            }
        }
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.effect.recipes"), y + 4, width);
        if (using.isEmpty()) return wrapped(graphics, Component.translatable("alchemia.guide.effect.no_recipes"), RIGHT_X, y, width, INK_FAINT);
        return linkedRecipes(graphics, using, y, width);
    }

    private int linkedRecipes(GuiGraphics graphics, List<BrewRecipe> using, int y, int width) {
        for (BrewRecipe recipe : using) {
            boolean over = hovered(RIGHT_X - 2, y - 1, width + 4, STEP_ROW) && inView();
            if (over) wash(graphics, RIGHT_X - 2, y - 1, width + 4, STEP_ROW, false);
            graphics.renderItem(potionFor(recipe), RIGHT_X, y);
            text(graphics, Component.literal(clip(recipe.displayName(), width - 22)), RIGHT_X + 20, y + 4, over ? INK_LINK : INK);
            clippedHotspot(RIGHT_X - 2, y - 1, width + 4, STEP_ROW, () -> openRecipe(recipe), () -> List.of(recipe.displayName()));
            y += STEP_ROW;
        }
        return y;
    }

    private static ItemStack ingredientStack(Ingredients ingredient) {
        return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(Alchemia.MODID, ingredient.getRegistryName())));
    }

    private void extractOpus(GuiGraphics graphics) {
        if (machinePage) {
            extractMachine(graphics);
            return;
        }
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.chapter.opus"), LEFT_X, TOP + 2);
        centred(graphics, Component.translatable("alchemia.guide.opus.known", greatWork.size(), GreatWork.total()), LEFT_X, y, INK_FAINT);

        List<MagnumOpus.Stage> stages = MagnumOpus.stages();
        if (selectedStage == null) {
            for (MagnumOpus.Stage stage : stages) {
                if (greatWork.contains(stage.result())) selectedStage = stage.result();
            }
        }
        int cellX = LEFT_X + STAGE_INDENT;
        int rowY = y + 14;
        for (int index = 0; index < stages.size(); index++) {
            MagnumOpus.Stage stage = stages.get(index);
            boolean known = greatWork.contains(stage.result());
            boolean chosen = stage.result().equals(selectedStage);
            int top = rowY + index * STAGE_ROW;
            boolean over = hovered(LEFT_X - 2, top - 2, PAGE_W + 4, STAGE_ROW - 2);

            if (index + 1 < stages.size()) {
                boolean bound = known && greatWork.contains(stages.get(index + 1).result());
                int lineX = cellX + CELL / 2;
                for (int lineY = top + CELL; lineY < top + STAGE_ROW; lineY++) {
                    if (bound || (lineY - top) % 2 == 0) graphics.fill(lineX, lineY, lineX + 1, lineY + 1, bound ? INK_SOFT : INK_FAINT);
                }
            }

            if (over || chosen) wash(graphics, LEFT_X - 2, top - 2, PAGE_W + 4, STAGE_ROW - 2, chosen);
            String numeral = Component.translatable("enchantment.level." + (index + 1)).getString();
            float numeralX = cellX - 3 - letteredWidth(numeral) * SUBTITLE_SCALE;
            float numeralY = top + (CELL - TITLE_CAP * SUBTITLE_SCALE) / 2.0F;
            inked(graphics, numeral, numeralX, numeralY, SUBTITLE_SCALE, known ? INK_RED : INK_FAINT, false);
            cell(graphics, cellX, top, known ? chosen ? 2 : over ? 1 : 0 : 3);
            if (known) {
                graphics.renderItem(GreatWork.result(stage), cellX + 1, top + 1);
                text(graphics, GreatWork.result(stage).getHoverName(), cellX + CELL + 6, top, over ? INK_LINK : INK_HEAD);
                text(graphics, Component.translatable("alchemia.opus." + stage.result() + ".epithet"), cellX + CELL + 6, top + 10, INK_FAINT);
            } else {
                icon(graphics, Icon.UNKNOWN, cellX + 3, top + 3, true);
                text(graphics, Component.translatable("alchemia.guide.opus.unrevealed"), cellX + CELL + 6, top, INK_FAINT);
                text(graphics, Component.translatable("alchemia.guide.opus.buried"), cellX + CELL + 6, top + 10, INK_FAINT);
            }
            hotspot(LEFT_X - 2, top - 2, PAGE_W + 4, STAGE_ROW - 2, () -> {
                selectedStage = stage.result();
                resetScroll();
            });
        }

        Component legend = Component.translatable("alchemia.guide.opus.legend");
        int lines = this.font.split(legend, PAGE_W - 8).size();
        wrappedCentred(graphics, legend, LEFT_X, BOTTOM - lines * LINE, INK_FAINT);

        corner(graphics, true, () -> {
            machinePage = true;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.machine.title"));
        scrollPanel(graphics, TOP, BOTTOM - 20, () -> opusDetail(graphics));
    }

    private int opusDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll;
        MagnumOpus.Stage stage = selectedStage == null ? null : GreatWork.byName(selectedStage);
        if (stage == null) {
            String key = greatWork.isEmpty() ? "alchemia.guide.opus.none" : "alchemia.guide.opus.select";
            return wrapped(graphics, Component.translatable(key), RIGHT_X, y + 30, width, INK_FAINT);
        }

        if (!greatWork.contains(stage.result())) {
            y = banner(graphics, Component.translatable("alchemia.guide.opus.unrevealed").getString(), y, width,
                    (x, iy) -> icon(graphics, Icon.UNKNOWN, x + 3, iy + 3, true));
            return wrapped(graphics, Component.translatable("alchemia.guide.opus.locked"), RIGHT_X, y, width, INK_FAINT);
        }

        ItemStack result = GreatWork.result(stage);
        y = banner(graphics, result.getHoverName().getString(), y, width, (x, iy) -> graphics.renderItem(result, x + 1, iy + 1));
        y = wrapped(graphics, Component.translatable("alchemia.opus." + stage.result() + ".lore"), RIGHT_X, y, width, INK_SOFT) + 4;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.opus.input"), y + 2, width);
        ItemStack input = GreatWork.input(stage);
        if (input.isEmpty()) {
            icon(graphics, Icon.UNKNOWN, RIGHT_X + 2, y + 2, true);
            y = wrapped(graphics, Component.translatable("alchemia.guide.opus.input.empty"), RIGHT_X + 20, y + 4, width - 20, INK) + 6;
        } else {
            MagnumOpus.Stage before = GreatWork.byName(stage.previous());
            boolean over = hovered(RIGHT_X - 2, y - 1, width + 4, STEP_ROW) && inView();
            if (over) wash(graphics, RIGHT_X - 2, y - 1, width + 4, STEP_ROW, false);
            graphics.renderItem(input, RIGHT_X, y);
            text(graphics, input.getHoverName(), RIGHT_X + 20, y + 4, over ? INK_LINK : INK);
            if (before != null) clippedHotspot(RIGHT_X - 2, y - 1, width + 4, STEP_ROW, () -> {
                selectedStage = before.result();
                resetScroll();
            }, () -> List.of(input.getHoverName()));
            y += STEP_ROW + 2;
        }
        y = wrapped(graphics, Component.translatable("alchemia.guide.opus.fuel"), RIGHT_X, y, width, INK_FAINT) + 4;

        y = subtitle(graphics, Component.translatable("alchemia.guide.opus.flasks"), y + 2, width) + 3;
        y = flaskGrid(graphics, stage, y, width);

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.opus.yields"), y + 4, width);
        graphics.renderItem(result, RIGHT_X, y);
        text(graphics, result.getHoverName(), RIGHT_X + 20, y + 4, INK_HEAD);
        return y + STEP_ROW;
    }

    private void extractMachine(GuiGraphics graphics) {
        ItemStack core = MachineBlueprints.CORE.resultStack();
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.machine.title"), LEFT_X, TOP + 2);
        y = wrappedCentred(graphics, Component.translatable("alchemia.guide.machine.lore"), LEFT_X, y, INK_SOFT) + 4;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.machine.assembly"), LEFT_X, y, PAGE_W);
        int centre = LEFT_X + PAGE_W / 2;
        int rowY = y + CELL + 2;
        schematicCell(graphics, UpgradeSide.TOP, centre - CELL / 2, y);
        schematicCell(graphics, UpgradeSide.LEFT, centre - CELL / 2 - CELL - 2, rowY);
        cell(graphics, centre - CELL / 2, rowY, 2);
        graphics.renderItem(core, centre - CELL / 2 + 1, rowY + 1);
        hotspot(centre - CELL / 2, rowY, CELL, CELL, null, () -> List.of(core.getHoverName()));
        schematicCell(graphics, UpgradeSide.RIGHT, centre + CELL / 2 + 2, rowY);
        int frontY = rowY + CELL + 4;
        centred(graphics, Component.translatable("alchemia.guide.machine.front"), LEFT_X, frontY, INK_FAINT);
        wrappedCentred(graphics, Component.translatable("alchemia.guide.machine.attach"), LEFT_X, frontY + LINE + 6, INK_SOFT);

        corner(graphics, false, () -> {
            machinePage = false;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.chapter.opus"));
        scrollPanel(graphics, TOP, BOTTOM, () -> machineRecipes(graphics));
    }

    private int machineRecipes(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll;
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.machine.core"), y + 2, width);
        y = blueprintGrid(graphics, MachineBlueprints.CORE, y, width);
        y = wrapped(graphics, Component.translatable("alchemia.guide.opus.fuel"), RIGHT_X, y, width, INK_FAINT) + 4;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.machine.upgrades"), y + 2, width);
        for (UpgradeSide side : new UpgradeSide[]{UpgradeSide.RIGHT, UpgradeSide.LEFT, UpgradeSide.TOP}) {
            y = wrapped(graphics, Component.translatable("alchemia.guide.machine.upgrade." + side.getSerializedName(), side.potionSlots()),
                    RIGHT_X, y, width, INK) + 2;
            y = blueprintGrid(graphics, MachineBlueprints.upgrade(side), y, width) + 2;
        }
        return y;
    }

    private void schematicCell(GuiGraphics graphics, UpgradeSide side, int x, int y) {
        ItemStack upgrade = MachineBlueprints.upgrade(side).resultStack();
        cell(graphics, x, y, 0);
        graphics.renderItem(upgrade, x + 1, y + 1);
        String slots = String.valueOf(side.potionSlots());
        graphics.drawString(this.font, slots, x + CELL - this.font.width(slots), y + CELL - 8, 0xFFFFFFFF, true);
        hotspot(x, y, CELL, CELL, null, () -> List.of(upgrade.getHoverName(),
                Component.translatable("alchemia.guide.machine.slots", side.potionSlots())));
    }

    private int blueprintGrid(GuiGraphics graphics, MachineBlueprints.Blueprint blueprint, int y, int width) {
        int rows = blueprint.rows().size();
        int columns = blueprint.rows().get(0).length();
        int gridW = columns * CELL;
        int arrowW = 12;
        int total = gridW + 6 + arrowW + 6 + CELL;
        int x0 = RIGHT_X + (width - total) / 2;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int x = x0 + column * CELL;
                int cy = y + row * CELL;
                ItemStack item = blueprint.at(row, column);
                cell(graphics, x, cy, item.isEmpty() ? 3 : 0);
                if (item.isEmpty()) continue;
                graphics.renderItem(item, x + 1, cy + 1);
                clippedHotspot(x, cy, CELL, CELL, null, () -> List.of(item.getHoverName()));
            }
        }
        int arrowX = x0 + gridW + 6;
        int arrowY = y + rows * CELL / 2;
        graphics.fill(arrowX, arrowY, arrowX + arrowW - 3, arrowY + 1, INK_SOFT);
        for (int step = 0; step < 4; step++) {
            graphics.fill(arrowX + arrowW - 4 + step, arrowY - 3 + step, arrowX + arrowW - 3 + step, arrowY + 4 - step, INK_SOFT);
        }
        ItemStack result = blueprint.resultStack();
        int resultX = arrowX + arrowW + 6;
        int resultY = arrowY - CELL / 2;
        cell(graphics, resultX, resultY, 2);
        graphics.renderItem(result, resultX + 1, resultY + 1);
        clippedHotspot(resultX, resultY, CELL, CELL, null, () -> List.of(result.getHoverName()));
        return y + rows * CELL + 4;
    }

    private int flaskGrid(GuiGraphics graphics, MagnumOpus.Stage stage, int y, int width) {
        if (!stage.result().equals(flaskStage)) {
            flaskStage = stage.result();
            flaskFocus = 0;
        }
        int count = stage.potionCount();
        if (count == 0) return y;
        flaskFocus = Mth.clamp(flaskFocus, 0, count - 1);

        int columns = Math.min(count, FLASK_COLUMNS);
        int rows = Mth.ceil(count / (float) columns);
        int x0 = RIGHT_X + (width - columns * CELL) / 2;
        int gridY = y + ICON + 6;
        effectBar(graphics, stage.potions().get(flaskFocus), y, width);
        y = gridY;
        boolean creative = this.minecraft != null && this.minecraft.player != null && this.minecraft.player.getAbilities().instabuild;
        for (int index = 0; index < count; index++) {
            int x = x0 + (index % columns) * CELL;
            int cy = y + (index / columns) * CELL;
            boolean over = hovered(x, cy, CELL, CELL) && inView();
            cell(graphics, x, cy, index == flaskFocus ? 2 : over ? 1 : 0);
            PotionEffectIngredient flask = stage.potions().get(index);
            graphics.renderItem(flaskPotion(flask), x + 1, cy + 1);
            int flaskIndex = index;
            List<Component> names = flaskNames(flask);
            clippedHotspot(x, cy, CELL, CELL, () -> flaskFocus = flaskIndex, () -> names);
            if (creative) {
                clippedRightClick(x, cy, CELL, CELL, () -> send(PacketGuideAction.Action.SUMMON_FLASK, PacketGuideAction.flask(stage.result(), flaskIndex)));
            }
        }
        return y + rows * CELL + 4;
    }

    private int effectBar(GuiGraphics graphics, PotionEffectIngredient flask, int y, int width) {
        List<Component> names = flaskNames(flask);
        List<PotionEffectIngredient.Requirement> levels = new ArrayList<>();
        for (PotionEffectIngredient.Requirement requirement : flask.required()) {
            for (int level = 0; level <= requirement.amplifier(); level++) levels.add(requirement);
        }
        int slots = Math.max(EFFECT_SLOTS, levels.size());
        int rowW = slots * EFFECT_SLOT;
        int x0 = RIGHT_X + (width - rowW) / 2;
        if (rowW + 2 * (VINE_W + 3) <= width) {
            widget(graphics, x0 - VINE_W - 2, y + 2, VINE_U, VINE_V, VINE_W, VINE_H);
            widget(graphics, x0 + rowW + 1, y + 2, VINE_RIGHT_U, VINE_V, VINE_W, VINE_H);
        }
        for (int slot = 0; slot < slots; slot++) {
            int x = x0 + slot * EFFECT_SLOT;
            if (slot >= levels.size()) {
                widget(graphics, x + (EFFECT_SLOT - DIAMOND) / 2, y + (ICON - DIAMOND) / 2 + 1, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND);
                continue;
            }
            PotionEffectIngredient.Requirement requirement = levels.get(slot);
            PotionEnum recipe = RecipeDiscovery.byEffect(requirement.effect());
            boolean known = recipe != null && discovered.contains(recipe);
            int iconX = x + (EFFECT_SLOT - ICON) / 2;
            if (known) {
                sprite(graphics, mobSprite(requirement.effect()), iconX, y, ICON, ICON, -1);
            } else {
                icon(graphics, Icon.UNKNOWN, iconX, y, true);
            }
            Component name = names.get(flask.required().indexOf(requirement));
            clippedHotspot(x, y, EFFECT_SLOT, ICON, known ? () -> followEffect(recipe) : null, () -> List.of(name));
        }
        return y + ICON + 6;
    }

    private List<Component> flaskNames(PotionEffectIngredient flask) {
        List<Component> names = new ArrayList<>();
        for (PotionEffectIngredient.Requirement requirement : flask.required()) {
            PotionEnum known = RecipeDiscovery.byEffect(requirement.effect());
            Component effectName = known != null && discovered.contains(known) ? requirement.effect().getDisplayName()
                    : Component.translatable("alchemia.guide.effect.unknown");
            names.add(Component.translatable("alchemia.guide.opus.flask.effect", effectName,
                    Component.translatable("enchantment.level." + (requirement.amplifier() + 1))));
        }
        return names;
    }

    private static ItemStack flaskPotion(PotionEffectIngredient flask) {
        int red = 0, green = 0, blue = 0;
        for (PotionEffectIngredient.Requirement requirement : flask.required()) {
            int colour = requirement.effect().getColor();
            red += (colour >> 16) & 255;
            green += (colour >> 8) & 255;
            blue += colour & 255;
        }
        int count = Math.max(1, flask.required().size());
        ItemStack potion = new ItemStack(Items.POTION);
        PotionStacks.apply(potion, PotionRegistry.ALCHEMICAL.get(), List.of(), (red / count) << 16 | (green / count) << 8 | blue / count);
        return potion;
    }

    private void extractIngredients(GuiGraphics graphics) {
        if (reagentPage) {
            extractReagents(graphics);
            return;
        }
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.chapter.ingredients"), LEFT_X, TOP + 2);
        int gridX = LEFT_X + (PAGE_W - GRID_COLUMNS * CELL) / 2;
        y = ingredientGroup(graphics, IngredientType.Flower, "alchemia.guide.ingredients.flower", gridX, y);
        y = ingredientGroup(graphics, IngredientType.Herb, "alchemia.guide.ingredients.herb", gridX, y);
        y = ingredientGroup(graphics, IngredientType.Mushroom, "alchemia.guide.ingredients.mushroom", gridX, y);
        ingredientGroup(graphics, IngredientType.Root, "alchemia.guide.ingredients.root", gridX, y);

        corner(graphics, true, () -> {
            reagentPage = true;
            reagentSpread = 0;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.reagents.title"));
        scrollPanel(graphics, TOP, BOTTOM - 20, () -> selectedReagent != null ? reagentDetail(graphics) : ingredientDetail(graphics));
    }

    private int ingredientGroup(GuiGraphics graphics, IngredientType type, String title, int gridX, int y) {
        List<Ingredients> items = ingredientsOf(type);
        centred(graphics, Component.translatable(title, knownIngredients(items), items.size()), LEFT_X, y, INK_HEAD);
        return ingredientGrid(graphics, items, gridX, y + 10) + 1;
    }

    private List<Ingredients> ingredientsOf(IngredientType type) {
        List<Ingredients> items = new ArrayList<>();
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType() == type) items.add(ingredient);
        }
        items.sort(Comparator.comparing(ingredient -> ingredientStack(ingredient).getHoverName().getString()));
        return items;
    }

    private int knownIngredients(List<Ingredients> items) {
        int count = 0;
        for (Ingredients ingredient : items) {
            if (knownIngredients.contains(ingredient)) count++;
        }
        return count;
    }

    private int ingredientGrid(GuiGraphics graphics, List<Ingredients> items, int gridX, int gridY) {
        List<Ingredients> shown = new ArrayList<>();
        for (Ingredients ingredient : items) {
            if (knownIngredients.contains(ingredient)) shown.add(ingredient);
        }
        for (int slot = 0; slot < items.size(); slot++) {
            int x = gridX + (slot % GRID_COLUMNS) * CELL;
            int cy = gridY + (slot / GRID_COLUMNS) * CELL;
            Ingredients ingredient = slot < shown.size() ? shown.get(slot) : null;
            boolean over = ingredient != null && hovered(x, cy, CELL, CELL);
            cell(graphics, x, cy, ingredient == null ? 3 : ingredient == selectedIngredient ? 2 : over ? 1 : 0);
            if (ingredient == null) {
                icon(graphics, Icon.UNKNOWN, x + 3, cy + 3, true);
                hotspot(x, cy, CELL, CELL, null, () -> List.of(Component.translatable("alchemia.guide.ingredients.undiscovered")));
                continue;
            }
            ItemStack stackOf = ingredientStack(ingredient);
            graphics.renderItem(stackOf, x + 1, cy + 1);
            hotspot(x, cy, CELL, CELL, () -> {
                selectedIngredient = ingredient;
                selectedReagent = null;
                resetScroll();
            }, () -> List.of(stackOf.getHoverName()));
        }
        return gridY + Mth.ceil(items.size() / (float) GRID_COLUMNS) * CELL;
    }

    private void extractReagents(GuiGraphics graphics) {
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.reagents.title"), LEFT_X, TOP + 2);
        int gridX = LEFT_X + (PAGE_W - GRID_COLUMNS * CELL) / 2;
        if (reagentSpread == 0) {
            List<Item> reagents = generalReagents();
            centred(graphics, Component.translatable("alchemia.guide.reagents.general", known(reagents), reagents.size()), LEFT_X, y, INK_HEAD);
            int gridEnd = reagentGrid(graphics, reagents, gridX, y + 12);
            List<Item> monsters = lureReagents();
            centred(graphics, Component.translatable("alchemia.guide.reagents.monster_drops", known(monsters), monsters.size()), LEFT_X, gridEnd + 6, INK_HEAD);
            gridEnd = reagentGrid(graphics, monsters, gridX, gridEnd + 18);
            List<Ingredients> minerals = ingredientsOf(IngredientType.Mineral);
            centred(graphics, Component.translatable("alchemia.guide.ingredients.mineral", knownIngredients(minerals), minerals.size()), LEFT_X, gridEnd + 6, INK_HEAD);
            ingredientGrid(graphics, minerals, gridX, gridEnd + 18);
        } else {
            List<Item> modifiers = VanillaIngredients.MODIFIERS;
            centred(graphics, Component.translatable("alchemia.guide.reagents.modifiers", known(modifiers), modifiers.size()), LEFT_X, y, INK_HEAD);
            int gridEnd = reagentGrid(graphics, modifiers, gridX, y + 12);
            List<Item> imbuements = Imbuements.items();
            centred(graphics, Component.translatable("alchemia.guide.reagents.imbuements", known(imbuements), imbuements.size()), LEFT_X, gridEnd + 6, INK_HEAD);
            reagentGrid(graphics, imbuements, gridX, gridEnd + 18);
        }

        if (reagentSpread == 0) corner(graphics, false, () -> {
            reagentPage = false;
            reagentSpread = 0;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.chapter.ingredients"));
        else corner(graphics, false, () -> {
            reagentSpread = 0;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.previous"));
        if (reagentSpread == 0) corner(graphics, true, () -> {
            reagentSpread = 1;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.next"));
        scrollPanel(graphics, TOP, BOTTOM - 20, () -> selectedReagent != null ? reagentDetail(graphics) : ingredientDetail(graphics));
    }

    private int imbuementDetail(GuiGraphics graphics, Item item, int y, int width) {
        Imbuements.Attuner attuner = Imbuements.of(new ItemStack(item));
        Component region = item instanceof ItemBark ? Component.translatable("alchemia.guide.reagent.attunes.bark")
                : attuner == null ? Component.empty() : attuner.region();
        y = wrapped(graphics, Component.translatable("alchemia.guide.reagent.imbuement"), RIGHT_X, y, width, INK_HEAD) + 2;
        y = wrapped(graphics, Component.translatable("alchemia.guide.reagent.attunes", region), RIGHT_X, y, width, INK_HEAD) + 2;
        boolean altitude = attuner != null && attuner.placesAltitude();
        y = wrapped(graphics, Component.translatable(altitude ? "alchemia.guide.reagent.imbuement.altitude" : "alchemia.guide.reagent.imbuement.land"),
                RIGHT_X, y, width, INK_SOFT) + 2;
        String gather = "alchemia.guide.reagent." + BuiltInRegistries.ITEM.getKey(item).getPath() + ".gather";
        Component gathered = Component.translatableWithFallback(gather, "");
        if (!gathered.getString().isEmpty()) y = wrapped(graphics, gathered, RIGHT_X, y, width, INK_SOFT) + 2;
        return y + 2;
    }

    private int known(List<Item> items) {
        int count = 0;
        for (Item item : items) {
            if (knownReagents.contains(item)) count++;
        }
        return count;
    }

    private int reagentGrid(GuiGraphics graphics, List<Item> items, int gridX, int gridY) {
        List<Item> shown = new ArrayList<>();
        for (Item item : items) {
            if (knownReagents.contains(item)) shown.add(item);
        }
        for (int slot = 0; slot < items.size(); slot++) {
            int x = gridX + (slot % GRID_COLUMNS) * CELL;
            int cy = gridY + (slot / GRID_COLUMNS) * CELL;
            Item reagent = slot < shown.size() ? shown.get(slot) : null;
            boolean over = reagent != null && hovered(x, cy, CELL, CELL);
            cell(graphics, x, cy, reagent == null ? 3 : reagent == selectedReagent ? 2 : over ? 1 : 0);
            if (reagent == null) {
                icon(graphics, Icon.UNKNOWN, x + 3, cy + 3, true);
                hotspot(x, cy, CELL, CELL, null, () -> List.of(Component.translatable("alchemia.guide.ingredients.undiscovered")));
                continue;
            }
            ItemStack stackOf = new ItemStack(reagent);
            graphics.renderItem(stackOf, x + 1, cy + 1);
            hotspot(x, cy, CELL, CELL, () -> {
                selectedReagent = reagent;
                selectedIngredient = null;
                resetScroll();
            }, () -> List.of(stackOf.getHoverName()));
        }
        return gridY + Mth.ceil(items.size() / (float) GRID_COLUMNS) * CELL;
    }

    private int reagentDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll;
        if (selectedReagent == null) {
            return wrapped(graphics, Component.translatable("alchemia.guide.reagents.select"), RIGHT_X, y + 30, width, INK_FAINT);
        }

        ItemStack item = new ItemStack(selectedReagent);
        boolean attuning = Imbuements.isAttuner(item);
        y = banner(graphics, item.getHoverName().getString(), y, width, (x, iy) -> graphics.renderItem(item, x + 1, iy + 1));

        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(item);
        if (drift != null) {
            Component target = drift.target().getEffect().getDisplayName();
            boolean over = hovered(RIGHT_X - 2, y - 1, width + 4, LINE + 1) && inView();
            y = wrapped(graphics, Component.translatable(drift.lure() ? "alchemia.guide.reagent.lure" : "alchemia.guide.reagent.drift", target),
                    RIGHT_X, y, width, over ? INK_LINK : INK_HEAD) + 2;
            clippedHotspot(RIGHT_X - 2, y - LINE - 3, width + 4, LINE + 1, () -> followEffect(drift.target()), () -> List.of(target));
            y = wrapped(graphics, Component.translatable(drift.lure() ? "alchemia.guide.reagent.lure.detail" : "alchemia.guide.reagent.drift.detail"),
                    RIGHT_X, y, width, INK_SOFT) + 2;
            y = wrapped(graphics, Component.translatable(drift.lure() ? "alchemia.guide.reagent.lure.grind" : "alchemia.guide.reagent.drift.grind",
                    String.format(java.util.Locale.ROOT, "%.1f", VanillaIngredients.grindScale(Ingredients.MAX_CRUSH_UNITS))),
                    RIGHT_X, y, width, INK_SOFT) + 4;

            PathPanel panel = drift.lure() || !PotionMap.isReady() ? null : driftPanel(drift);
            if (panel != null && panel.drawable()) {
                y = subtitle(graphics, Component.translatable("alchemia.tooltip.label.path"), y + 2, width) + 6;
                int pathW = panel.plainWidth();
                int pathH = panel.plainHeight();
                int pathX = RIGHT_X + (width - pathW) / 2;
                inkCorners(graphics, pathX - 4, y - 2, pathW + 8, pathH + 4);
                panel.drawPlain(graphics, pathX, y);
                y += pathH + 8;
            }
        } else if (attuning) {
            y = imbuementDetail(graphics, selectedReagent, y, width);
        } else {
            String use = selectedReagent == Items.REDSTONE ? "alchemia.tooltip.use.redstone"
                    : selectedReagent == Items.GUNPOWDER ? "alchemia.tooltip.use.gunpowder"
                    : selectedReagent == Items.GLOWSTONE_DUST ? "alchemia.tooltip.use.glowstone" : "alchemia.tooltip.use.dragon_breath";
            y = wrapped(graphics, Component.translatable("alchemia.guide.reagent.modifier"), RIGHT_X, y, width, INK_HEAD) + 2;
            y = wrapped(graphics, Component.translatable(use), RIGHT_X, y, width, INK_SOFT) + 4;
        }

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.your_notes"), y, width);
        y = ingredientNotes(graphics, reagentKey(selectedReagent), y, width) + 6;

        List<BrewRecipe> using = new ArrayList<>();
        for (BrewRecipe recipe : recipes) {
            boolean uses = false;
            for (ItemStack used : recipe.resolve()) uses |= used.getItem() == selectedReagent && !attuning;
            if (selectedReagent == Items.REDSTONE) uses |= recipe.steps().contains(String.valueOf(BrewRecipe.STEP_REDSTONE));
            if (selectedReagent == Items.GUNPOWDER) uses |= splash(recipe);
            if (selectedReagent == Items.GLOWSTONE_DUST) uses |= recipe.steps().contains(String.valueOf(BrewRecipe.STEP_GLOW));
            if (attuning) uses |= Imbuements.of(recipe).stream().anyMatch(used -> used.item() == selectedReagent);
            if (uses) using.add(recipe);
        }
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.ingredient.recipes"), y, width);
        if (using.isEmpty()) return wrapped(graphics, Component.translatable("alchemia.guide.effect.no_recipes"), RIGHT_X, y, width, INK_FAINT);
        return linkedRecipes(graphics, using, y, width);
    }

    private static PathPanel driftPanel(VanillaIngredients.Drift drift) {
        PotionMap map = PotionMap.get(muddykat.alchemia.common.potion.BrewBases.defaultBase());
        List<PathPoint> full = map.fixedDriftOffsets(drift.target(), Ingredients.MAX_CRUSH_UNITS);
        double base = PathGeometry.length(map.fixedDriftOffsets(drift.target(), 0));
        return new PathPanel(Component.translatable("alchemia.tooltip.label.path"), full, base, false, 96, 44);
    }

    private int ingredientDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll;
        if (selectedIngredient == null) {
            return wrapped(graphics, Component.translatable("alchemia.guide.ingredients.select"), RIGHT_X, y + 30, width, INK_FAINT);
        }

        ItemStack icon = ingredientStack(selectedIngredient);
        y = banner(graphics, icon.getHoverName().getString(), y, width, (x, iy) -> graphics.renderItem(icon, x + 1, iy + 1));


        PathPanel panel = pathPanel(selectedIngredient);
        if (panel != null && panel.drawable()) {
            boolean teleports = IngredientPath.of(selectedIngredient).teleports();
            y = subtitle(graphics, Component.translatable(teleports ? "alchemia.tooltip.label.teleport" : "alchemia.tooltip.label.path"), y + 2, width) + 6;
            int pathW = panel.plainWidth();
            int pathH = panel.plainHeight();
            int pathX = RIGHT_X + (width - pathW) / 2;
            inkCorners(graphics, pathX - 4, y - 2, pathW + 8, pathH + 4);
            panel.drawPlain(graphics, pathX, y);
            y += pathH + 8;
        }

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.your_notes"), y, width);
        y = ingredientNotes(graphics, selectedIngredient.name(), y, width) + 6;

        List<BrewRecipe> using = new ArrayList<>();
        for (BrewRecipe recipe : recipes) {
            for (ItemStack item : recipe.resolve()) {
                if (item.getItem() instanceof ItemIngredient ingredient && ingredient.getIngredient() == selectedIngredient) {
                    using.add(recipe);
                    break;
                }
            }
        }
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.ingredient.recipes"), y, width);
        if (using.isEmpty()) return wrapped(graphics, Component.translatable("alchemia.guide.effect.no_recipes"), RIGHT_X, y, width, INK_FAINT);
        return linkedRecipes(graphics, using, y, width);
    }

    private int ingredientNotes(GuiGraphics graphics, String key, int y, int width) {
        int height = INGREDIENT_NOTE_LINES * LINE + 5;
        int boxX = RIGHT_X - 2;
        int boxW = width + 4;
        graphics.fill(boxX, y, boxX + boxW, y + 1, RULE);
        graphics.fill(boxX, y + height, boxX + boxW, y + height + 1, RULE);
        graphics.fill(boxX, y, boxX + 1, y + height, RULE);
        graphics.fill(boxX + boxW - 1, y, boxX + boxW, y + height, RULE);
        int textY = y + 4;
        for (int index = 0; index < INGREDIENT_NOTE_LINES; index++) {
            int ruleY = textY + index * LINE + LINE - 1;
            graphics.fill(RIGHT_X, ruleY, RIGHT_X + width, ruleY + 1, 0x33B8A27A);
        }

        boolean active = editing(Editor.INGREDIENT_NOTES, key);
        String saved = BrewRecipeBook.ingredientNotes(stack).getOrDefault(key, "");
        String text = active ? nameField.getValue() : saved;
        if (active) markEditor(boxX, y, boxW, height);
        if (text.isEmpty() && !active) noteHint(graphics, RIGHT_X + 1, textY, width - 2, INGREDIENT_NOTE_LINES);
        drawNote(graphics, text, RIGHT_X + 1, textY, width - 2, INGREDIENT_NOTE_LINES, active);
        if (!active) {
            clippedHotspot(boxX, y, boxW, height,
                    () -> startEdit(Editor.INGREDIENT_NOTES, key, saved, PacketGuideAction.MAX_NOTE, width - 2, INGREDIENT_NOTE_LINES),
                    () -> List.of(Component.translatable("alchemia.guide.notes.tip")));
        }
        return y + height;
    }

    private static void inkCorners(GuiGraphics graphics, int x, int y, int w, int h) {
        int arm = 5;
        int colour = 0xA0967E5C;
        graphics.fill(x, y, x + arm, y + 1, colour);
        graphics.fill(x, y, x + 1, y + arm, colour);
        graphics.fill(x + w - arm, y, x + w, y + 1, colour);
        graphics.fill(x + w - 1, y, x + w, y + arm, colour);
        graphics.fill(x, y + h - 1, x + arm, y + h, colour);
        graphics.fill(x, y + h - arm, x + 1, y + h, colour);
        graphics.fill(x + w - arm, y + h - 1, x + w, y + h, colour);
        graphics.fill(x + w - 1, y + h - arm, x + w, y + h, colour);
    }

    private static @Nullable PathPanel pathPanel(Ingredients ingredient) {
        IngredientPath shape = IngredientPath.of(ingredient);
        int dx = Integer.signum(ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX());
        int dy = Integer.signum(ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY());
        if (shape.needsDirection() && dx == 0 && dy == 0) return null;

        double ceiling = IngredientPath.teleportCeiling(ingredient);
        List<PathPoint> full = shape.teleports()
                ? shape.teleportPreview(dx, dy, ceiling, ceiling)
                : IngredientPath.pathFor(ingredient, dx, dy, ingredient.getPotency(Ingredients.MAX_CRUSH));
        List<PathPoint> base = shape.teleports()
                ? shape.teleportPreview(dx, dy, ceiling, IngredientPath.reachFor(ingredient.getPotency(0), 0))
                : IngredientPath.pathFor(ingredient, dx, dy, ingredient.getPotency(0));

        Component label = Component.translatable(shape.teleports() ? "alchemia.tooltip.label.teleport" : "alchemia.tooltip.label.path");
        return new PathPanel(label, full, PathGeometry.length(base), shape.teleports(), 96, 44);
    }

    private int countHeld(ItemStack wanted) {
        if (this.minecraft == null || this.minecraft.player == null) return 0;
        int found = 0;
        for (ItemStack held : this.minecraft.player.getInventory().items) {
            if (held.getItem() != wanted.getItem()) continue;
            if (wanted.getItem() instanceof ItemBark && !Imbuements.sameKind(held, wanted)) continue;
            found += held.getCount();
        }
        return found;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        toBook(mouseX, mouseY);
        if (editor != null) {
            if (bookMouseX >= editX && bookMouseX < editX + editW && bookMouseY >= editY && bookMouseY < editY + editH) return true;
            commitEdit();
        }
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (startDrag()) return true;
            for (int index = hotspots.size() - 1; index >= 0; index--) {
                Hotspot spot = hotspots.get(index);
                if (!spot.covers(bookMouseX, bookMouseY)) continue;
                if (spot.action() != null) {
                    spot.action().run();
                    return true;
                }
                break;
            }
        }
        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            for (int index = rightHotspots.size() - 1; index >= 0; index--) {
                Hotspot spot = rightHotspots.get(index);
                if (spot.covers(bookMouseX, bookMouseY) && spot.action() != null) {
                    spot.action().run();
                    return true;
                }
            }
        }
        if (button == InputConstants.MOUSE_BUTTON_RIGHT && openRecipe != null && !pageMode) {
            openRecipe = null;
            resetScroll();
            turnPage();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean scrollable() {
        return contentHeight > viewBottom - viewTop && (openRecipe != null || chapter == Chapter.EFFECTS || chapter == Chapter.INGREDIENTS
                || chapter == Chapter.OPUS);
    }

    private boolean startDrag() {
        if (!scrollable()) return false;
        int height = viewBottom - viewTop;
        int maxScroll = Math.max(1, contentHeight - height);
        int thumbH = thumbHeight(height);
        int thumbY = viewTop + (int) Math.round((height - thumbH) * scroll / maxScroll);
        int x = scrollX();
        if (bookMouseX < x || bookMouseX >= x + TRACK_W || bookMouseY < viewTop || bookMouseY >= viewBottom) return false;

        if (bookMouseY < thumbY || bookMouseY >= thumbY + thumbH) {
            scroll = Mth.clamp((bookMouseY - viewTop - thumbH / 2.0) / (height - thumbH) * maxScroll, 0, maxScroll);
            thumbY = viewTop + (int) Math.round((height - thumbH) * scroll / maxScroll);
        }
        dragging = true;
        dragOffset = bookMouseY - thumbY;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (!dragging) return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        toBook(mouseX, mouseY);
        int height = viewBottom - viewTop;
        int maxScroll = Math.max(1, contentHeight - height);
        int thumbH = thumbHeight(height);
        scroll = Mth.clamp((bookMouseY - dragOffset - viewTop) / Math.max(1, height - thumbH) * maxScroll, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        toBook(mouseX, mouseY);
        if (ingredientMax > 0 && hovered(ingredientPaneX, ingredientPaneY, ingredientPaneW, ingredientPaneH)) {
            ingredientScroll = Mth.clamp(ingredientScroll - Math.signum(scrollY) * INGREDIENT_ROW, 0, ingredientMax);
            return true;
        }
        boolean overRight = bookMouseX >= RIGHT_X - 4 && bookMouseX < RIGHT_X + PAGE_W + 6;
        if (scrollable() && overRight) {
            scroll = Mth.clamp(scroll - scrollY * LINE * 2, 0, Math.max(0, contentHeight - (viewBottom - viewTop)));
            return true;
        }
        if (chapter == Chapter.RECIPES && openRecipe == null) {
            flipIndex(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (editor != null && nameField != null) {
            if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
                commitEdit();
                return true;
            }
            if (key == InputConstants.KEY_ESCAPE) {
                stopEdit();
                return true;
            }
            nameField.keyPressed(key, scanCode, modifiers);
            return true;
        }
        if (key == InputConstants.KEY_LEFT || key == InputConstants.KEY_RIGHT) {
            int delta = key == InputConstants.KEY_LEFT ? -1 : 1;
            if (openRecipe != null && !pageMode) {
                flipRecipe(delta);
            } else if (chapter == Chapter.RECIPES) {
                flipIndex(delta);
            } else if (!pageMode) {
                goTo(Chapter.values()[Math.floorMod(chapter.ordinal() + delta, Chapter.values().length)]);
            }
            return true;
        }
        if (key == InputConstants.KEY_BACKSPACE && openRecipe != null && !pageMode) {
            openRecipe = null;
            resetScroll();
            turnPage();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
