#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);

    // 1. İÇİNİ BOŞALT: Eğer piksel doluysa (Entity'nin kendisi) SİL!
    if (center.a > 0.1) {
        discard; // Burası şeffaf olacak
    }

    // 2. KENAR BUL: Etrafta dolu piksel var mı?
    vec2 oneTexel = 1.0 / OutSize;
    float alphaSum = 0.0;

    // 2 piksellik genişlikte tara
    for(int x = -1; x <= 1; x++) {
        for(int y = -1; y <= 1; y++) {
            if(x==0 && y==0) continue;
            alphaSum += texture(DiffuseSampler, texCoord + vec2(x, y) * oneTexel).a;
        }
    }

    // Biz boşuz ama komşu dolu -> KENAR!
    if (alphaSum > 0.0) {
        // Titreme
        float flicker = sin(Time * 25.0) * 0.5 + 0.5;
        if (flicker < 0.15) discard;

        // RENK: Neon Turkuaz
        fragColor = vec4(0.2, 1.0, 1.0, 1.0);
    } else {
        discard; // Dışarıdaki boşluk
    }
}