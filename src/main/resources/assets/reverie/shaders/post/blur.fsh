#version 150
in vec2 TexCoord0;
out vec4 FragColor;
uniform sampler2D uDiffuseSampler;
uniform vec2 BlurDir;
uniform float Radius;

void main() {
    vec2 inv = vec2(1.0) / vec2(textureSize(uDiffuseSampler,0));
    vec4 sum = vec4(0);
    float total = 0.0;
    for (float i = -Radius; i <= Radius; i += 1.0) {
        vec2 off = BlurDir * i * inv;
        sum += texture(uDiffuseSampler, TexCoord0 + off);
        total += 1.0;
    }
    FragColor = sum / total;
}
