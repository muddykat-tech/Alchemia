package muddykat.alchemia.client.effect;

#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
#else
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
#endif
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.effect.TeleportViewState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class TeleportView {

    public static final int SPOKES = 12;
    public static final int RINGS = 7;
    public static final float CORE = 0.5F;
    public static final float REACH = 0.8F;
    public static final float LINE_SCALE = 7.5F;
    public static final int HITS = 3;
    public static final int SHATTER = HITS - 1;
    public static final float[] HIT_AT = {3.0F, 15.0F};
    public static final float[] HIT_SHAKE = {1.1F, 2.0F, 3.4F};
    public static final float[] HIT_PITCH = {1.5F, 1.1F, 0.7F};
    public static final float[] HIT_VOLUME = {0.45F, 0.65F, 0.9F};
    public static final float SHATTER_DELAY = 2.0F;
    public static final float BREAK_SPREAD = 7.0F;
    public static final float FALL_TICKS = 10.0F;
    public static final float RETURN_SPREAD = 9.0F;
    public static final float RETURN_TICKS = 5.0F;
    public static final float FLASH_TICKS = 5.0F;
    public static final float UNHIT_FROM = 20.0F;
    public static final float FIZZLE_FROM = 1.0F;
    public static final float UNHIT_SPACING = 7.0F;
    public static final float SETTLE_TICKS = 8.0F;
    public static final float UNHIT_SHAKE = 0.35F;
    public static final Impacts IMPACTS = new Impacts(new float[]{0.32F, 0.6F, 1.2F}, new float[]{1.5F, 2.0F, 2.5F},
            new float[]{0.55F, 0.75F, 1.0F}, new float[]{0.55F, 0.85F, 1.3F}, new float[]{0.24F, 0.5F, 0.9F},
            new float[]{0.3F, 0.45F, 0.6F}, 1, 0.32F, 0.14F, 9.0F, 4.0F, 3.0F, false);

    private static final int REVEAL_WAIT = 60;
    private static final String DEPART = "teleport:depart";
    private static final String ARRIVE = "teleport:arrive";

    private static int phase = -1;
    private static int seenRevision = -1;
    private static float phaseTicks;
    private static float chargeTicks;
    private static float chargeRate = 1.0F;
    private static float breakTicks;
    private static float revealTicks;
    private static int waitTicks;
    private static int sounded;
    private static boolean broke;
    private static boolean revealing;
    private static int seed;
    private static @Nullable FracturePattern pattern;

    private record Layer(RenderPipeline pipeline, TextureSetup textureSetup, ScreenRectangle bounds, Consumer<VertexConsumer> builder)
            implements GuiElementRenderState {

        @Override
        public void buildVertices(VertexConsumer buffer) {
            builder.accept(buffer);
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return null;
        }
    }

    private TeleportView() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.isAlive()) {
            finish();
            seenRevision = TeleportViewState.revision();
            return;
        }

        int revision = TeleportViewState.revision();
        if (revision != seenRevision) {
            seenRevision = revision;
            enter(TeleportViewState.phase(), minecraft);
        }
        if (phase < 0) return;

        phaseTicks += 1.0F;
        if (phase == TeleportViewState.CHARGE) chargeTicks += chargeRate;
        if (phase == TeleportViewState.OPEN || phase == TeleportViewState.HOLD) breakTicks += 1.0F;
        if (phase == TeleportViewState.REVEAL) {
            if (!broke) {
                if (phaseTicks >= choreography(0.0F).end()) finish();
            } else if (!revealing) {
                if (sectionReady(minecraft) || ++waitTicks > REVEAL_WAIT) revealing = true;
            } else {
                revealTicks += 1.0F;
                if (revealTicks >= choreography(0.0F).end()) finish();
            }
        }
        if (phase >= 0) sound(minecraft);
    }

    private static void sound(Minecraft minecraft) {
        Screen screen = choreography(0.0F);
        for (int level = 0; level < HITS; level++) {
            int hit = 1 << level;
            if ((sounded & hit) == 0 && screen.age(level) >= 0.0F && !screen.reversing()) {
                sounded |= hit;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, HIT_PITCH[level], HIT_VOLUME[level]));
            }
            int unhit = 1 << (level + HITS);
            if ((sounded & unhit) == 0 && screen.reached(level) && screen.undone(level) >= 0.0F) {
                sounded |= unhit;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F - HIT_PITCH[level] * 0.5F,
                        HIT_VOLUME[level] * 0.8F));
            }
        }
    }

    private static void enter(int next, Minecraft minecraft) {
        phase = next;
        phaseTicks = 0.0F;
        switch (next) {
            case TeleportViewState.CHARGE -> {
                SceneCapture.release(DEPART);
                SceneCapture.release(ARRIVE);
                seed = (int) (minecraft.player.getUUID().getLeastSignificantBits() ^ minecraft.level.getGameTime());
                pattern = new FracturePattern(seed, SPOKES, RINGS, 1.0F, CORE);
                broke = false;
                chargeTicks = 0.0F;
                chargeRate = TeleportViewState.chargeRate();
                breakTicks = 0.0F;
                sounded = 0;
            }
            case TeleportViewState.OPEN -> {
                broke = true;
                breakTicks = 0.0F;
            }
            case TeleportViewState.REVEAL -> {
                revealing = false;
                revealTicks = 0.0F;
                waitTicks = 0;
            }
            default -> {
            }
        }
        if (pattern == null) phase = -1;
    }

    private static void finish() {
        phase = -1;
        pattern = null;
        SceneCapture.release(DEPART);
        SceneCapture.release(ARRIVE);
    }

    private static boolean sectionReady(Minecraft minecraft) {
        BlockPos at = minecraft.player.blockPosition();
#if MC_VER >= 26300
        return minecraft.levelRenderer.isSectionCompiledAndVisible(at, 0L);
#else
        return minecraft.levelRenderer.isSectionCompiledAndVisible(at);
#endif
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        FracturePattern current = pattern;
        if (phase < 0 || current == null) return;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int capture = captureSlot();

        RealityBreak.Mapping mapping = capture < 0 ? null : rest -> new float[]{rest.x / width, 1.0F - rest.y / height};
        Screen screen = choreography(partial);
        RealityBreak.Frame frame = RealityBreak.frame(current, plane(width, height, screen.severity()), screen, seed, mapping);
        ScreenRectangle bounds = new ScreenRectangle(0, 0, width, height);
        PoseStack.Pose pose = new PoseStack().last();
        if (frame.holes()) {
            graphics.nextStratum();
            graphics.submitGuiElementRenderState(new Layer(PortalShader.GUI_PIPELINE, PortalShader.guiStars(), bounds,
                    buffer -> frame.voids(pose, buffer, true)));
        }
        GpuTextureView view = capture < 0 ? null : SceneCapture.textureView(capture);
        if (frame.flights() && view != null) {
            graphics.nextStratum();
            graphics.submitGuiElementRenderState(new Layer(RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(view, SceneCapture.sampler()),
                    bounds, buffer -> frame.shards(pose, buffer, true)));
        }

        graphics.nextStratum();
        graphics.submitGuiElementRenderState(new Layer(RenderPipelines.GUI, TextureSetup.noTexture(), bounds, buffer -> {
            if (frame.flights()) frame.shards(pose, buffer, false);
            frame.lines(pose, buffer);
        }));
    }

    private static int captureSlot() {
        switch (phase) {
            case TeleportViewState.CHARGE -> {
                return live(DEPART);
            }
            case TeleportViewState.OPEN, TeleportViewState.HOLD -> {
                if (phase == TeleportViewState.OPEN && breakTicks < SHATTER_DELAY + 2.0F) return live(DEPART);
                if (phase == TeleportViewState.HOLD) SceneCapture.request();
                return SceneCapture.slotOf(DEPART);
            }
            case TeleportViewState.REVEAL -> {
                if (!broke) return live(DEPART);
                if (!revealing) {
                    SceneCapture.request();
                    return -1;
                }
                return live(ARRIVE);
            }
            default -> {
                return -1;
            }
        }
    }

    private static int live(String owner) {
        SceneCapture.request();
        int slot = SceneCapture.freeze(owner);
        return slot < 0 ? SceneCapture.slotOf(owner) : slot;
    }

    private static RealityBreak.Plane plane(int width, int height, float severity) {
        float radius = (float) Math.sqrt(width * width + height * height) * 0.5F / REACH;
        float lines = radius / LINE_SCALE * (0.6F + 0.25F * severity);
        return new RealityBreak.Plane(new Vector3f(width * 0.5F, height * 0.5F, 0.0F), new Vector3f(1.0F, 0.0F, 0.0F),
                new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.001F), radius, radius, lines);
    }

    private static Screen choreography(float partial) {
        float charge = phase == TeleportViewState.CHARGE ? chargeTicks + partial * chargeRate : chargeTicks;
        float broken = phase == TeleportViewState.OPEN || phase == TeleportViewState.HOLD ? breakTicks + partial : breakTicks;
        float reveal = revealing ? revealTicks + partial : 0.0F;
        return new Screen(phase, charge, broken, broke, revealing, reveal, phaseTicks + partial, seed);
    }

    private static boolean covered() {
        return switch (phase) {
            case TeleportViewState.OPEN -> breakTicks > SHATTER_DELAY + 3.0F;
            case TeleportViewState.HOLD -> true;
            case TeleportViewState.REVEAL -> broke && (!revealing || revealTicks < RETURN_SPREAD + RETURN_TICKS);
            default -> false;
        };
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (phase < 0 || pattern == null) return;
        Screen screen = choreography((float) event.getPartialTick());
        event.setRoll(event.getRoll() + screen.shake(0.0F));
        event.setPitch(event.getPitch() + screen.shake(1.7F) * 0.45F);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (phase >= 0 && covered()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (phase >= 0 && covered()) event.setCanceled(true);
    }

    public record Screen(int phase, float charge, float broken, boolean broke, boolean revealing, float reveal, float age, int seed)
            implements RealityBreak.Choreography, Impacts.Clock {

        private static float inner(FracturePattern.Cell cell) {
            return Mth.clamp(cell.reach() / REACH, 0.0F, 1.0F);
        }

        @Override
        public float age(int level) {
            if (level == SHATTER) return broke ? broken : -1.0F;
            return charge - HIT_AT[level];
        }

        public boolean reversing() {
            return phase == TeleportViewState.REVEAL && (revealing || !broke);
        }

        private int top() {
            for (int level = HITS - 1; level >= 0; level--) if (reached(level)) return level;
            return -1;
        }

        @Override
        public float undone(int level) {
            if (!reversing() || !reached(level)) return Impacts.NEVER;
            float from = broke ? UNHIT_FROM : FIZZLE_FROM;
            float clock = broke ? reveal : age;
            return clock - from - (top() - level) * UNHIT_SPACING;
        }

        public float end() {
            float from = broke ? UNHIT_FROM : FIZZLE_FROM;
            return from + Math.max(0, top()) * UNHIT_SPACING + SETTLE_TICKS;
        }

        public float severity() {
            return IMPACTS.severity(this);
        }

        public float shake(float offset) {
            return IMPACTS.shake(this, HIT_SHAKE, UNHIT_SHAKE, offset);
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            float flash = IMPACTS.flash(this, cell);
            if (phase == TeleportViewState.OPEN || phase == TeleportViewState.HOLD) {
                float since = broken - (SHATTER_DELAY + inner(cell) * BREAK_SPREAD + cell.wobble() * 1.5F + 0.5F);
                if (since < 0.0F) return IMPACTS.cracked(this, cell, seed, index, flash);
                return new RealityBreak.CellState(Math.min(1.0F, since / FALL_TICKS), true, 0.0F);
            }
            if (phase != TeleportViewState.REVEAL || !broke) return IMPACTS.cracked(this, cell, seed, index, flash);
            if (!revealing || IMPACTS.chipGone(this, cell, seed, index)) return new RealityBreak.CellState(1.0F, true, 0.0F);
            float returnAt = (1.0F - inner(cell)) * RETURN_SPREAD + 1.0F + cell.wobble();
            float back = (reveal - returnAt) / RETURN_TICKS;
            if (back <= 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
            if (back < 1.0F) return new RealityBreak.CellState(1.0F - back * back, true, 0.0F);
            float slam = Mth.clamp(1.0F - (reveal - returnAt - RETURN_TICKS) / FLASH_TICKS, 0.0F, 1.0F);
            return IMPACTS.cracked(this, cell, seed, index, Math.max(flash, slam));
        }

        @Override
        public float crackFront() {
            return IMPACTS.crackFront(this);
        }

        @Override
        public float crackGlow() {
            if (phase == TeleportViewState.REVEAL && broke && !revealing) return 0.85F;
            return IMPACTS.crackGlow(this, age);
        }

        @Override
        public float rimGlow() {
            return 0.9F;
        }
    }
}
