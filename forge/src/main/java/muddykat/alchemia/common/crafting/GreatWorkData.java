package muddykat.alchemia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.Alchemia;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;

public class GreatWorkData extends SavedData {

    public static final String NAME = Alchemia.MODID + "_great_work";

    public static final Codec<GreatWorkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("version", 0).forGetter(data -> data.version),
            MagnumOpus.Stage.CODEC.listOf().optionalFieldOf("stages", List.of()).forGetter(data -> data.stages)
    ).apply(instance, GreatWorkData::new));

    private int version;
    private List<MagnumOpus.Stage> stages;

    public GreatWorkData() {
        this(0, List.of());
    }

    private GreatWorkData(int version, List<MagnumOpus.Stage> stages) {
        this.version = version;
        this.stages = List.copyOf(stages);
    }

    public static GreatWorkData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial(Alchemia.LOGGER::error).orElseGet(GreatWorkData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(Alchemia.LOGGER::error)
                .ifPresent(encoded -> {
                    if (encoded instanceof CompoundTag compound) tag.merge(compound);
                });
        return tag;
    }

    public List<MagnumOpus.Stage> stages() {
        return stages;
    }

    public boolean holds() {
        return version == GreatWorkGenerator.VERSION && stages.size() == MagnumOpus.CLASSIC.size();
    }

    public void store(List<MagnumOpus.Stage> generated) {
        version = GreatWorkGenerator.VERSION;
        stages = List.copyOf(generated);
        setDirty();
    }
}
