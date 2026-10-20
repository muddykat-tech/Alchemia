package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.effect.FlameCloudEntity;
import muddykat.alchemia.common.effect.TeleportFieldEntity;
import muddykat.alchemia.common.effect.ReturnPortalEntity;
import muddykat.alchemia.common.effect.RiftEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Alchemia.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ReturnPortalEntity>> RETURN_PORTAL = ENTITY_TYPES.register("return_portal",
            id -> EntityType.Builder.<ReturnPortalEntity>of(ReturnPortalEntity::new, MobCategory.MISC)
                    .sized(1.6F, 2.4F)
                    .noSummon()
                    .fireImmune()
                    .noLootTable()
                    .clientTrackingRange(8)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<FlameCloudEntity>> FLAME_CLOUD = ENTITY_TYPES.register("flame_cloud",
            id -> EntityType.Builder.<FlameCloudEntity>of(FlameCloudEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.5F)
                    .noSummon()
                    .fireImmune()
                    .noLootTable()
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<TeleportFieldEntity>> TELEPORT_FIELD = ENTITY_TYPES.register("teleport_field",
            id -> EntityType.Builder.<TeleportFieldEntity>of(TeleportFieldEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.25F)
                    .noSummon()
                    .noSave()
                    .fireImmune()
                    .noLootTable()
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<RiftEntity>> RIFT = ENTITY_TYPES.register("rift",
            id -> EntityType.Builder.<RiftEntity>of(RiftEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .noSave()
                    .fireImmune()
                    .noLootTable()
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
}
