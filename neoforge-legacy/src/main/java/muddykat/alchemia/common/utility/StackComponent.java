package muddykat.alchemia.common.utility;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class StackComponent<T> {
    private final Supplier<DataComponentType<T>> type;

    private StackComponent(Supplier<DataComponentType<T>> type) {
        this.type = type;
    }

    public static <T> StackComponent<T> of(Supplier<DataComponentType<T>> type) {
        return new StackComponent<>(type);
    }

    public DataComponentType<T> type() {
        return type.get();
    }

    public boolean has(ItemStack stack) {
        return stack.has(type.get());
    }

    public T get(ItemStack stack) {
        return stack.get(type.get());
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        return stack.getOrDefault(type.get(), fallback);
    }

    public void set(ItemStack stack, T value) {
        if (value == null) {
            remove(stack);
            return;
        }
        stack.set(type.get(), value);
    }

    public void remove(ItemStack stack) {
        stack.remove(type.get());
    }
}
