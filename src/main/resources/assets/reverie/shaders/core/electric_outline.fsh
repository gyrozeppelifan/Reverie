#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform vec2 OneTexel;

in vec2 texCoord;

out vec4 fragColor;

// Rastgele sayı üretici (Gürültü/Noise için)
float random(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    // 1. Mevcut pikseli kontrol et
    float center = texture(DiffuseSampler, texCoord).a;

    // Eğer zaten mobun kendisiyse (içi doluysa) çizme, sadece dış hat istiyoruz.
    // X-Ray istiyorsan burayı kapatabilirsin ama genelde sadece outline istenir.
    if (center > 0.1) {
        discard;
    }

    // 2. Kenar Tespiti (Etrafta dolu piksel var mı?)
    float alphaSum = 0.0;
    // Çizgi kalınlığı için döngü (Daha kalın istersen 1 yerine 2 yap)
    for(int x = -1; x <= 1; x++) {
        for(int y = -1; y <= 1; y++) {
            vec2 offset = vec2(x, y) * OneTexel;
            alphaSum += texture(DiffuseSampler, texCoord + offset).a;
        }
    }

    // 3. Elektrik Efekti
    if (alphaSum > 0.0) {
        // Zamanla kayan bir gürültü (Elektrik akımı hissi)
        float noise = random(texCoord + vec2(0.0, Time * 2.0));

        // Titreme (Flicker) - Bazı karelerde hat kaybolup gelir
        float flicker = sin(Time * 20.0) * 0.5 + 0.5;
        if (flicker < 0.2) discard; // Arada kesilme efekti

        // Renk: Neon Turkuaz (R:0.2, G:1.0, B:1.0)
        vec4 electricColor = vec4(0.2, 1.0, 1.0, 1.0);

        // Kenara ne kadar yakınsak o kadar parlak
        fragColor = electricColor * (0.5 + noise * 0.5);
    } else {
        discard;
    }
}