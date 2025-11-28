#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Time kullanımı (Optimizer silmesin diye)
    float activeTime = Time * 0.0001;

    // Dinamik Boyut Hesaplama
    ivec2 texSize = textureSize(DiffuseSampler, 0);
    vec2 oneTexel = 1.0 / vec2(texSize);

    vec4 centerColor = texture(DiffuseSampler, texCoord);
    float alphaSum = 0.0;

    // 3x3 Kenar Tarama
    for(int x = -1; x <= 1; x++) {
        for(int y = -1; y <= 1; y++) {
            if(x == 0 && y == 0) continue;
            alphaSum += texture(DiffuseSampler, texCoord + vec2(x, y) * oneTexel).a;
        }
    }

    // Mantık: Merkez boş ama etraf doluysa -> Kenar
    if (centerColor.a < 0.1 && alphaSum > 0.0) {
        // Neon Turkuaz
        fragColor = vec4(0.0, 1.0, 1.0, 1.0);
    } else {
        // Arka planı veya içini çizme (0 Alpha)
        fragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}