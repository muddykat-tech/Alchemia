package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.network.packets.PacketForgetRecipe;
import muddykat.alchemia.common.network.packets.PacketSelectRecipe;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.core.component.DataComponentType;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import muddykat.alchemia.common.network.packets.PacketDiscover;
import muddykat.alchemia.common.items.ItemIngredient;
import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.Hud;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GuideScreen extends Screen {
    private static final Identifier PAGES_TEXTURE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/book/images/alchemical_guide_pages.png");

    private static final int SHEET = 512;

    private static final int COVER_W = 412;
    private static final int COVER_H = 200;
    private static final int PAGES_U = 0;
    private static final int PAGES_V = 201;
    private static final int PAGES_W = 404;
    private static final int PAGES_H = 192;
    private static final int PAGES_INSET = 0;

    private static final int LEFT_PAGE_X = 20;
    private static final int RIGHT_PAGE_X = 213;
    private static final int PAGE_Y = 13;
    private static final int LEFT_PAGE_W = 180;
    private static final int RIGHT_PAGE_W = 179;
    private static final int PAGE_H = 172;

    private static final int MARK_U = 0;
    private static final int MARK_RIGHT_V = 400;
    private static final int MARK_LEFT_V = 409;
    private static final int MARK_W = 31;
    private static final int MARK_H = 9;

    private static final int CHIP_U = 32;
    private static final int CHIP_V = 402;
    private static final int CHIP_SIZE = 5;
    private static final int CHIP_GAP = 1;
    private static final int CHIP_Y_OFFSET = 2;

    private static final int LEFT_EDGE_X = 8;
    private static final int RIGHT_EDGE_X = 403;

    private static final int TURN_RIGHT_V = 0;
    private static final int TURN_LEFT_V = 10;
    private static final int TURN_U = 412;
    private static final int TURN_W = 18;
    private static final int TURN_H = 10;
    private static final int TAB_SPACING = 14;

    private static final int ARROW_UP_U = 412;
    private static final int ARROW_DOWN_U = 422;
    private static final int ARROW_V = 58;
    private static final int ARROW_W = 10;
    private static final int ARROW_H = 18;

    private static final int ROW_HEIGHT = 13;
    private static final int LINE_HEIGHT = 10;
    private static final int HEADER_H = 14;
    private static final int LIST_TEXT_X = LEFT_PAGE_X;
    private static final int NAME_W = LEFT_PAGE_W;

    private static final int INK = 0xFF4A3A28;
    private static final int INK_FAINT = 0xFF8A7A62;
    private static final int INK_HEAD = 0xFF2A1F14;
    private static final int INK_SHORT = 0xFFA03030;
    private static final int INK_LINK = 0xFF2A5AA0;
    private static final int DETAIL_ROW_LIMIT = 3;

    private static final int DELETE_W = 62;
    private static final int DELETE_H = 16;

    private static final int GRID_CELL = 5;
    private static final double PREVIEW_SCALE = 0.36;
    private static final int PATH_TAKEN = PathPalette.TAKEN;
    private static final int PATH_POTENTIAL = PathPalette.POTENTIAL;
    private static final int PATH_ORIGIN = PathPalette.ORIGIN;

    public enum Section {
        RECIPES("recipes"),
        EFFECTS("effects"),
        INGREDIENTS("ingredients");

        private final String key;

        Section(String key) {
            this.key = key;
        }

        public Component title() {
            return Component.translatable("alchemia.guide.section." + key);
        }
    }

    private final ItemStack guide;
    private final InteractionHand hand;

    private record Link(int x, int y, int w, int h, Section target, String name) {
        boolean covers(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        }
    }

    private final List<Link> links = new ArrayList<>();

    private Section section = Section.RECIPES;
    private PotionEnum selectedEffect;
    private Ingredients selectedIngredient;
    private List<PotionEnum> effects = List.of();
    private List<Ingredients> ingredients = List.of();

    private List<BrewRecipe> recipes = List.of();
    private Set<PotionEnum> discovered = Set.of();
    private String selected;
    private int scroll;
    private int left;
    private int top;

    public GuideScreen(ItemStack guide, InteractionHand hand) {
        super(Component.translatable("alchemia.guide.title"));
        this.guide = guide;
        this.hand = hand;
    }

    @Override
    protected void init() {
        this.left = (this.width - COVER_W) / 2;
        this.top = (this.height - COVER_H) / 2;
        refresh();
    }

    private void refresh() {
        recipes = new ArrayList<>(BrewRecipeBook.recipes(guide));
        discovered = RecipeDiscovery.knownTo(guide);
        selected = BrewRecipeBook.selected(guide);

        effects = new ArrayList<>(discovered);
        effects.sort(Comparator.comparing(Enum::name));

        ingredients = new ArrayList<>(IngredientDiscovery.knownTo(guide));
        ingredients.sort(Comparator.comparing(Enum::name));

        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private int entryCount() {
        return switch (section) {
            case RECIPES -> recipes.size();
            case EFFECTS -> effects.size();
            case INGREDIENTS -> ingredients.size();
        };
    }

    private Component entryName(int index) {
        return switch (section) {
            case RECIPES -> Component.translatable("alchemia.guide.recipe.entry",
                    recipes.get(index).displayName(),
                    Component.translatable(recipes.get(index).brewBase().translationKey()));
            case EFFECTS -> effects.get(index).getEffect().value().getDisplayName();
            case INGREDIENTS -> ingredientStack(ingredients.get(index)).getHoverName();
        };
    }

    private boolean entrySelected(int index) {
        return switch (section) {
            case RECIPES -> recipes.get(index).id().equals(selected);
            case EFFECTS -> effects.get(index) == selectedEffect;
            case INGREDIENTS -> ingredients.get(index) == selectedIngredient;
        };
    }

    private static ItemStack ingredientStack(Ingredients ingredient) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(Alchemia.MODID, ingredient.getRegistryName())));
    }

    private void setSection(Section next) {
        section = next;
        scroll = 0;
    }

    private void follow(Link link) {
        boolean toIngredient = link.target() == Section.INGREDIENTS;

        if (toIngredient) {
            Ingredients ingredient = IngredientDiscovery.byName(link.name());
            if (ingredient == null) return;
            discover(true, link.name(), DataComponentRegistry.DISCOVERED_INGREDIENTS.get());
            selectedIngredient = ingredient;
        } else {
            PotionEnum effect = RecipeDiscovery.byName(link.name());
            if (effect == null) return;
            discover(false, link.name(), DataComponentRegistry.DISCOVERED_RECIPES.get());
            selectedEffect = effect;
        }

        setSection(link.target());
        refresh();
    }

    private void discover(boolean ingredient, String name, DataComponentType<List<String>> component) {
        List<String> known = new ArrayList<>(guide.getOrDefault(component, List.of()));
        if (known.contains(name)) return;

        known.add(name);
        known.sort(null);
        guide.set(component, List.copyOf(known));
        ClientPacketDistributor.sendToServer(new PacketDiscover(ingredient, name, hand == InteractionHand.MAIN_HAND));
    }

    private int tabY(int index) {
        return pageTop() + 6 + index * TAB_SPACING;
    }

    private int sectionTabAt(double mouseX, double mouseY) {
        if (mouseX < rightMarkX() || mouseX >= rightMarkX() + MARK_W) return -1;
        for (int i = 0; i < Section.values().length; i++) {
            if (mouseY >= tabY(i) && mouseY < tabY(i) + MARK_H) return i;
        }
        return -1;
    }

    private int turnY() {
        return pageBottom() - TURN_H;
    }

    private boolean overTurn(double mouseX, double mouseY, int x) {
        return mouseX >= x && mouseX < x + TURN_W && mouseY >= turnY() && mouseY < turnY() + TURN_H;
    }

    private int turnRightX() {
        return detailX() + RIGHT_PAGE_W - TURN_W;
    }

    private int turnLeftX() {
        return turnRightX() - TURN_W - 4;
    }

    private int visibleRows() {
        return (PAGE_H - HEADER_H - ARROW_H - 4) / ROW_HEIGHT;
    }

    private int maxScroll() {
        return Math.max(0, entryCount() - visibleRows());
    }

    private int pageTop() {
        return top + PAGES_INSET + PAGE_Y;
    }

    private int pageBottom() {
        return pageTop() + PAGE_H;
    }

    private int listX() {
        return left + PAGES_INSET + LEFT_PAGE_X;
    }

    private int markX() {
        return left + PAGES_INSET + LEFT_EDGE_X;
    }

    private int listTextX() {
        return left + PAGES_INSET + LIST_TEXT_X;
    }

    private int leftMarkX() {
        return markX() - MARK_W;
    }

    private int rightMarkX() {
        return left + PAGES_INSET + RIGHT_EDGE_X;
    }

    private void leftBookmark(GuiGraphicsExtractor graphics, int y, boolean selected) {
        int x = leftMarkX();
        sprite(graphics, x, y, MARK_U, MARK_LEFT_V, MARK_W, MARK_H);
        if (selected) {
            sprite(graphics, x - CHIP_SIZE - CHIP_GAP, y + CHIP_Y_OFFSET, CHIP_U, CHIP_V, CHIP_SIZE, CHIP_SIZE);
        }
    }

    private void rightBookmark(GuiGraphicsExtractor graphics, int y, boolean selected) {
        int x = rightMarkX();
        sprite(graphics, x, y, MARK_U, MARK_RIGHT_V, MARK_W, MARK_H);
        if (selected) {
            sprite(graphics, x + MARK_W + CHIP_GAP, y + CHIP_Y_OFFSET, CHIP_U, CHIP_V, CHIP_SIZE, CHIP_SIZE);
        }
    }

    private int markRowY(int slot) {
        return listTop() + slot * ROW_HEIGHT - 1;
    }

    private int bookmarkAt(double mouseX, double mouseY) {
        if (mouseX < leftMarkX() || mouseX >= leftMarkX() + MARK_W) return -1;

        int rows = Math.min(visibleRows(), entryCount() - scroll);
        for (int i = 0; i < rows; i++) {
            int y = markRowY(i);
            if (mouseY >= y && mouseY < y + MARK_H) return scroll + i;
        }
        return -1;
    }

    private void select(int index) {
        switch (section) {
            case RECIPES -> {
                String name = recipes.get(index).id();
                selected = name.equals(selected) ? null : name;
                BrewRecipeBook.select(guide, selected);
                ClientPacketDistributor.sendToServer(new PacketSelectRecipe(selected == null ? "" : selected, hand == InteractionHand.MAIN_HAND));
            }
            case EFFECTS -> selectedEffect = effects.get(index) == selectedEffect ? null : effects.get(index);
            case INGREDIENTS -> selectedIngredient = ingredients.get(index) == selectedIngredient ? null : ingredients.get(index);
        }
    }

    private int detailX() {
        return left + PAGES_INSET + RIGHT_PAGE_X;
    }

    private int listTop() {
        return pageTop() + HEADER_H;
    }

    private int arrowY() {
        return pageBottom() - ARROW_H;
    }

    private int upArrowX() {
        return listX() + LEFT_PAGE_W - ARROW_W * 2 - 4;
    }

    private int downArrowX() {
        return listX() + LEFT_PAGE_W - ARROW_W;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    private boolean overArrow(double mouseX, double mouseY, int x) {
        return mouseX >= x && mouseX < x + ARROW_W && mouseY >= arrowY() && mouseY < arrowY() + ARROW_H;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (Link link : links) {
            if (link.covers(event.x(), event.y())) {
                follow(link);
                return true;
            }
        }

        int tab = sectionTabAt(event.x(), event.y());
        if (tab >= 0) {
            setSection(Section.values()[tab]);
            return true;
        }

        if (overTurn(event.x(), event.y(), turnLeftX())) {
            setSection(Section.values()[Math.floorMod(section.ordinal() - 1, Section.values().length)]);
            return true;
        }
        if (overTurn(event.x(), event.y(), turnRightX())) {
            setSection(Section.values()[Math.floorMod(section.ordinal() + 1, Section.values().length)]);
            return true;
        }

        if (maxScroll() > 0) {
            if (overArrow(event.x(), event.y(), upArrowX())) {
                scroll = Math.max(0, scroll - 1);
                return true;
            }
            if (overArrow(event.x(), event.y(), downArrowX())) {
                scroll = Math.min(maxScroll(), scroll + 1);
                return true;
            }
        }

        if (section == Section.RECIPES && overDelete(event.x(), event.y())) {
            String removed = selected;
            BrewRecipeBook.forget(guide, removed);
            selected = null;
            ClientPacketDistributor.sendToServer(new PacketForgetRecipe(removed, hand == InteractionHand.MAIN_HAND));
            refresh();
            return true;
        }

        int marked = bookmarkAt(event.x(), event.y());
        if (marked >= 0 && marked < entryCount()) {
            select(marked);
            return true;
        }

        int index = rowAt(event.x(), event.y());
        if (index >= 0 && index < entryCount()) {
            select(index);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < listX() || mouseX > listX() + LEFT_PAGE_W) return -1;
        if (mouseY < listTop() || mouseY > listTop() + visibleRows() * ROW_HEIGHT) return -1;
        return scroll + (int) ((mouseY - listTop()) / ROW_HEIGHT);
    }

    private void sprite(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, PAGES_TEXTURE, x, y, u, v, w, h, SHEET, SHEET);
    }

    private int drawWrapped(GuiGraphicsExtractor graphics, Component text, int x, int y, int maxWidth, int colour) {
        for (FormattedCharSequence line : this.font.split(text, maxWidth)) {
            graphics.text(this.font, line, x, y, colour, false);
            y += LINE_HEIGHT;
        }
        return y;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        refresh();
        super.extractRenderState(graphics, mouseX, mouseY, a);

        sprite(graphics, left, top, 0, 0, COVER_W, COVER_H);
        sprite(graphics, left + PAGES_INSET, top + PAGES_INSET, PAGES_U, PAGES_V, PAGES_W, PAGES_H);

        links.clear();
        extractList(graphics, mouseX, mouseY);

        switch (section) {
            case RECIPES -> extractDetail(graphics, mouseX, mouseY);
            case EFFECTS -> extractEffectDetail(graphics);
            case INGREDIENTS -> extractIngredientDetail(graphics);
        }

        extractSectionTabs(graphics, mouseX, mouseY);
    }

    private void extractList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = listX();
        Component header = switch (section) {
            case RECIPES -> Component.translatable("alchemia.guide.recipes.header", recipes.size());
            case EFFECTS -> Component.translatable("alchemia.guide.effects.header", effects.size(), RecipeDiscovery.total());
            case INGREDIENTS -> Component.translatable("alchemia.guide.ingredients.header", ingredients.size(), IngredientDiscovery.total());
        };
        graphics.text(this.font, header, listTextX(), pageTop(), INK_HEAD, false);

        if (entryCount() == 0) {
            Component empty = switch (section) {
                case RECIPES -> Component.translatable("alchemia.guide.recipes.none");
                case EFFECTS -> Component.translatable("alchemia.guide.effects.none");
                case INGREDIENTS -> Component.translatable("alchemia.guide.ingredients.none");
            };
            drawWrapped(graphics, empty, x, listTop(), LEFT_PAGE_W, INK_FAINT);
            return;
        }

        int rows = visibleRows();
        int hovered = rowAt(mouseX, mouseY);
        int marked = bookmarkAt(mouseX, mouseY);

        for (int i = 0; i < rows; i++) {
            int index = scroll + i;
            if (index >= entryCount()) break;

            int rowY = listTop() + i * ROW_HEIGHT;
            boolean isSelected = entrySelected(index);

            if (!isSelected && (hovered == index || marked == index)) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.ROW_HIGHLIGHT,
                        listTextX() - 2, rowY - 2, x + LEFT_PAGE_W - (listTextX() - 2), ROW_HEIGHT - 1);
            }

            leftBookmark(graphics, markRowY(i), isSelected);

            String name = this.font.plainSubstrByWidth(entryName(index).getString(), NAME_W);
            graphics.text(this.font, name, listTextX(), rowY, isSelected ? INK_HEAD : INK, false);
        }

        if (marked >= 0 && marked < entryCount()) {
            graphics.setTooltipForNextFrame(entryName(marked), mouseX, mouseY);
        }

        if (maxScroll() > 0) {
            sprite(graphics, upArrowX(), arrowY(), ARROW_UP_U, ARROW_V, ARROW_W, ARROW_H);
            sprite(graphics, downArrowX(), arrowY(), ARROW_DOWN_U, ARROW_V, ARROW_W, ARROW_H);

            Component page = Component.translatable("alchemia.guide.page", scroll + 1, maxScroll() + 1);
            graphics.text(this.font, page, listTextX(), arrowY() + 5, INK_FAINT, false);
        }
    }

    private void extractSectionTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int hovered = sectionTabAt(mouseX, mouseY);

        for (int i = 0; i < Section.values().length; i++) {
            rightBookmark(graphics, tabY(i), Section.values()[i] == section);
        }

        if (hovered >= 0) {
            graphics.setTooltipForNextFrame(Section.values()[hovered].title(), mouseX, mouseY);
        }

        sprite(graphics, turnLeftX(), turnY(), TURN_U, TURN_LEFT_V, TURN_W, TURN_H);
        sprite(graphics, turnRightX(), turnY(), TURN_U, TURN_RIGHT_V, TURN_W, TURN_H);
    }

    private void extractEffectDetail(GuiGraphicsExtractor graphics) {
        int x = detailX();
        int y = pageTop();

        if (selectedEffect == null) {
            drawWrapped(graphics, Component.translatable("alchemia.guide.effects.select"), x, y, RIGHT_PAGE_W, INK_FAINT);
            return;
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(selectedEffect.getEffect()), x, y - 2, 16, 16);
        drawWrapped(graphics, selectedEffect.getEffect().value().getDisplayName(), x + 20, y + 2, RIGHT_PAGE_W - 20, INK_HEAD);
        y += 22;

        y = drawWrapped(graphics, Component.translatable("alchemia.guide.effect.strength", selectedEffect.getMaxStrength() + 1),
                x, y, RIGHT_PAGE_W, INK);
        y = drawWrapped(graphics, Component.translatable("alchemia.guide.effect.rarity",
                Component.literal(selectedEffect.getRarity().name())), x, y, RIGHT_PAGE_W, INK_FAINT);

        drawWrapped(graphics, Component.translatable("alchemia.guide.effect.alignment",
                        alignmentName(selectedEffect.getPrimaryAlignment()), alignmentName(selectedEffect.getSecondaryAlignment())),
                x, y + 2, RIGHT_PAGE_W, INK_FAINT);
    }

    private static Component alignmentName(IngredientAlignment alignment) {
        return Component.translatable("alchemia.alignment." + alignment.name().toLowerCase());
    }

    private void extractIngredientDetail(GuiGraphicsExtractor graphics) {
        int x = detailX();
        int y = pageTop();

        if (selectedIngredient == null) {
            drawWrapped(graphics, Component.translatable("alchemia.guide.ingredients.select"), x, y, RIGHT_PAGE_W, INK_FAINT);
            return;
        }

        ItemStack icon = ingredientStack(selectedIngredient);
        graphics.item(icon, x, y - 2);
        drawWrapped(graphics, icon.getHoverName(), x + 20, y + 2, RIGHT_PAGE_W - 20, INK_HEAD);
        y += 22;

        IngredientPath shape = IngredientPath.of(selectedIngredient);
        y = drawWrapped(graphics, Component.translatable("alchemia.guide.ingredient.alignment",
                alignmentName(selectedIngredient.getPrimaryAlignment()),
                alignmentName(selectedIngredient.getSecondaryAlignment())), x, y, RIGHT_PAGE_W, INK_FAINT);

        int dx = Integer.signum(selectedIngredient.getPrimaryAlignment().getX() + selectedIngredient.getSecondaryAlignment().getX());
        int dy = Integer.signum(selectedIngredient.getPrimaryAlignment().getY() + selectedIngredient.getSecondaryAlignment().getY());

        if (shape.teleports()) {
            y = drawWrapped(graphics, Component.translatable("alchemia.tooltip.teleport",
                    IngredientPath.teleportDistance(selectedIngredient.getPotency(Ingredients.MAX_CRUSH))), x, y, RIGHT_PAGE_W, INK);
        }

        if (shape.needsDirection() && dx == 0 && dy == 0) return;

        List<int[]> full = shape.teleports()
                ? shape.teleportPreview(dx, dy, selectedIngredient.getPotency(Ingredients.MAX_CRUSH), PREVIEW_SCALE)
                : IngredientPath.pathFor(selectedIngredient, dx, dy, selectedIngredient.getPotency(Ingredients.MAX_CRUSH));
        List<int[]> base = shape.teleports()
                ? shape.teleportPreview(dx, dy, selectedIngredient.getPotency(0), PREVIEW_SCALE)
                : IngredientPath.pathFor(selectedIngredient, dx, dy, selectedIngredient.getPotency(0));

        drawPathGrid(graphics, full, base.size(), x, y + 4);
    }

    private void drawPathGrid(GuiGraphicsExtractor graphics, List<int[]> cells, int taken, int x, int y) {
        if (cells.isEmpty()) return;

        int lowX = 0, highX = 0, lowY = 0, highY = 0;
        for (int[] cell : cells) {
            lowX = Math.min(lowX, cell[0]);
            highX = Math.max(highX, cell[0]);
            lowY = Math.min(lowY, cell[1]);
            highY = Math.max(highY, cell[1]);
        }

        int columns = highX - lowX + 1;
        int rows = highY - lowY + 1;
        int cell = Math.max(2, Math.min(GRID_CELL, Math.min(RIGHT_PAGE_W / Math.max(1, columns),
                (pageBottom() - y) / Math.max(1, rows))));

        int gridX = x + (RIGHT_PAGE_W - columns * cell) / 2;
        int originX = gridX + (-lowX) * cell;
        int originY = y + (-lowY) * cell;

        for (int i = 0; i < cells.size(); i++) {
            int[] point = cells.get(i);
            int px = gridX + (point[0] - lowX) * cell;
            int py = y + (point[1] - lowY) * cell;
            graphics.fill(px, py, px + cell - 1, py + cell - 1, i < taken ? PATH_TAKEN : PATH_POTENTIAL);
        }

        graphics.fill(originX, originY, originX + cell - 1, originY + cell - 1, PATH_ORIGIN);
    }

    private static boolean hovering(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void extractDetail(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = detailX();
        int y = pageTop();

        BrewRecipe recipe = selected == null ? null : BrewRecipeBook.find(guide, selected);
        if (recipe == null) {
            drawWrapped(graphics, Component.translatable("alchemia.guide.select"), x, y, RIGHT_PAGE_W, INK_FAINT);
            return;
        }

        List<FormattedCharSequence> hint = this.font.split(Component.translatable("alchemia.guide.brew.hint"), RIGHT_PAGE_W);
        int hintY = deleteY() - 4 - hint.size() * LINE_HEIGHT;
        int contentBottom = hintY - 4;

        if (!recipe.name().isBlank()) {
            y = drawWrapped(graphics, Component.literal(recipe.name()), x, y, RIGHT_PAGE_W, INK_HEAD) + 2;
        }

        y = drawWrapped(graphics, Component.translatable("alchemia.guide.recipe.base",
                Component.translatable(recipe.brewBase().translationKey())), x, y, RIGHT_PAGE_W, INK_FAINT) + 2;

        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            if (y + LINE_HEIGHT > contentBottom) break;

            Component line = Component.translatable("alchemia.guide.effect.line", effect.displayName(), effect.potency());
            PotionEnum linked = effect.asPotion();
            boolean hot = linked != null && hovering(mouseX, mouseY, x, y, this.font.width(line), LINE_HEIGHT);

            y = drawWrapped(graphics, line, x, y, RIGHT_PAGE_W, hot ? INK_LINK : INK);

            if (linked != null) {
                links.add(new Link(x, y - LINE_HEIGHT, this.font.width(line), LINE_HEIGHT, Section.EFFECTS, linked.name()));
            }
        }
        y += 4;

        if (y + 12 <= contentBottom) {
            graphics.text(this.font, Component.translatable("alchemia.guide.ingredients"), x, y, INK_HEAD, false);
            y += 12;
        }

        Map<ItemStack, Integer> tally = recipe.tally();
        int shown = 0;
        for (Map.Entry<ItemStack, Integer> entry : tally.entrySet()) {
            boolean last = shown == DETAIL_ROW_LIMIT || y + 18 > contentBottom;
            if (last) {
                if (y + LINE_HEIGHT <= contentBottom) {
                    graphics.text(this.font, Component.translatable("alchemia.guide.more", tally.size() - shown), x, y, INK_FAINT, false);
                    y += LINE_HEIGHT;
                }
                break;
            }
            shown++;

            ItemStack icon = entry.getKey();
            graphics.item(icon, x, y - 4);

            int held = countHeld(icon);
            boolean enough = held >= entry.getValue();
            Component label = Component.literal(entry.getValue() + "x ").append(icon.getHoverName());
            String clipped = this.font.plainSubstrByWidth(label.getString(), RIGHT_PAGE_W - 24 - 34);

            Ingredients linked = icon.getItem() instanceof ItemIngredient item ? item.getIngredient() : null;
            boolean hot = linked != null && hovering(mouseX, mouseY, x, y - 4, 20 + this.font.width(clipped), 16);

            graphics.text(this.font, clipped, x + 20, y, hot ? INK_LINK : (enough ? INK : INK_SHORT), false);

            if (linked != null) {
                links.add(new Link(x, y - 4, 20 + this.font.width(clipped), 16, Section.INGREDIENTS, linked.name()));
            }

            if (!enough) {
                Component shortfall = Component.translatable("alchemia.guide.have", held);
                graphics.text(this.font, shortfall, x + 20 + this.font.width(clipped) + 4, y, INK_FAINT, false);
            }
            y += 18;
        }

        if (recipe.requiresGrinding() && y + LINE_HEIGHT <= contentBottom) {
            boolean hasMortar = this.minecraft != null && this.minecraft.player != null
                    && this.minecraft.player.getInventory().contains(
                            held -> held.getItem() instanceof ItemMortarPestle);
            drawWrapped(graphics, Component.translatable("alchemia.guide.needs_mortar"), x, y, RIGHT_PAGE_W,
                    hasMortar ? INK_FAINT : INK_SHORT);
        }

        for (FormattedCharSequence line : hint) {
            graphics.text(this.font, line, x, hintY, INK_FAINT, false);
            hintY += LINE_HEIGHT;
        }


        extractDeleteButton(graphics, mouseX, mouseY);
    }

    private int deleteX() {
        return detailX();
    }

    private int deleteY() {
        return pageBottom() - DELETE_H;
    }

    private boolean overDelete(double mouseX, double mouseY) {
        if (selected == null) return false;
        return mouseX >= deleteX() && mouseX < deleteX() + DELETE_W
                && mouseY >= deleteY() && mouseY < deleteY() + DELETE_H;
    }

    private void extractDeleteButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = deleteX();
        int y = deleteY();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                overDelete(mouseX, mouseY) ? AlchemiaSprites.DELETE_BUTTON_HIGHLIGHTED : AlchemiaSprites.DELETE_BUTTON,
                x, y, DELETE_W, DELETE_H);

        Component label = Component.translatable("alchemia.guide.delete");
        graphics.text(this.font, label, x + (DELETE_W - this.font.width(label)) / 2, y + 4, INK_HEAD, false);
    }

    private int countHeld(ItemStack wanted) {
        if (this.minecraft == null || this.minecraft.player == null) return 0;
        int found = 0;
        for (ItemStack stack : this.minecraft.player.getInventory()) {
            if (stack.getItem() == wanted.getItem()) {
                found += stack.getCount();
            }
        }
        return found;
    }
}
