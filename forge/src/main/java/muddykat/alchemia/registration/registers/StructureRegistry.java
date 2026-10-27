package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.structure.ForgottenShackStructure;
import muddykat.alchemia.common.world.structure.ShackPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class StructureRegistry {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Alchemia.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Alchemia.MODID);

    public static final RegistryObject<StructureType<ForgottenShackStructure>> FORGOTTEN_SHACK =
            STRUCTURE_TYPES.register("forgotten_alchemy_shack", () -> () -> ForgottenShackStructure.CODEC);

    public static final RegistryObject<StructurePieceType> SHACK_PIECE =
            STRUCTURE_PIECES.register("forgotten_alchemy_shack", () -> (StructurePieceType.ContextlessType) ShackPiece::new);
}
