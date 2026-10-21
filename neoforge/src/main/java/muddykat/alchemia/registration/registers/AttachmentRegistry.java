package muddykat.alchemia.registration.registers;

import com.mojang.serialization.Codec;
import muddykat.alchemia.Alchemia;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class AttachmentRegistry {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Alchemia.MODID);

    public static final Supplier<AttachmentType<List<String>>> PRIMER_CHAPTERS = ATTACHMENT_TYPES.register("primer_chapters",
            () -> AttachmentType.<List<String>>builder(() -> List.of())
                    .serialize(Codec.STRING.listOf().fieldOf("chapters"))
                    .copyOnDeath()
                    .build());
}
