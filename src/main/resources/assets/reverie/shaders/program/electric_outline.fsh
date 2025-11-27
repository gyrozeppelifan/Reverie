#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // 1. Otomatik Boyut Hesaplama (Java'dan beklemeye gerek yok!)
    ivec2 texSize = textureSize(DiffuseSampler, 0);
    vec2 oneTexel = 1.0 / vec2(texSize);

    // 2. İçini Boşalt
    float centerAlpha = texture(DiffuseSampler, texCoord).a;
    if (centerAlpha > 0.1) {
        discard;
    }

    // 3. Kenar Bul
    float alphaSum = 0.0;
    // 2 piksellik tarama
    for(int x = -1; x <= 1; x++) {
        for(int y = -1; y <= 1; y++) {
            if(x == 0 && y == 0) continue;
            alphaSum += texture(DiffuseSampler, texCoord + vec2(x, y) * oneTexel).a;
        }
    }

    // 4. Boya
    if (alphaSum > 0.0) {
        // Titreme
        float flicker = sin(Time * 30.0) * 0.5 + 0.5;
        if (flicker < 0.15) discard;

        // RENK: Neon Turkuaz
        fragColor = vec4(0.2, 1.0, 1.0, 1.0);
    } else {
        discard;
    }
}