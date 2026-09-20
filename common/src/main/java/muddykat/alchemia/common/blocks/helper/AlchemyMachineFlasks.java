package muddykat.alchemia.common.blocks.helper;

import java.util.List;

public final class AlchemyMachineFlasks {

    public record Vertex(float x, float y, float z, float u, float v) {}

    public record Quad(List<Vertex> corners) {}

    public record Flask(String name, List<Quad> quads) {}

    private AlchemyMachineFlasks() {
    }

    public static final List<Flask> RIGHT = List.of(
        new Flask("double_flask_lower", List.of(
                new Quad(List.of(new Vertex(-0.0938F, 0.3750F, 0.3438F, 0.85156F, 0.17188F), new Vertex(0.2812F, 0.3750F, 0.3438F, 0.85156F, 0.19531F), new Vertex(0.2812F, 0.3750F, 0.7188F, 0.87500F, 0.19531F), new Vertex(-0.0938F, 0.3750F, 0.7188F, 0.87500F, 0.17188F))),
                new Quad(List.of(new Vertex(-0.2188F, 0.6875F, 0.2188F, 0.93750F, 0.24609F), new Vertex(-0.2188F, 0.5000F, 0.2188F, 0.93750F, 0.25781F), new Vertex(-0.2188F, 0.5000F, 0.8438F, 0.97656F, 0.25781F), new Vertex(-0.2188F, 0.6875F, 0.8438F, 0.97656F, 0.24609F))),
                new Quad(List.of(new Vertex(0.4062F, 0.6875F, 0.8438F, 0.71484F, 0.25391F), new Vertex(0.4062F, 0.5000F, 0.8438F, 0.71484F, 0.26562F), new Vertex(0.4062F, 0.5000F, 0.2188F, 0.75391F, 0.26562F), new Vertex(0.4062F, 0.6875F, 0.2188F, 0.75391F, 0.25391F))),
                new Quad(List.of(new Vertex(0.4062F, 0.6875F, 0.2188F, 0.94141F, 0.25781F), new Vertex(0.4062F, 0.5000F, 0.2188F, 0.94141F, 0.26953F), new Vertex(-0.2188F, 0.5000F, 0.2188F, 0.98047F, 0.26953F), new Vertex(-0.2188F, 0.6875F, 0.2188F, 0.98047F, 0.25781F))),
                new Quad(List.of(new Vertex(-0.2188F, 0.6875F, 0.8438F, 0.75391F, 0.26172F), new Vertex(-0.2188F, 0.5000F, 0.8438F, 0.75391F, 0.27344F), new Vertex(0.4062F, 0.5000F, 0.8438F, 0.79297F, 0.27344F), new Vertex(0.4062F, 0.6875F, 0.8438F, 0.79297F, 0.26172F))),
                new Quad(List.of(new Vertex(0.3438F, 0.7500F, 0.2812F, 0.90625F, 0.11719F), new Vertex(-0.1562F, 0.7500F, 0.2812F, 0.90625F, 0.14844F), new Vertex(-0.1562F, 0.7500F, 0.7812F, 0.93750F, 0.14844F), new Vertex(0.3438F, 0.7500F, 0.7812F, 0.93750F, 0.11719F))))),
        new Flask("double_flask_upper", List.of(
                new Quad(List.of(new Vertex(-0.0938F, 1.0585F, 0.3438F, 0.63281F, 0.24609F), new Vertex(-0.0938F, 0.8710F, 0.3438F, 0.63281F, 0.25781F), new Vertex(-0.0938F, 0.8710F, 0.7188F, 0.65625F, 0.25781F), new Vertex(-0.0938F, 1.0585F, 0.7188F, 0.65625F, 0.24609F))),
                new Quad(List.of(new Vertex(0.2812F, 1.0585F, 0.7188F, 0.97656F, 0.24609F), new Vertex(0.2812F, 0.8710F, 0.7188F, 0.97656F, 0.25781F), new Vertex(0.2812F, 0.8710F, 0.3438F, 1.00000F, 0.25781F), new Vertex(0.2812F, 1.0585F, 0.3438F, 1.00000F, 0.24609F))),
                new Quad(List.of(new Vertex(0.2812F, 1.0585F, 0.3438F, 0.97266F, 0.28125F), new Vertex(0.2812F, 0.8710F, 0.3438F, 0.97266F, 0.29297F), new Vertex(-0.0938F, 0.8710F, 0.3438F, 0.99609F, 0.29297F), new Vertex(-0.0938F, 1.0585F, 0.3438F, 0.99609F, 0.28125F))),
                new Quad(List.of(new Vertex(-0.0938F, 1.0585F, 0.7188F, 0.78906F, 0.28516F), new Vertex(-0.0938F, 0.8710F, 0.7188F, 0.78906F, 0.29688F), new Vertex(0.2812F, 0.8710F, 0.7188F, 0.81250F, 0.29688F), new Vertex(0.2812F, 1.0585F, 0.7188F, 0.81250F, 0.28516F))))),
        new Flask("separatory_lower", List.of(
                new Quad(List.of(new Vertex(0.4688F, 1.9375F, 1.7188F, 0.21484F, 0.08203F), new Vertex(0.4688F, 1.6875F, 1.7188F, 0.21484F, 0.09766F), new Vertex(0.4688F, 1.6875F, 1.5312F, 0.22656F, 0.09766F), new Vertex(0.4688F, 1.9375F, 1.5312F, 0.22656F, 0.08203F))),
                new Quad(List.of(new Vertex(0.0312F, 1.9375F, 1.5312F, 0.48828F, 0.10156F), new Vertex(0.0312F, 1.6875F, 1.5312F, 0.48828F, 0.11719F), new Vertex(0.0312F, 1.6875F, 1.7188F, 0.50000F, 0.11719F), new Vertex(0.0312F, 1.9375F, 1.7188F, 0.50000F, 0.10156F))),
                new Quad(List.of(new Vertex(0.1562F, 1.9375F, 1.8438F, 0.48828F, 0.11719F), new Vertex(0.1562F, 1.6875F, 1.8438F, 0.48828F, 0.13281F), new Vertex(0.3438F, 1.6875F, 1.8438F, 0.50000F, 0.13281F), new Vertex(0.3438F, 1.9375F, 1.8438F, 0.50000F, 0.11719F))),
                new Quad(List.of(new Vertex(0.3438F, 1.9375F, 1.4062F, 0.48828F, 0.13281F), new Vertex(0.3438F, 1.6875F, 1.4062F, 0.48828F, 0.14844F), new Vertex(0.1562F, 1.6875F, 1.4062F, 0.50000F, 0.14844F), new Vertex(0.1562F, 1.9375F, 1.4062F, 0.50000F, 0.13281F))))),
        new Flask("separatory_upper", List.of(
                new Quad(List.of(new Vertex(0.0000F, 2.5625F, 1.3750F, 0.28906F, 0.06250F), new Vertex(0.0000F, 2.3750F, 1.3750F, 0.28906F, 0.07422F), new Vertex(0.0000F, 2.3750F, 1.8750F, 0.32031F, 0.07422F), new Vertex(0.0000F, 2.5625F, 1.8750F, 0.32031F, 0.06250F))),
                new Quad(List.of(new Vertex(0.5000F, 2.5625F, 1.8750F, 0.92188F, 0.17969F), new Vertex(0.5000F, 2.3750F, 1.8750F, 0.92188F, 0.19141F), new Vertex(0.5000F, 2.3750F, 1.3750F, 0.95312F, 0.19141F), new Vertex(0.5000F, 2.5625F, 1.3750F, 0.95312F, 0.17969F))),
                new Quad(List.of(new Vertex(0.5000F, 2.5625F, 1.3750F, 0.43359F, 0.18359F), new Vertex(0.5000F, 2.3750F, 1.3750F, 0.43359F, 0.19531F), new Vertex(0.0000F, 2.3750F, 1.3750F, 0.46484F, 0.19531F), new Vertex(0.0000F, 2.5625F, 1.3750F, 0.46484F, 0.18359F))),
                new Quad(List.of(new Vertex(0.0000F, 2.5625F, 1.8750F, 0.46484F, 0.18359F), new Vertex(0.0000F, 2.3750F, 1.8750F, 0.46484F, 0.19531F), new Vertex(0.5000F, 2.3750F, 1.8750F, 0.49609F, 0.19531F), new Vertex(0.5000F, 2.5625F, 1.8750F, 0.49609F, 0.18359F))))),
        new Flask("burner_flask", List.of(
                new Quad(List.of(new Vertex(-0.8750F, 2.7111F, 1.0625F, 0.35156F, 0.05859F), new Vertex(-0.8750F, 2.5236F, 1.0625F, 0.35156F, 0.07031F), new Vertex(-0.8750F, 2.5236F, 1.6875F, 0.39062F, 0.07031F), new Vertex(-0.8750F, 2.7111F, 1.6875F, 0.39062F, 0.05859F))),
                new Quad(List.of(new Vertex(-0.2500F, 2.7111F, 1.6875F, 0.87500F, 0.21484F), new Vertex(-0.2500F, 2.5236F, 1.6875F, 0.87500F, 0.22656F), new Vertex(-0.2500F, 2.5236F, 1.0625F, 0.91406F, 0.22656F), new Vertex(-0.2500F, 2.7111F, 1.0625F, 0.91406F, 0.21484F))),
                new Quad(List.of(new Vertex(-0.2500F, 2.7111F, 1.0625F, 0.95312F, 0.21484F), new Vertex(-0.2500F, 2.5236F, 1.0625F, 0.95312F, 0.22656F), new Vertex(-0.8750F, 2.5236F, 1.0625F, 0.99219F, 0.22656F), new Vertex(-0.8750F, 2.7111F, 1.0625F, 0.99219F, 0.21484F))),
                new Quad(List.of(new Vertex(-0.8750F, 2.7111F, 1.6875F, 0.42969F, 0.21875F), new Vertex(-0.8750F, 2.5236F, 1.6875F, 0.42969F, 0.23047F), new Vertex(-0.2500F, 2.5236F, 1.6875F, 0.46875F, 0.23047F), new Vertex(-0.2500F, 2.7111F, 1.6875F, 0.46875F, 0.21875F)))))
    );

