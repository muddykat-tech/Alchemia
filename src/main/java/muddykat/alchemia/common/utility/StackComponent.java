package muddykat.alchemia.common.utility;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class StackComponent<T> {
    private final String key;
    private final Codec<T> codec;

    private StackComponent(String key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
    }

    public static <T> StackComponent<T> of(String key, Codec<T> codec) {
        return new StackComponent<>(key, codec);
    }

    public String key() {
        return key;
    }

    public boolean has(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key);
    }

    public T get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        Tag stored = tag.get(key);
        if (stored == null) return null;
        return codec.parse(NbtOps.INSTANCE, stored).result().orElse(null);
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        T value = get(stack);
        return value != null ? value : fallback;
    }

    public void set(ItemStack stack, T value) {
        if (value == null) {
            remove(stack);
            return;
        }
        codec.encodeStart(NbtOps.INSTANCE, value).result()
                .ifPresent(encoded -> stack.getOrCreateTag().put(key, encoded));
    }

    public void remove(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(key);
        if (tag.isEmpty()) stack.setTag(null);
    }
}
