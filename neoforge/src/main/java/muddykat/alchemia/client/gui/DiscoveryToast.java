package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.guide.GuideNews;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryToast implements Toast {

    private static final Identifier BACKGROUND = sprite("discovery");
    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int WIDTH = 160;
    private static final int HEIGHT = 32;
    private static final int RIBBON_X = 143;
    private static final int RIBBON_W = 9;
    private static final int RIBBON_H = 26;
    private static final int SLOT_X = 4;
    private static final int SLOT_Y = 4;
    private static final int SLOT = 24;
    private static final int ITEM_INSET = 4;
    private static final int EFFECT_INSET = 3;
    private static final int EYE_U = 130;
    private static final int EYE = 12;
    private static final int SHEET = 256;
    private static final int TEXT_X = 36;
    private static final int TITLE_Y = 6;
    private static final int NAME_Y = 19;
    private static final int TEXT_RIGHT = 140;
    private static final float TITLE_SCALE = 0.75F;
    private static final int INK = 0xFF3B2A1E;
    private static final int INK_RED = 0xFF8E2A22;
    private static final int INK_SHADOW = 0xFFB7A27B;
    private static final long BASE_MS = 4000L;
    private static final long PER_ENTRY_MS = 1500L;
    private static final int MAX_PACED = 6;

    private final GuideNews.Kind kind;
    private final Identifier ribbon;
    private final List<GuideNews.Entry> entries = new ArrayList<>();
    private long lastChanged;
    private boolean changed;
    private int shown;
    private Toast.Visibility wanted = Toast.Visibility.HIDE;

    private DiscoveryToast(GuideNews.Kind kind) {
        this.kind = kind;
        this.ribbon = sprite("ribbon_" + switch (kind) {
            case INGREDIENT, REAGENT -> "ingredient";
            case EFFECT -> "effect";
            case SECRET -> "secret";
            case RECIPE -> "recipe";
            case OPUS -> "opus";
        });
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "toast/" + name);
    }

    public static void show(List<GuideNews.Entry> news) {
        Minecraft minecraft = Minecraft.getInstance();
#if MC_VER >= 26200
        boolean brewing = minecraft.gui.screen() instanceof AlchemicalScreen;
#else
        boolean brewing = minecraft.screen instanceof AlchemicalScreen;
#endif
        ToastManager toasts = toasts(minecraft);
        for (GuideNews.Entry entry : news) {
            if (brewing && entry.kind() != GuideNews.Kind.RECIPE) continue;

            DiscoveryToast toast = toasts.getToast(DiscoveryToast.class, entry.kind());
            if (toast == null) {
                toast = new DiscoveryToast(entry.kind());
                toasts.addToast(toast);
            }
            toast.add(entry);
        }
    }

    private static ToastManager toasts(Minecraft minecraft) {
#if MC_VER >= 26200
        return minecraft.gui.toastManager();
#else
        return minecraft.getToastManager();
#endif
    }

    private void add(GuideNews.Entry entry) {
        for (GuideNews.Entry existing : entries) {
            if (existing.key().equals(entry.key())) return;
        }
        entries.add(entry);
        changed = true;
    }

    @Override
    public Object getToken() {
        return kind;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return wanted;
    }

    @Override
    public @Nullable SoundEvent getSoundEvent() {
        return SoundEvents.BOOK_PAGE_TURN;
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        return HEIGHT;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        if (changed) {
            lastChanged = fullyVisibleForMs;
            changed = false;
        }
        if (entries.isEmpty()) {
            wanted = Toast.Visibility.HIDE;
            return;
        }

        double scale = manager.getNotificationDisplayTimeMultiplier();
        double lasting = (BASE_MS + PER_ENTRY_MS * Math.min(entries.size(), MAX_PACED)) * scale;
        wanted = fullyVisibleForMs - lastChanged >= lasting ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        double each = Math.max(1.0, lasting / entries.size());
        shown = (int) (fullyVisibleForMs / each % entries.size());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, WIDTH, HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ribbon, RIBBON_X, 0, RIBBON_W, RIBBON_H);
        if (entries.isEmpty()) return;

        GuideNews.Entry entry = entries.get(Math.min(shown, entries.size() - 1));
        icon(graphics, entry);
        title(graphics, font, Component.translatable("alchemia.toast." + kind.name().toLowerCase(java.util.Locale.ROOT)
                + (entries.size() > 1 ? ".many" : "")).getString());
        graphics.text(font, clip(font, entry.name().getString(), TEXT_RIGHT - TEXT_X), TEXT_X, NAME_Y, INK, false);

        if (entries.size() > 1) {
            String count = String.valueOf(entries.size());
            graphics.text(font, count, SLOT_X + SLOT - 1 - font.width(count), SLOT_Y + SLOT - 9, 0xFFFFFFFF, true);
        }
    }

    private void icon(GuiGraphicsExtractor graphics, GuideNews.Entry entry) {
        switch (entry.kind()) {
            case EFFECT -> {
                AlchemiaEffect recipe = RecipeDiscovery.byName(entry.key());
                if (recipe != null) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, recipe.sprite(),
                            SLOT_X + EFFECT_INSET, SLOT_Y + EFFECT_INSET, SLOT - 2 * EFFECT_INSET, SLOT - 2 * EFFECT_INSET);
                }
            }
            case SECRET -> graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, SLOT_X + (SLOT - EYE) / 2, SLOT_Y + (SLOT - EYE) / 2,
                    EYE_U, 0, EYE, EYE, SHEET, SHEET);
            default -> graphics.fakeItem(entry.icon(), SLOT_X + ITEM_INSET, SLOT_Y + ITEM_INSET);
        }
    }


    private void title(GuiGraphicsExtractor graphics, Font font, String text) {
        if (text.isEmpty()) return;
        String initial = text.substring(0, text.offsetByCodePoints(0, 1));
        String rest = text.substring(initial.length());
        lettered(graphics, font, text, TEXT_X + TITLE_SCALE, TITLE_Y + TITLE_SCALE, INK_SHADOW);
        lettered(graphics, font, initial, TEXT_X, TITLE_Y, INK_RED);
        lettered(graphics, font, rest, TEXT_X + font.width(Lettering.of(initial)) * TITLE_SCALE, TITLE_Y, INK);
    }

    private static void lettered(GuiGraphicsExtractor graphics, Font font, String text, float x, float capTop, int colour) {
        if (text.isEmpty()) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, capTop);
        graphics.pose().scale(TITLE_SCALE, TITLE_SCALE);
        graphics.text(font, Lettering.of(text), 0, Lettering.RISE, colour, false);
        graphics.pose().popMatrix();
    }

    private static String clip(Font font, String text, int width) {
        if (font.width(text) <= width) return text;
        return font.plainSubstrByWidth(text, width - font.width("...")) + "...";
    }
}
