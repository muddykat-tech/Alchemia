package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class PrimerBook {

    static final String FOLDER = "guide";
    static final String FALLBACK_LANGUAGE = "en_us";

    enum Align {
        LEFT, CENTER, RIGHT
    }

    enum Shape {
        WIDE(128, 72),
        SMALL(96, 54),
        SQUARE(72, 72),
        PORTRAIT(54, 72);

        final int width;
        final int height;

        Shape(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    enum PhotoFilter {
        NONE, INKED, INKED_DETAILED
    }

    static final float MAX_ANGLE = 15.0F;

    sealed interface Block permits Text, Photo, Items, Path, Crafts, Rule, Break {
    }

    record Text(String line) implements Block {
    }

    record Photo(Shape shape, Align align, PhotoFilter filter, float angle, List<String> names, List<String> titles) implements Block {
        String title(int index) {
            return index < titles.size() ? titles.get(index) : "";
        }
    }

    record Items(Align align, List<String> slots) implements Block {
    }

    record Path(String itemName, boolean compact, Align align, boolean grind) implements Block {
    }

    record Crafts(List<GuidePrimer.CraftSource> crafts, boolean compact) implements Block {
    }

    record Rule() implements Block {
    }

    record Break() implements Block {
    }

    enum Gate {
        ADVANCEMENT, HOLD
    }

    record Unlock(Gate gate, Identifier id) {
    }

    record Section(String key, String title, String label, List<Block> blocks, List<Unlock> unlocks) {
    }

    private static final Pattern TOKEN = Pattern.compile("([A-Za-z]+=)?\"([^\"]*)\"|(\\S+)");
    private static final Map<String, Section> SECTIONS = new HashMap<>();
    private static String language = "";

    private PrimerBook() {
    }

    static void clear() {
        SECTIONS.clear();
    }

    static String language() {
        return Minecraft.getInstance().getLanguageManager().getSelected();
    }

    static Section section(String key) {
        String current = language();
        if (!current.equals(language)) {
            SECTIONS.clear();
            language = current;
        }
        return SECTIONS.computeIfAbsent(key, missing -> load(current, missing));
    }

    private static Section load(String current, String key) {
        Optional<String> file = read(current, key);
        if (file.isEmpty() && !current.equals(FALLBACK_LANGUAGE)) file = read(FALLBACK_LANGUAGE, key);
        if (file.isEmpty()) return new Section(key, key, key, List.of(new Text("[Missing guide page: " + key + "]")), List.of());
        return parse(key, file.get());
    }

    private static Optional<String> read(String lang, String key) {
        Identifier id = Identifier.fromNamespaceAndPath(Alchemia.MODID, FOLDER + "/" + lang + "/" + key + ".txt");
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
        if (resource.isEmpty()) return Optional.empty();
        try (BufferedReader reader = resource.get().openAsReader()) {
            return Optional.of(reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException error) {
            Alchemia.LOGGER.warn("Could not read guide page {}", id, error);
            return Optional.empty();
        }
    }

    static Section parse(String key, String file) {
        String[] lines = file.replace("\r", "").split("\n", -1);
        String title = lines.length > 0 ? lines[0].strip() : key;
        String label = lines.length > 1 ? lines[1].strip() : "";
        List<Block> blocks = new ArrayList<>();
        List<Unlock> unlocks = new ArrayList<>();
        List<GuidePrimer.CraftSource> crafts = new ArrayList<>();
        boolean compact = false;
        for (int index = 2; index < lines.length; index++) {
            String line = lines[index].stripTrailing();
            boolean recipe = line.startsWith("@craft ");
            boolean manual = line.startsWith("@manual_craft ");
            List<String> craftTokens = recipe ? tokens(line.substring("@craft ".length()))
                    : manual ? tokens(line.substring("@manual_craft ".length())) : List.of();
            boolean lineCompact = craftTokens.stream().anyMatch(token -> token.equalsIgnoreCase("style=compact"));
            craftTokens = craftTokens.stream().filter(token -> !token.startsWith("style=")).toList();
            if (!crafts.isEmpty() && (!(recipe || manual) || lineCompact != compact)) {
                blocks.add(new Crafts(List.copyOf(crafts), compact));
                crafts.clear();
            }
            if (recipe || manual) compact = lineCompact;
            if (recipe) {
                for (String token : craftTokens) {
                    Identifier id = Identifier.tryParse(token);
                    if (id != null) crafts.add(new GuidePrimer.CraftRecipe(id));
                }
            } else if (manual) {
                GuidePrimer.Craft parsed = craft(craftTokens);
                if (parsed != null) crafts.add(parsed);
            } else if (line.equals("@unlock") || line.startsWith("@unlock ")) {
                Unlock unlock = unlock(key, tokens(line.substring("@unlock".length())));
                if (unlock != null) unlocks.add(unlock);
            } else if (line.startsWith("@")) {
                Block block = directive(tokens(line.substring(1)));
                if (block != null) blocks.add(block);
            } else {
                blocks.add(new Text(line.strip()));
            }
        }
        if (!crafts.isEmpty()) blocks.add(new Crafts(List.copyOf(crafts), compact));
        while (!blocks.isEmpty() && blocks.getLast() instanceof Text text && text.line().isEmpty()) blocks.removeLast();
        return new Section(key, title, label.isEmpty() ? title : label, List.copyOf(blocks), List.copyOf(unlocks));
    }

    private static List<String> tokens(String line) {
        List<String> tokens = new ArrayList<>();
        Matcher match = TOKEN.matcher(line);
        while (match.find()) tokens.add(match.group(3) != null ? match.group(3) : (match.group(1) == null ? "" : match.group(1)) + match.group(2));
        return tokens;
    }

    private static Block directive(List<String> tokens) {
        if (tokens.isEmpty()) return null;
        String name = tokens.getFirst().toLowerCase(Locale.ROOT);
        List<String> rest = tokens.subList(1, tokens.size());
        return switch (name) {
            case "photo" -> {
                Shape shape = Shape.WIDE;
                Align align = Align.CENTER;
                PhotoFilter filter = PhotoFilter.NONE;
                float angle = 0.0F;
                List<String> names = new ArrayList<>();
                List<String> titles = new ArrayList<>();
                for (String token : rest) {
                    String value = token.contains("=") ? token.substring(token.indexOf('=') + 1).toUpperCase(Locale.ROOT) : "";
                    if (token.startsWith("size=")) {
                        shape = parse(Shape.class, value, shape);
                    } else if (token.startsWith("align=")) {
                        align = parse(Align.class, value, align);
                    } else if (token.startsWith("filter=")) {
                        filter = parse(PhotoFilter.class, value, filter);
                    } else if (token.startsWith("title=")) {
                        for (String title : token.substring(6).split("\\|", -1)) titles.add(title.strip());
                    } else if (token.startsWith("angle=")) {
                        try {
                            angle = Math.max(-MAX_ANGLE, Math.min(MAX_ANGLE, Float.parseFloat(value)));
                        } catch (NumberFormatException ignored) {
                        }
                    } else {
                        names.add(token);
                    }
                }
                yield names.isEmpty() ? null : new Photo(shape, align, filter, angle, List.copyOf(names), List.copyOf(titles));
            }
            case "items" -> {
                Align align = Align.CENTER;
                List<String> slots = new ArrayList<>();
                for (String token : rest) {
                    String value = token.startsWith("align=") ? token.substring(6) : token;
                    Align named = parse(Align.class, value.toUpperCase(Locale.ROOT), null);
                    if (named != null && (token.startsWith("align=") || slots.isEmpty())) {
                        align = named;
                    } else {
                        slots.add(token);
                    }
                }
                yield slots.isEmpty() ? null : new Items(align, List.copyOf(slots));
            }
            case "path" -> {
                boolean compact = false;
                boolean grind = false;
                Align align = Align.CENTER;
                List<String> names = new ArrayList<>();
                for (String token : rest) {
                    if (token.startsWith("style=")) {
                        compact = token.substring(6).equalsIgnoreCase("compact");
                    } else if (token.startsWith("align=")) {
                        align = parse(Align.class, token.substring(6).toUpperCase(Locale.ROOT), align);
                    } else if (token.startsWith("grind=")) {
                        grind = token.substring(6).equalsIgnoreCase("true");
                    } else {
                        names.add(token);
                    }
                }
                yield names.size() == 1 ? new Path(names.getFirst(), compact, align, grind) : null;
            }
            case "break" -> new Rule();
            case "page" -> new Break();
            default -> {
                Alchemia.LOGGER.warn("Unknown guide directive @{}", name);
                yield null;
            }
        };
    }

    private static Unlock unlock(String key, List<String> tokens) {
        if (tokens.isEmpty()) return new Unlock(Gate.ADVANCEMENT, GuidePrimer.BUBBLE);
        Gate gate = parse(Gate.class, tokens.getFirst().toUpperCase(Locale.ROOT), null);
        Identifier id = tokens.size() > 1 ? Identifier.tryParse(tokens.get(1)) : null;
        if (gate == null || id == null) {
            Alchemia.LOGGER.warn("Guide page {} has an @unlock it can't read, so the chapter stays locked: {}", key, String.join(" ", tokens));
            return new Unlock(Gate.ADVANCEMENT, Identifier.fromNamespaceAndPath(Alchemia.MODID, "unreadable_unlock"));
        }
        return new Unlock(gate, id);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException error) {
            return fallback;
        }
    }

    private static GuidePrimer.Craft craft(List<String> tokens) {
        if (tokens.isEmpty()) return null;
        String result = tokens.getFirst();
        int count = 1;
        int star = result.indexOf('*');
        if (star > 0) {
            try {
                count = Integer.parseInt(result.substring(star + 1));
            } catch (NumberFormatException ignored) {
            }
            result = result.substring(0, star);
        }
        List<String> pattern = new ArrayList<>();
        Map<Character, Identifier> key = new HashMap<>();
        for (String token : tokens.subList(1, tokens.size())) {
            int equals = token.indexOf('=');
            if (equals == 1) {
                key.put(token.charAt(0), Identifier.parse(token.substring(2)));
            } else if (pattern.size() < 3) {
                pattern.add(token.length() > 3 ? token.substring(0, 3) : token);
            }
        }
        if (pattern.isEmpty()) return null;
        return new GuidePrimer.Craft(List.copyOf(pattern), Map.copyOf(key), Identifier.parse(result), count);
    }
}
