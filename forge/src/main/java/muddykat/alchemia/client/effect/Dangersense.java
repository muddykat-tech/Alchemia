package muddykat.alchemia.client.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class Dangersense {

    private static final int RANGE_PER_LEVEL = 16;
    private static final int MAX_TRAPS = 256;
    private static final int DEPTH_REFRESH = 4;
    private static final int FADE_IN = 8;
    private static final int FADE_OUT = 12;
    private static final float OVERLAY_FROM = 0.5F;
    private static final int DANGER_RGB = 0xE0201C;
    private static final int GLINT_RGB = 0xFF6A50;
    private static final int MOTES = 4;
    private static final int HALO_ALPHA = 70;
    private static final int MOTE_ALPHA = 220;
    private static final double VIGNETTE_PEAK = 15.0;
    private static final int VIGNETTE_STRIPS = 24;
    private static final int VIGNETTE_DEPTH = 48;

    private record Trap(BlockPos pos, AABB box, int depth) {
    }

    private record Marked(int id, int depth) {
    }

    private static final SenseTargets TARGETS = new SenseTargets(Configuration::dangerBlocks, Configuration::dangerEntities);
    private static final SenseScanner SCANNER = new SenseScanner(TARGETS::matches);
    private static final SenseFade<Long, Trap> TRAPS = new SenseFade<>(FADE_IN, FADE_OUT);
    private static final SenseFade<Integer, Marked> MOBS = new SenseFade<>(FADE_IN, FADE_OUT);
    private static int range;
    private static double pressure;
    private static int ticks;

    private Dangersense() {
    }

    public static boolean threatens(LivingEntity entity) {
        return MOBS.fade(entity.getId(), 1.0F) >= OVERLAY_FROM;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        MobEffectInstance effect = player == null ? null : player.getEffect(MobEffectRegistry.DANGERSENSE.get());
        if (level == null) {
            SCANNER.clear();
            TRAPS.clear();
            MOBS.clear();
            range = 0;
            pressure = 0.0;
            return;
        }
        if (effect == null) {
            if (range != 0) {
                SCANNER.clear();
                TRAPS.refresh(Map.of());
                MOBS.refresh(Map.of());
            }
            range = 0;
            pressure *= 0.85;
            TRAPS.tick();
            MOBS.tick();
            return;
        }
        range = RANGE_PER_LEVEL * (effect.getAmplifier() + 1);
        TARGETS.refresh();
        SCANNER.tick(level, player.blockPosition(), range);
        TRAPS.tick();
        MOBS.tick();
        if (ticks++ % DEPTH_REFRESH != 0) return;

        Vec3 eye = player.getEyePosition();
        double nearest = range;
        Map<Integer, Marked> mobs = new HashMap<>();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
                entity -> entity != player && entity.isAlive() && entity.distanceToSqr(player) <= (double) range * range && TARGETS.matches(entity))) {
            mobs.put(entity.getId(), new Marked(entity.getId(), SenseGeometry.wallDepth(level, eye, BlockPos.containing(entity.getBoundingBox().getCenter()), range)));
            nearest = Math.min(nearest, entity.distanceTo(player));
        }
        MOBS.refresh(mobs);

        List<SenseScanner.Hit> hits = new ArrayList<>(SCANNER.found());
        hits.sort((a, b) -> Double.compare(a.pos().distToCenterSqr(eye), b.pos().distToCenterSqr(eye)));
        Map<Long, Trap> seen = new HashMap<>();
        for (SenseScanner.Hit hit : hits) {
            if (seen.size() >= MAX_TRAPS) break;
            if (!TARGETS.matches(level.getBlockState(hit.pos()))) continue;
            int depth = SenseGeometry.wallDepth(level, eye, hit.pos(), range);
            VoxelShape shape = hit.state().getShape(level, hit.pos());
            AABB box = (shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds()).move(hit.pos()).inflate(0.02);
            seen.put(hit.pos().asLong(), new Trap(hit.pos(), box, depth));
            nearest = Math.min(nearest, Math.sqrt(hit.pos().distToCenterSqr(player.position())));
        }
        TRAPS.refresh(seen);
        pressure += (1.0 - nearest / range - pressure) * 0.5;
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
        if (level == null || TRAPS.isEmpty() && MOBS.isEmpty()) return;

        Vec3 camera = event.getCamera().getPosition();
        Quaternionf facing = event.getCamera().rotation();
        double pulse = 0.75 + 0.25 * Math.sin(Util.getMillis() / 1000.0 * 4.0);
        float partial = Minecraft.getInstance().getFrameTime();

        boolean boxes = Configuration.dangerHighlights();
        double seconds = Util.getMillis() / 1000.0;
        if (boxes) Submit.geometry(buffers, event.getPoseStack(), RenderType.debugQuads(), (pose, buffer) -> TRAPS.forEach(partial,
                (trap, fade) -> {
                    if (trap.depth() == 0) SenseGeometry.plainBox(pose, buffer, trap.box().move(-camera.x, -camera.y, -camera.z), shade(110, pulse * fade, 0));
                }));
        Submit.geometry(buffers, event.getPoseStack(), SenseShader.glow(SenseGeometry.GLOW), (pose, buffer) ->
                TRAPS.forEach(partial, (trap, fade) -> {
                    for (int mote = 0; mote < MOTES; mote++) {
                        double blink = TreasureVision.blink(trap.pos().asLong(), mote, seconds);
                        if (blink <= 0.02) continue;
                        SenseGeometry.billboard(pose, buffer, TreasureVision.motePosition(Vec3.atCenterOf(trap.pos()), trap.pos().asLong(), mote, seconds)
                                .subtract(camera), facing, 0.16F, 0.16F, ARGB.color((int) Math.round(HALO_ALPHA * blink * fade), DANGER_RGB));
                    }
                }));
        Submit.geometry(buffers, event.getPoseStack(), SenseShader.glow(TreasureVision.FIREFLY), (pose, buffer) ->
                TRAPS.forEach(partial, (trap, fade) -> {
                    for (int mote = 0; mote < MOTES; mote++) {
                        double blink = TreasureVision.blink(trap.pos().asLong(), mote, seconds);
                        if (blink <= 0.02) continue;
                        SenseGeometry.billboard(pose, buffer, TreasureVision.motePosition(Vec3.atCenterOf(trap.pos()), trap.pos().asLong(), mote, seconds)
                                .subtract(camera), facing, 0.045F, 0.045F, ARGB.color((int) Math.round(MOTE_ALPHA * blink * fade), GLINT_RGB),
                                TreasureVision.FIREFLY_UV, TreasureVision.FIREFLY_UV);
                    }
                }));
        if (boxes) Submit.geometry(buffers, event.getPoseStack(), SenseShader.seeThrough(), (pose, buffer) ->
                TRAPS.forEach(partial, (trap, fade) -> {
                    if (trap.depth() > 0) SenseGeometry.shell(pose, buffer, trap.box(), SenseGeometry.ALL_SIDES, camera, shade(110, pulse * fade, trap.depth()));
                }));
        Submit.geometry(buffers, event.getPoseStack(), SenseShader.glow(SenseGeometry.GLOW), (pose, buffer) ->
                MOBS.forEach(partial, (mob, fade) -> {
                    if (mob.depth() == 0 || !(level.getEntity(mob.id()) instanceof LivingEntity entity)) return;
                    AABB bounds = entity.getBoundingBox();
                    Vec3 centre = entity.getPosition(partial).add(0.0, bounds.getYsize() / 2.0, 0.0).subtract(camera);
                    SenseGeometry.billboard(pose, buffer, centre, facing, (float) (bounds.getXsize() * 0.9 + 0.3),
                            (float) (bounds.getYsize() * 0.6 + 0.2), shade(200, pulse * fade, mob.depth()));
                }));
    }

    private static int shade(int alpha, double strength, int depth) {
        return ARGB.color((int) Math.round(alpha * strength * SenseGeometry.throughWalls(depth)), DANGER_RGB);
    }

    public static void renderVignette(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (pressure <= 0.01) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int peak = (int) Math.round(VIGNETTE_PEAK * Math.min(1.0, pressure) * (0.8 + 0.2 * Math.sin(Util.getMillis() / 180.0)));
        if (peak <= 0) return;
        int edge = ARGB.color(peak, DANGER_RGB);
        int clear = ARGB.color(0, DANGER_RGB);
        graphics.fillGradient(0, 0, width, VIGNETTE_DEPTH, edge, clear);
        graphics.fillGradient(0, height - VIGNETTE_DEPTH, width, height, clear, edge);
        int strip = Math.max(1, VIGNETTE_DEPTH / VIGNETTE_STRIPS);
        for (int index = 0; index < VIGNETTE_STRIPS; index++) {
            int colour = ARGB.color(peak * (VIGNETTE_STRIPS - index) / VIGNETTE_STRIPS, DANGER_RGB);
            graphics.fill(index * strip, 0, (index + 1) * strip, height, colour);
            graphics.fill(width - (index + 1) * strip, 0, width - index * strip, height, colour);
        }
    }
}
