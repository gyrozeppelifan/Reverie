#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
// uniform vec2 OutSize; // Bunu kullanmıyoruz, otomatik hesaplayacağız

in vec2 texCoord;
out vec4 fragColor;

float random(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    // 1. Ekran Boyutunu Otomatik Al
    ivec2 texSize = textureSize(DiffuseSampler, 0);
    vec2 oneTexel = 1.0 / vec2(texSize);

    // 2. İçini Boşalt
    float centerAlpha = texture(DiffuseSampler, texCoord).a;
    if (centerAlpha > 0.1) {
        discard;
    }

    // 3. Kenar Bul (Edge Detection)
    float alphaSum = 0.0;
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