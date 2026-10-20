package muddykat.alchemia.client.effect;

#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
#else
import com.mojang.blaze3d.pipeline.RenderPipeline;
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
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
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
    public static final float CHARGE_CRACK_TICKS = 24.0F;
    public static final float BREAK_SPREAD = 8.0F;
    public static final float FALL_TICKS = 10.0F;
    public static final float RETURN_SPREAD = 9.0F;
    public static final float RETURN_TICKS = 5.0F;
    public static final float FLASH_TICKS = 5.0F;
    public static final float HEAL_FROM = 14.0F;
    public static final float FIZZLE_TICKS = 12.0F;

    private static final int REVEAL_WAIT = 60;
    private static final String DEPART = "teleport:depart";
    private static final String ARRIVE = "teleport:arrive";

    private static int phase = -1;
    private static int seenRevision = -1;
    private static float phaseTicks;
    private static float breakTicks;
    private static float revealTicks;
    private static int waitTicks;
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
        if (phase == TeleportViewState.OPEN || phase == TeleportViewState.HOLD) breakTicks += 1.0F;
        if (phase != TeleportViewState.REVEAL) return;
        if (!broke) {
            if (phaseTicks >= FIZZLE_TICKS) finish();
            return;
        }
        if (!revealing) {
            if (sectionReady(minecraft) || ++waitTicks > REVEAL_WAIT) revealing = true;
            return;
        }
        revealTicks += 1.0F;
        if (revealTicks >= TeleportViewState.REVEAL_TICKS) finish();
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
                breakTicks = 0.0F;
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
        RealityBreak.Frame frame = RealityBreak.frame(current, plane(width, height), choreography(partial), seed, mapping);
        ScreenRectangle bounds = new ScreenRectangle(0, 0, width, height);
        PoseStack.Pose pose = new PoseStack().last();
        boolean shaded = false;

#if MC_VER >= 26300
        shaded = true;
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
#endif

        boolean flatVoid = !shaded;
        graphics.nextStratum();
        graphics.submitGuiElementRenderState(new Layer(RenderPipelines.GUI, TextureSetup.noTexture(), bounds, buffer -> {
            if (flatVoid && frame.holes()) frame.voids(pose, buffer, false);
            if (frame.flights()) frame.shards(pose, buffer, false);
            frame.lines(pose, buffer);
        }));
    }

    private static int captureSlot() {
        switch (phase) {
            case TeleportViewState.CHARGE -> {
                SceneCapture.request();
                return -1;
            }
            case TeleportViewState.OPEN, TeleportViewState.HOLD -> {
                int slot = SceneCapture.slotOf(DEPART);
                if (slot < 0 && breakTicks < 2.0F) slot = SceneCapture.freeze(DEPART);
                if (phase == TeleportViewState.HOLD) SceneCapture.request();
                return slot;
            }
            case TeleportViewState.REVEAL -> {
                if (!broke) return -1;
                if (!revealing) {
                    SceneCapture.request();
                    return -1;
                }
                int slot = SceneCapture.slotOf(ARRIVE);
                if (slot < 0 && revealTicks < 2.0F) slot = SceneCapture.freeze(ARRIVE);
                return slot;
            }
            default -> {
                return -1;
            }
        }
    }

    private static RealityBreak.Plane plane(int width, int height) {
        float radius = (float) Math.sqrt(width * width + height * height) * 0.5F / REACH;
        return new RealityBreak.Plane(new Vector3f(width * 0.5F, height * 0.5F, 0.0F), new Vector3f(1.0F, 0.0F, 0.0F),
                new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.001F), radius, radius, radius / LINE_SCALE);
    }

    private static Screen choreography(float partial) {
        float charge = phase == TeleportViewState.CHARGE ? phaseTicks + partial : CHARGE_CRACK_TICKS;
        float reveal = revealing ? revealTicks + partial : 0.0F;
        return new Screen(phase, charge, breakTicks + partial, broke, revealing, reveal, phaseTicks + partial);
    }

    private static boolean covered() {
        return switch (phase) {
            case TeleportViewState.OPEN -> breakTicks > 3.0F;
            case TeleportViewState.HOLD -> true;
            case TeleportViewState.REVEAL -> broke && (!revealing || revealTicks < RETURN_SPREAD + RETURN_TICKS);
            default -> false;
        };
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (phase >= 0 && covered()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (phase >= 0 && covered()) event.setCanceled(true);
    }

    public record Screen(int phase, float charge, float broken, boolean broke, boolean revealing, float reveal, float age)
            implements RealityBreak.Choreography {

        private static float inner(FracturePattern.Cell cell) {
            return Mth.clamp(cell.reach() / REACH, 0.0F, 1.0F);
        }

        private static float smooth(float value) {
            float t = Mth.clamp(value, 0.0F, 1.0F);
            return t * t * (3.0F - 2.0F * t);
        }

        @Override
        public RealityBreak.CellState cell(FracturePattern.Cell cell, int index) {
            if (phase == TeleportViewState.OPEN || phase == TeleportViewState.HOLD) {
                float since = broken - (inner(cell) * BREAK_SPREAD + cell.wobble() * 1.5F + 0.5F);
                if (since < 0.0F) return RealityBreak.CellState.REST;
                return new RealityBreak.CellState(Math.min(1.0F, since / FALL_TICKS), true, 0.0F);
            }
            if (phase != TeleportViewState.REVEAL || !broke) return RealityBreak.CellState.REST;
            if (!revealing) return new RealityBreak.CellState(1.0F, true, 0.0F);
            float returnAt = (1.0F - inner(cell)) * RETURN_SPREAD + 1.0F + cell.wobble();
            float back = (reveal - returnAt) / RETURN_TICKS;
            if (back <= 0.0F) return new RealityBreak.CellState(1.0F, true, 0.0F);
            if (back < 1.0F) return new RealityBreak.CellState(1.0F - back * back, true, 0.0F);
            float flash = 1.0F - (reveal - returnAt - RETURN_TICKS) / FLASH_TICKS;
            return new RealityBreak.CellState(0.0F, false, Mth.clamp(flash, 0.0F, 1.0F));
        }

        @Override
        public float crackFront() {
            return phase == TeleportViewState.CHARGE ? 0.95F * smooth(charge / CHARGE_CRACK_TICKS) : 1.2F;
        }

        @Override
        public float crackGlow() {
            float pulse = 0.12F * (float) Math.sin(age * 0.35F);
            return switch (phase) {
                case TeleportViewState.CHARGE -> 0.6F + pulse;
                case TeleportViewState.REVEAL -> {
                    if (!broke) yield 0.6F * (1.0F - smooth(age / FIZZLE_TICKS));
                    if (!revealing) yield 0.85F;
                    float heal = Mth.clamp((reveal - HEAL_FROM) / (TeleportViewState.REVEAL_TICKS - HEAL_FROM), 0.0F, 1.0F);
                    yield 0.6F * (1.0F - heal) * (1.0F - heal);
                }
                default -> 0.85F;
            };
        }

        @Override
        public float rimGlow() {
            return 0.9F;
        }
    }
}