    public static final List<Flask> LEFT = List.of(
        new Flask("ground_flask", List.of(
                new Quad(List.of(new Vertex(0.3750F, -0.0000F, 0.2500F, 0.55469F, 0.09375F), new Vertex(0.8750F, -0.0000F, 0.2500F, 0.55469F, 0.12500F), new Vertex(0.8750F, 0.0000F, 0.7500F, 0.58594F, 0.12500F), new Vertex(0.3750F, 0.0000F, 0.7500F, 0.58594F, 0.09375F))))),
        new Flask("addition_flask", List.of(
                new Quad(List.of(new Vertex(0.3750F, 1.1250F, 0.2500F, 0.58594F, 0.18359F), new Vertex(0.3750F, 0.9375F, 0.2500F, 0.58594F, 0.19531F), new Vertex(0.3750F, 0.9375F, 0.7500F, 0.61719F, 0.19531F), new Vertex(0.3750F, 1.1250F, 0.7500F, 0.61719F, 0.18359F))),
                new Quad(List.of(new Vertex(0.8750F, 1.1250F, 0.7500F, 0.81250F, 0.18359F), new Vertex(0.8750F, 0.9375F, 0.7500F, 0.81250F, 0.19531F), new Vertex(0.8750F, 0.9375F, 0.2500F, 0.84375F, 0.19531F), new Vertex(0.8750F, 1.1250F, 0.2500F, 0.84375F, 0.18359F))),
                new Quad(List.of(new Vertex(0.8750F, 1.1250F, 0.2500F, 0.66406F, 0.18750F), new Vertex(0.8750F, 0.9375F, 0.2500F, 0.66406F, 0.19922F), new Vertex(0.3750F, 0.9375F, 0.2500F, 0.69531F, 0.19922F), new Vertex(0.3750F, 1.1250F, 0.2500F, 0.69531F, 0.18750F))),
                new Quad(List.of(new Vertex(0.3750F, 1.1250F, 0.7500F, 0.69531F, 0.18750F), new Vertex(0.3750F, 0.9375F, 0.7500F, 0.69531F, 0.19922F), new Vertex(0.8750F, 0.9375F, 0.7500F, 0.72656F, 0.19922F), new Vertex(0.8750F, 1.1250F, 0.7500F, 0.72656F, 0.18750F))))),
        new Flask("retort", List.of(
                new Quad(List.of(new Vertex(2.5000F, 1.6250F, 0.6875F, 0.61719F, 0.00000F), new Vertex(2.5000F, 1.2500F, 0.6875F, 0.61719F, 0.02344F), new Vertex(2.5000F, 1.2500F, 0.3125F, 0.64062F, 0.02344F), new Vertex(2.5000F, 1.6250F, 0.3125F, 0.64062F, 0.00000F))),
                new Quad(List.of(new Vertex(2.0000F, 1.6250F, 0.8125F, 0.35156F, 0.03125F), new Vertex(2.0000F, 1.2500F, 0.8125F, 0.35156F, 0.05469F), new Vertex(2.3750F, 1.2500F, 0.8125F, 0.37500F, 0.05469F), new Vertex(2.3750F, 1.6250F, 0.8125F, 0.37500F, 0.03125F))),
                new Quad(List.of(new Vertex(2.3750F, 1.6250F, 0.1875F, 0.42969F, 0.03125F), new Vertex(2.3750F, 1.2500F, 0.1875F, 0.42969F, 0.05469F), new Vertex(2.0000F, 1.2500F, 0.1875F, 0.45312F, 0.05469F), new Vertex(2.0000F, 1.6250F, 0.1875F, 0.45312F, 0.03125F))),
                new Quad(List.of(new Vertex(2.0000F, 1.1250F, 0.3125F, 0.47656F, 0.03125F), new Vertex(2.3750F, 1.1250F, 0.3125F, 0.47656F, 0.05469F), new Vertex(2.3750F, 1.1250F, 0.6875F, 0.50000F, 0.05469F), new Vertex(2.0000F, 1.1250F, 0.6875F, 0.50000F, 0.03125F))),
                new Quad(List.of(new Vertex(0.7312F, 1.0562F, 0.4062F, 0.70975F, 0.26172F), new Vertex(0.7312F, 1.0562F, 0.5938F, 0.70975F, 0.27344F), new Vertex(2.0000F, 1.4781F, 0.6344F, 0.62619F, 0.27598F), new Vertex(2.0000F, 1.4781F, 0.3656F, 0.62619F, 0.25918F))),
                new Quad(List.of(new Vertex(2.0000F, 1.4781F, 0.6344F, 0.11929F, 0.23234F), new Vertex(2.0000F, 1.7469F, 0.6344F, 0.11354F, 0.24813F), new Vertex(0.7312F, 1.2437F, 0.5938F, 0.19885F, 0.24574F), new Vertex(0.7312F, 1.0562F, 0.5938F, 0.20286F, 0.23473F))),
                new Quad(List.of(new Vertex(2.0000F, 1.7469F, 0.6344F, 0.83235F, 0.27090F), new Vertex(2.0000F, 1.7469F, 0.3656F, 0.83235F, 0.28770F), new Vertex(0.7312F, 1.2437F, 0.4062F, 0.91765F, 0.28516F), new Vertex(0.7312F, 1.2437F, 0.5938F, 0.91765F, 0.27344F))),
                new Quad(List.of(new Vertex(2.0000F, 1.7469F, 0.3656F, 0.43385F, 0.24016F), new Vertex(2.0000F, 1.4781F, 0.3656F, 0.43961F, 0.25594F), new Vertex(0.7312F, 1.0562F, 0.4062F, 0.52318F, 0.25355F), new Vertex(0.7312F, 1.2437F, 0.4062F, 0.51916F, 0.24254F))))),
        new Flask("spiral_flask", List.of(
                new Quad(List.of(new Vertex(1.1875F, 2.0625F, 0.9375F, 0.81250F, 0.00000F), new Vertex(1.1875F, 1.3125F, 0.9375F, 0.81250F, 0.04688F), new Vertex(1.1875F, 1.3125F, 1.3125F, 0.83594F, 0.04688F), new Vertex(1.1875F, 2.0625F, 1.3125F, 0.83594F, 0.00000F))),
                new Quad(List.of(new Vertex(1.5625F, 2.0625F, 1.3125F, 0.83594F, 0.00000F), new Vertex(1.5625F, 1.3125F, 1.3125F, 0.83594F, 0.04688F), new Vertex(1.5625F, 1.3125F, 0.9375F, 0.85938F, 0.04688F), new Vertex(1.5625F, 2.0625F, 0.9375F, 0.85938F, 0.00000F))),
                new Quad(List.of(new Vertex(1.5625F, 2.0625F, 0.9375F, 0.85938F, 0.00000F), new Vertex(1.5625F, 1.3125F, 0.9375F, 0.85938F, 0.04688F), new Vertex(1.1875F, 1.3125F, 0.9375F, 0.88281F, 0.04688F), new Vertex(1.1875F, 2.0625F, 0.9375F, 0.88281F, 0.00000F))),
                new Quad(List.of(new Vertex(1.1875F, 2.0625F, 1.3125F, 0.88281F, 0.00000F), new Vertex(1.1875F, 1.3125F, 1.3125F, 0.88281F, 0.04688F), new Vertex(1.5625F, 1.3125F, 1.3125F, 0.90625F, 0.04688F), new Vertex(1.5625F, 2.0625F, 1.3125F, 0.90625F, 0.00000F)))))
    );


