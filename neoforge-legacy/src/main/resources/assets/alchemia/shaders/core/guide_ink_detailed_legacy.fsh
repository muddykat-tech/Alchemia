#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const vec3 OUTLINE = vec3(0.408, 0.290, 0.196);
const vec3 SHADE_0 = vec3(0.502, 0.376, 0.262);
const vec3 SHADE_1 = vec3(0.537, 0.415, 0.295);
const vec3 SHADE_2 = vec3(0.573, 0.455, 0.327);
const vec3 SHADE_3 = vec3(0.608, 0.494, 0.360);
const vec3 SHADE_4 = vec3(0.643, 0.533, 0.392);
const vec3 SHADE_5 = vec3(0.676, 0.572, 0.429);
const vec3 SHADE_6 = vec3(0.710, 0.612, 0.467);
const vec3 SHADE_7 = vec3(0.743, 0.651, 0.504);
const vec3 SHADE_8 = vec3(0.776, 0.690, 0.541);
const vec3 SHADE_9 = vec3(0.808, 0.727, 0.580);
const vec3 SHADE_10 = vec3(0.839, 0.763, 0.621);
const vec3 SHADE_11 = vec3(0.871, 0.800, 0.659);

float alphaAt(vec2 offset) {
    return texture(Sampler0, texCoord0 + offset).a;
}

vec3 detailedInk(float light) {
    if (light < 0.10) return SHADE_0;
    if (light < 0.16) return SHADE_1;
    if (light < 0.22) return SHADE_2;
    if (light < 0.28) return SHADE_3;
    if (light < 0.34) return SHADE_4;
    if (light < 0.40) return SHADE_5;
    if (light < 0.46) return SHADE_6;
    if (light < 0.52) return SHADE_7;
    if (light < 0.58) return SHADE_8;
    if (light < 0.64) return SHADE_9;
    if (light < 0.70) return SHADE_10;
    return SHADE_11;
}

vec3 detailedItemInk(float light) {
    if (light < 0.083333) return SHADE_0;
    if (light < 0.166667) return SHADE_1;
    if (light < 0.250000) return SHADE_2;
    if (light < 0.333333) return SHADE_3;
    if (light < 0.416667) return SHADE_4;
    if (light < 0.500000) return SHADE_5;
    if (light < 0.583333) return SHADE_6;
    if (light < 0.666667) return SHADE_7;
    if (light < 0.750000) return SHADE_8;
    if (light < 0.833333) return SHADE_9;
    if (light < 0.916667) return SHADE_10;
    return SHADE_11;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }

    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    float edge = min(min(alphaAt(vec2(texel.x, 0.0)), alphaAt(vec2(-texel.x, 0.0))),
                     min(alphaAt(vec2(0.0, texel.y)), alphaAt(vec2(0.0, -texel.y))));

    float light = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    bool hasItemRange = vertexColor.b < 0.01;
    if (hasItemRange) {
        light = clamp((light - vertexColor.r) / max(vertexColor.g - vertexColor.r, 0.003922), 0.0, 1.0);
    }

    vec3 ink = edge < 0.1 ? OUTLINE : hasItemRange ? detailedItemInk(light) : detailedInk(light);
    fragColor = vec4(hasItemRange ? ink : ink * vertexColor.rgb, vertexColor.a) * ColorModulator;
}
