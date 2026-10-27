package muddykat.alchemia.common.utility;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public final class ValueOutput {

    private final CompoundTag tag;

    public ValueOutput(CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag tag() {
        return tag;
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(encoded -> tag.put(key, encoded));
    }

    public void putInt(String key, int value) {
        tag.putInt(key, value);
    }

    public void putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        tag.putString(key, value);
    }

    public ValueOutput child(String key) {
        CompoundTag child = new CompoundTag();
        tag.put(key, child);
        return new ValueOutput(child);
    }
}
