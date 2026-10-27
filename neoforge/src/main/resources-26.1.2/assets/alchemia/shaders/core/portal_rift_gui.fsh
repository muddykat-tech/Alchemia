#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:matrix.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const int LAYERS = 15;

const vec3[] COLORS = vec3[](
    vec3(0.022087, 0.098399, 0.110818),
    vec3(0.011892, 0.095924, 0.089485),
    vec3(0.027636, 0.101689, 0.100326),
    vec3(0.046564, 0.109883, 0.114838),
    vec3(0.064901, 0.117696, 0.097189),
    vec3(0.063761, 0.086895, 0.123646),
    vec3(0.084817, 0.111994, 0.166380),
    vec3(0.097489, 0.154120, 0.091064),
    vec3(0.106152, 0.131144, 0.195191),
    vec3(0.097721, 0.110188, 0.187229),
    vec3(0.133516, 0.138278, 0.148582),
    vec3(0.070006, 0.243332, 0.235792),
    vec3(0.196766, 0.142899, 0.214696),
    vec3(0.047281, 0.315338, 0.321970),
    vec3(0.204675, 0.390010, 0.302066),
    vec3(0.080955, 0.314821, 0.661491)
);

const vec3 VIOLET = vec3(0.52, 0.32, 1.0);
const vec3 LAVENDER = vec3(0.82, 0.64, 1.0);
const vec3 POWDER = vec3(0.62, 0.86, 1.0);
const vec3 VOID = vec3(0.03, 0.015, 0.065);

const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 portal_layer(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));
    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);
    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

vec3 tint(int index) {
    float t = float(index) / float(LAYERS);
    vec3 hue = mix(mix(VIOLET, LAVENDER, smoothstep(0.0, 0.6, t)), POWDER, smoothstep(0.55, 1.0, t));
    return hue * dot(COLORS[index], vec3(0.3, 0.45, 0.25)) * 2.2;
}

void main() {
    vec4 screen = vec4(gl_FragCoord.xy / ScreenSize, 0.0, 1.0);
    vec3 colour = VOID;
    for (int i = 0; i < LAYERS; i++) {
        vec4 coord = screen * portal_layer(float(i + 1));
        colour += texture(Sampler0, coord.xy / coord.w).rgb * tint(i);
    }

    float rim = pow(clamp(vertexColor.r, 0.0, 1.0), 2.5);
    colour += vec3(0.42, 0.32, 0.9) * rim * 0.55;

    fragColor = vec4(colour, vertexColor.a) * ColorModulator;
}
