package muddykat.alchemia.client.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.effect.TreasureLoot;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class TreasureVision {

    static final ResourceLocation FIREFLY = SenseGeometry.WHITE;
    static final float FIREFLY_UV = 1.0F;
    private static final int MAX_TREASURE = 384;
    private static final int REFRESH = 4;
    private static final int FADE_IN = 24;
    private static final int FADE_OUT = 30;
    private static final float OUTLINE_FROM = 0.5F;
    public static final int TREASURE_RGB = 0xFFD23A;
    private static final int GLINT_RGB = 0xFFF4B0;
    private static final int CART_RGB = 0xFFF4B0;
    private static final int MOTES = 2;
    private static final int BOX_ALPHA = 46;
    private static final int HALO_ALPHA = 60;
    private static final int MOTE_ALPHA = 220;
    private static final int EXPOSE_IN = 6;
    private static final int EXPOSE_OUT = 6;
    private static final double LOOK_OUTER = Math.cos(Math.toRadians(22.0));
    private static final double LOOK_INNER = Math.cos(Math.toRadians(8.0));
    private static final Direction[] SIDES = Direction.values();

    private record Treasure(BlockPos pos, AABB box, int open, int colour, long vein) {
    }

    private record Marked(int id, boolean hidden) {
    }

    private static final SenseTargets TARGETS = new SenseTargets(Configuration::treasureBlocks, Configuration::treasureEntities);
    private static final SenseScanner SCANNER = new SenseScanner(TARGETS::matches);
    private static final SenseFade<Long, Treasure> TREASURE = new SenseFade<>(FADE_IN, FADE_OUT);
    private static final SenseFade<Integer, Marked> CREATURES = new SenseFade<>(FADE_IN, FADE_OUT);
    private static final SenseFade<Long, Boolean> EXPOSED = new SenseFade<>(EXPOSE_IN, EXPOSE_OUT);
    private static final SenseFade<Integer, Integer> CARTS = new SenseFade<>(FADE_IN, FADE_OUT);
    private static int range;
    private static int ticks;
    private static Set<Long> loot = Set.of();
    private static Set<Integer> lootCarts = Set.of();

    private TreasureVision() {
    }

    public static void loot(List<Long> positions, List<Integer> carts) {
        loot = Set.copyOf(positions);
        lootCarts = Set.copyOf(carts);
    }

    private static boolean wanted(ClientLevel level, BlockPos pos) {
        if (!TARGETS.matches(level.getBlockState(pos))) return false;
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof RandomizableContainer || entity instanceof BrushableBlockEntity) return loot.contains(pos.asLong());
        return true;
    }

    public static boolean outlined(net.minecraft.world.entity.Entity entity) {
        return CREATURES.fade(entity.getId(), 1.0F) >= OUTLINE_FROM;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        MobEffectInstance effect = player == null ? null : player.getEffect(MobEffectRegistry.TREASURE_VISION);
        if (level == null) {
            SCANNER.clear();
            TREASURE.clear();
            CREATURES.clear();
            EXPOSED.clear();
            CARTS.clear();
            range = 0;
            return;
        }
        if (effect == null) {
            if (range != 0) {
                SCANNER.clear();
                TREASURE.refresh(Map.of());
                CREATURES.refresh(Map.of());
                CARTS.refresh(Map.of());
            }
            range = 0;
            TREASURE.tick();
            CREATURES.tick();
            EXPOSED.tick();
            CARTS.tick();
            return;
        }
        range = TreasureLoot.range(effect.getAmplifier());
        TARGETS.refresh();
        SCANNER.tick(level, player.blockPosition(), range);
        TREASURE.tick();
        CREATURES.tick();
        EXPOSED.tick();
        CARTS.tick();
        if (ticks++ % REFRESH != 0) return;

        Vec3 eye = player.getEyePosition();
        Map<Integer, Marked> creatures = new HashMap<>();
        if (TARGETS.anyEntities()) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
                    entity -> entity != player && entity.isAlive() && entity.distanceToSqr(player) <= (double) range * range && TARGETS.matches(entity))) {
                boolean hidden = SenseGeometry.wallDepth(level, eye, BlockPos.containing(entity.getBoundingBox().getCenter()), 0) > 0;
                creatures.put(entity.getId(), new Marked(entity.getId(), hidden));
            }
        }
        CREATURES.refresh(creatures);

        Map<Integer, Integer> carts = new HashMap<>();
        for (int id : lootCarts) {
            Entity cart = level.getEntity(id);
            if (cart != null && cart.distanceToSqr(player) <= (double) range * range) carts.put(id, id);
        }
        CARTS.refresh(carts);

        List<SenseScanner.Hit> hits = new ArrayList<>(SCANNER.found());
        hits.sort((a, b) -> Double.compare(a.pos().distToCenterSqr(eye), b.pos().distToCenterSqr(eye)));
        Map<Long, SenseScanner.Hit> kept = new HashMap<>();
        for (SenseScanner.Hit hit : hits) {
            if (kept.size() >= MAX_TREASURE) break;
            if (hit.pos().distToCenterSqr(eye) > (double) range * range || !wanted(level, hit.pos())) continue;
            kept.put(hit.pos().asLong(), hit);
        }
        Map<Long, Long> veins = veins(kept.keySet());
        Map<Long, Treasure> seen = new HashMap<>();
        for (SenseScanner.Hit hit : kept.values()) {
            VoxelShape shape = hit.state().getShape(level, hit.pos());
            boolean cube = Block.isShapeFullBlock(shape);
            AABB box = (shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds()).move(hit.pos());
            int open = 0;
            for (Direction side : SIDES) {
                if (!cube || !kept.containsKey(hit.pos().relative(side).asLong()) || !full(level, hit.pos().relative(side))) open |= 1 << side.ordinal();
            }
            int colour = TARGETS.colour(hit.state());
            seen.put(hit.pos().asLong(), new Treasure(hit.pos(), box, open, colour == SenseTargets.NO_COLOUR ? TREASURE_RGB : colour,
                    veins.get(hit.pos().asLong())));
        }
        TREASURE.refresh(seen);

        Map<Long, Boolean> exposed = new HashMap<>();
        for (SenseScanner.Hit hit : kept.values()) {
            if (SenseGeometry.wallDepth(level, eye, hit.pos(), 0) == 0) exposed.put(hit.pos().asLong(), Boolean.TRUE);
        }
        EXPOSED.refresh(exposed);
    }

    @SubscribeEvent
    public static void onStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        draw(event, buffers);
        buffers.endBatch();
    }

    private static void draw(RenderLevelStageEvent event, MultiBufferSource buffers) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || TREASURE.isEmpty() && CREATURES.isEmpty() && CARTS.isEmpty()) return;

        Vec3 camera = event.getCamera().getPosition();
        Quaternionf facing = event.getCamera().rotation();
        Vector3f look = facing.transform(new Vector3f(0.0F, 0.0F, -1.0F));
        double seconds = Util.getMillis() / 1000.0;
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        Map<Long, Float> shownVeins = new HashMap<>();
        TREASURE.forEach(partial, (treasure, fade) -> shownVeins.merge(treasure.vein(), revealed(treasure, camera, look, partial), Math::min));

        if (Configuration.treasureHighlights()) Submit.geometry(buffers, event.getPoseStack(), SenseShader.seeThrough(), (pose, buffer) ->
                TREASURE.forEach(partial, (treasure, fade) -> {
                    float shown = fade * shownVeins.getOrDefault(treasure.vein(), 1.0F);
                    if (shown <= 0.0F) return;
                    SenseGeometry.shell(pose, buffer, treasure.box(), treasure.open(), camera, ARGB.color(Math.round(BOX_ALPHA * shown), treasure.colour()));
                }));
        Submit.geometry(buffers, event.getPoseStack(), SenseShader.glow(SenseGeometry.GLOW), (pose, buffer) -> {
            TREASURE.forEach(partial, (treasure, fade) -> {
                float shown = fade * shownVeins.getOrDefault(treasure.vein(), 1.0F);
                for (int mote = 0; mote < MOTES; mote++) {
                    double blink = blink(treasure.pos().asLong(), mote, seconds);
                    if (blink <= 0.02) continue;
                    SenseGeometry.billboard(pose, buffer, motePosition(Vec3.atCenterOf(treasure.pos()), treasure.pos().asLong(), mote, seconds).subtract(camera),
                            facing, 0.16F, 0.16F, ARGB.color((int) Math.round(HALO_ALPHA * blink * shown), treasure.colour()));
                }
            });
            CARTS.forEach(partial, (id, fade) -> {
                if (!(level.getEntity(id) instanceof Entity cart)) return;
                Vec3 at = cart.getPosition(partial).add(0.0, 0.6, 0.0);
                for (int mote = 0; mote < MOTES; mote++) {
                    double blink = blink(id, mote, seconds);
                    if (blink <= 0.02) continue;
                    SenseGeometry.billboard(pose, buffer, motePosition(at, id, mote, seconds).subtract(camera), facing, 0.16F, 0.16F,
                            ARGB.color((int) Math.round(HALO_ALPHA * blink * fade), CART_RGB));
                }
            });
            CREATURES.forEach(partial, (marked, fade) -> {
                if (!marked.hidden() || !(level.getEntity(marked.id()) instanceof LivingEntity entity)) return;
                AABB bounds = entity.getBoundingBox();
                Vec3 centre = entity.getPosition(partial).add(0.0, bounds.getYsize() / 2.0, 0.0).subtract(camera);
                SenseGeometry.billboard(pose, buffer, centre, facing, (float) (bounds.getXsize() * 0.9 + 0.3),
                        (float) (bounds.getYsize() * 0.6 + 0.2), ARGB.color(Math.round(160 * fade), TREASURE_RGB));
            });
        });
        Submit.geometry(buffers, event.getPoseStack(), SenseShader.glow(FIREFLY), (pose, buffer) -> {
            TREASURE.forEach(partial, (treasure, fade) -> {
                float shown = fade * shownVeins.getOrDefault(treasure.vein(), 1.0F);
                for (int mote = 0; mote < MOTES; mote++) {
                    double blink = blink(treasure.pos().asLong(), mote, seconds);
                    if (blink <= 0.02) continue;
                    SenseGeometry.billboard(pose, buffer, motePosition(Vec3.atCenterOf(treasure.pos()), treasure.pos().asLong(), mote, seconds).subtract(camera),
                            facing, 0.045F, 0.045F, ARGB.color((int) Math.round(MOTE_ALPHA * blink * shown), glint(treasure.colour())), FIREFLY_UV, FIREFLY_UV);
                }
            });
            CARTS.forEach(partial, (id, fade) -> {
                if (!(level.getEntity(id) instanceof Entity cart)) return;
                Vec3 at = cart.getPosition(partial).add(0.0, 0.6, 0.0);
                for (int mote = 0; mote < MOTES; mote++) {
                    double blink = blink(id, mote, seconds);
                    if (blink <= 0.02) continue;
                    SenseGeometry.billboard(pose, buffer, motePosition(at, id, mote, seconds).subtract(camera), facing, 0.045F, 0.045F,
                            ARGB.color((int) Math.round(MOTE_ALPHA * blink * fade), glint(CART_RGB)), FIREFLY_UV, FIREFLY_UV);
                }
            });
        });
    }

    private static float revealed(Treasure treasure, Vec3 camera, Vector3f look, float partial) {
        float exposed = EXPOSED.fade(treasure.pos().asLong(), partial);
        if (exposed <= 0.0F) return 1.0F;
        Vec3 toward = Vec3.atCenterOf(treasure.pos()).subtract(camera);
        double length = toward.length();
        if (length < 1.0E-4) return 1.0F - exposed;
        double aim = (toward.x * look.x + toward.y * look.y + toward.z * look.z) / length;
        double gaze = Math.clamp((aim - LOOK_OUTER) / (LOOK_INNER - LOOK_OUTER), 0.0, 1.0);
        gaze = gaze * gaze * (3.0 - 2.0 * gaze);
        return (float) (1.0 - exposed * gaze);
    }

    private static Map<Long, Long> veins(Set<Long> members) {
        Map<Long, Long> vein = new HashMap<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        List<Long> component = new ArrayList<>();
        for (long start : members) {
            if (vein.containsKey(start)) continue;
            component.clear();
            vein.put(start, start);
            queue.add(start);
            long smallest = start;
            while (!queue.isEmpty()) {
                long current = queue.poll();
                component.add(current);
                smallest = Math.min(smallest, current);
                BlockPos at = BlockPos.of(current);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            long next = BlockPos.asLong(at.getX() + dx, at.getY() + dy, at.getZ() + dz);
                            if (members.contains(next) && !vein.containsKey(next)) {
                                vein.put(next, start);
                                queue.add(next);
                            }
                        }
                    }
                }
            }
            for (long member : component) vein.put(member, smallest);
        }
        return vein;
    }

    private static int glint(int colour) {
        return ARGB.srgbLerp(0.35F, colour, GLINT_RGB);
    }

    private static boolean full(ClientLevel level, BlockPos pos) {
        return Block.isShapeFullBlock(level.getBlockState(pos).getShape(level, pos));
    }

    private static double seed(long key, int mote) {
        long hash = key * 0x9E3779B97F4A7C15L + mote * 0xC2B2AE3D27D4EB4FL;
        hash ^= hash >>> 31;
        return (hash & 0xFFFF) / 65536.0 * Math.PI * 2.0;
    }

    static Vec3 motePosition(Vec3 centre, long key, int mote, double seconds) {
        double phase = seed(key, mote);
        double x = 0.42 * Math.sin(seconds * 0.45 + phase) + 0.12 * Math.sin(seconds * 1.3 + phase * 2.0);
        double y = 0.36 * Math.sin(seconds * 0.38 + phase * 1.7) + 0.1 * Math.cos(seconds * 1.1 + phase);
        double z = 0.42 * Math.cos(seconds * 0.41 + phase * 0.6) + 0.12 * Math.cos(seconds * 1.2 + phase * 3.0);
        return centre.add(x, y, z);
    }

    static double blink(long key, int mote, double seconds) {
        double wave = Math.sin(seconds * (0.9 + 0.25 * mote) + seed(key, mote) * 3.0);
        double lit = Math.max(0.0, (wave + 0.2) / 1.2);
        return lit * lit * (3.0 - 2.0 * lit);
    }
}
