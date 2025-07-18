#version 150
in vec2 TexCoord0;
out vec4 FragColor;
uniform sampler2D uDiffuseSampler;
uniform vec2 uInverseTextureSize; // 1.0/width,1.0/height

void main() {
    // Basit Sobel kenar detektörü
    float dx = texture(uDiffuseSampler, TexCoord0 + vec2(uInverseTextureSize.x,0)).a
             - texture(uDiffuseSampler, TexCoord0 - vec2(uInverseTextureSize.x,0)).a;
    float dy = texture(uDiffuseSampler, TexCoord0 + vec2(0,uInverseTextureSize.y)).a
             - texture(uDiffuseSampler, TexCoord0 - vec2(0,uInverseTextureSize.y)).a;
    float edge = length(vec2(dx,dy));
    // Pembe tint ve edge mask
    FragColor = vec4(1.0, 0.0, 0.5, edge);
}
