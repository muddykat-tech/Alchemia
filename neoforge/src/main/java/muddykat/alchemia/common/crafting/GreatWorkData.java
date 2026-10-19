package muddykat.alchemia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.Alchemia;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.List;

public class GreatWorkData extends SavedData {

    public static final Codec<GreatWorkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("version", 0).forGetter(data -> data.version),
            MagnumOpus.Stage.CODEC.listOf().optionalFieldOf("stages", List.of()).forGetter(data -> data.stages)
    ).apply(instance, GreatWorkData::new));

    public static final SavedDataType<GreatWorkData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Alchemia.MODID, "great_work"), GreatWorkData::new, CODEC);

    private int version;
    private List<MagnumOpus.Stage> stages;

    public GreatWorkData() {
        this(0, List.of());
    }

    private GreatWorkData(int version, List<MagnumOpus.Stage> stages) {
        this.version = version;
        this.stages = List.copyOf(stages);
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
