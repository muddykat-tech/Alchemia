package muddykat.alchemia.client.effect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

public final class SenseFade<K, V> {

    private static final class Mark<V> {
        private V value;
        private float now;
        private float before;
        private boolean seen;
    }

    private final Map<K, Mark<V>> marks = new HashMap<>();
    private final float rise;
    private final float fall;

    public SenseFade(int riseTicks, int fallTicks) {
        this.rise = 1.0F / riseTicks;
        this.fall = 1.0F / fallTicks;
    }

    public void refresh(Map<K, V> present) {
        for (Mark<V> mark : marks.values()) mark.seen = false;
        for (Map.Entry<K, V> entry : present.entrySet()) {
            Mark<V> mark = marks.computeIfAbsent(entry.getKey(), key -> new Mark<>());
            mark.value = entry.getValue();
            mark.seen = true;
        }
    }

    public void tick() {
        Iterator<Mark<V>> iterator = marks.values().iterator();
        while (iterator.hasNext()) {
            Mark<V> mark = iterator.next();
            mark.before = mark.now;
            mark.now = mark.seen ? Math.min(1.0F, mark.now + rise) : Math.max(0.0F, mark.now - fall);
            if (!mark.seen && mark.now <= 0.0F && mark.before <= 0.0F) iterator.remove();
        }
    }

    public float fade(K key, float partialTicks) {
        Mark<V> mark = marks.get(key);
        return mark == null ? 0.0F : ease(mark.before + (mark.now - mark.before) * partialTicks);
    }

    public void forEach(float partialTicks, BiConsumer<V, Float> action) {
        for (Mark<V> mark : marks.values()) {
            float fade = ease(mark.before + (mark.now - mark.before) * partialTicks);
            if (fade > 0.0F) action.accept(mark.value, fade);
        }
    }

    public boolean isEmpty() {
        return marks.isEmpty();
    }

    public void clear() {
        marks.clear();
    }

    private static float ease(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}