    public static final List<Flask> TOP = List.of(
        new Flask("cascade_lower", List.of(
                new Quad(List.of(new Vertex(0.9375F, 1.6875F, 0.5000F, 0.98438F, 0.10938F), new Vertex(0.9375F, 1.4375F, 0.5000F, 0.98438F, 0.12500F), new Vertex(0.9375F, 1.4375F, 0.7500F, 1.00000F, 0.12500F), new Vertex(0.9375F, 1.6875F, 0.7500F, 1.00000F, 0.10938F))),
                new Quad(List.of(new Vertex(1.0625F, 1.6875F, 0.8750F, 0.98438F, 0.12500F), new Vertex(1.0625F, 1.4375F, 0.8750F, 0.98438F, 0.14062F), new Vertex(1.3125F, 1.4375F, 0.8750F, 1.00000F, 0.14062F), new Vertex(1.3125F, 1.6875F, 0.8750F, 1.00000F, 0.12500F))),
                new Quad(List.of(new Vertex(1.3125F, 1.6875F, 0.3750F, 0.62500F, 0.12891F), new Vertex(1.3125F, 1.4375F, 0.3750F, 0.62500F, 0.14453F), new Vertex(1.0625F, 1.4375F, 0.3750F, 0.64062F, 0.14453F), new Vertex(1.0625F, 1.6875F, 0.3750F, 0.64062F, 0.12891F))))),
        new Flask("cascade_mid", List.of(
                new Quad(List.of(new Vertex(0.7500F, 1.8750F, 0.7500F, 0.89062F, 0.14844F), new Vertex(0.7500F, 1.6250F, 0.7500F, 0.89062F, 0.16406F), new Vertex(0.7500F, 1.6250F, 0.5000F, 0.90625F, 0.16406F), new Vertex(0.7500F, 1.8750F, 0.5000F, 0.90625F, 0.14844F))),
                new Quad(List.of(new Vertex(0.2500F, 1.8750F, 0.5000F, 0.90625F, 0.14844F), new Vertex(0.2500F, 1.6250F, 0.5000F, 0.90625F, 0.16406F), new Vertex(0.2500F, 1.6250F, 0.7500F, 0.92188F, 0.16406F), new Vertex(0.2500F, 1.8750F, 0.7500F, 0.92188F, 0.14844F))),
                new Quad(List.of(new Vertex(0.3750F, 1.8750F, 0.8750F, 0.92188F, 0.14844F), new Vertex(0.3750F, 1.6250F, 0.8750F, 0.92188F, 0.16406F), new Vertex(0.6250F, 1.6250F, 0.8750F, 0.93750F, 0.16406F), new Vertex(0.6250F, 1.8750F, 0.8750F, 0.93750F, 0.14844F))),
                new Quad(List.of(new Vertex(0.6250F, 1.8750F, 0.3750F, 0.35547F, 0.15234F), new Vertex(0.6250F, 1.6250F, 0.3750F, 0.35547F, 0.16797F), new Vertex(0.3750F, 1.6250F, 0.3750F, 0.37109F, 0.16797F), new Vertex(0.3750F, 1.8750F, 0.3750F, 0.37109F, 0.15234F))))),
        new Flask("cascade_upper", List.of(
                new Quad(List.of(new Vertex(0.0625F, 2.0625F, 0.7500F, 0.59375F, 0.15234F), new Vertex(0.0625F, 1.8125F, 0.7500F, 0.59375F, 0.16797F), new Vertex(0.0625F, 1.8125F, 0.5000F, 0.60938F, 0.16797F), new Vertex(0.0625F, 2.0625F, 0.5000F, 0.60938F, 0.15234F))),
                new Quad(List.of(new Vertex(-0.4375F, 2.0625F, 0.5000F, 0.93750F, 0.15625F), new Vertex(-0.4375F, 1.8125F, 0.5000F, 0.93750F, 0.17188F), new Vertex(-0.4375F, 1.8125F, 0.7500F, 0.95312F, 0.17188F), new Vertex(-0.4375F, 2.0625F, 0.7500F, 0.95312F, 0.15625F))),
                new Quad(List.of(new Vertex(-0.3125F, 2.0625F, 0.8750F, 0.97656F, 0.15625F), new Vertex(-0.3125F, 1.8125F, 0.8750F, 0.97656F, 0.17188F), new Vertex(-0.0625F, 1.8125F, 0.8750F, 0.99219F, 0.17188F), new Vertex(-0.0625F, 2.0625F, 0.8750F, 0.99219F, 0.15625F))),
                new Quad(List.of(new Vertex(-0.0625F, 2.0625F, 0.3750F, 0.71875F, 0.16016F), new Vertex(-0.0625F, 1.8125F, 0.3750F, 0.71875F, 0.17578F), new Vertex(-0.3125F, 1.8125F, 0.3750F, 0.73438F, 0.17578F), new Vertex(-0.3125F, 2.0625F, 0.3750F, 0.73438F, 0.16016F)))))
    );

    public static List<Flask> forSide(UpgradeSide side) {
        return switch (side) {
            case RIGHT -> RIGHT;
            case LEFT -> LEFT;
            case TOP -> TOP;
        };
    }
}
