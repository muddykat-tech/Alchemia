#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const vec3 OUTLINE = vec3(0.408, 0.290, 0.196);
const vec3 DEEP = vec3(0.502, 0.376, 0.262);
const vec3 MID = vec3(0.643, 0.533, 0.392);
const vec3 LIGHT = vec3(0.776, 0.690, 0.541);
const vec3 PALE = vec3(0.871, 0.800, 0.659);

float alphaAt(vec2 offset) {
    return texture(Sampler0, texCoord0 + offset).a;
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
    vec3 ink;
    if (edge < 0.1) {
        ink = OUTLINE;
    } else if (light < 0.22) {
        ink = DEEP;
    } else if (light < 0.45) {
        ink = MID;
    } else if (light < 0.70) {
        ink = LIGHT;
    } else {
        ink = PALE;
    }
    fragColor = vec4(ink * vertexColor.rgb, vertexColor.a) * ColorModulator;
}
