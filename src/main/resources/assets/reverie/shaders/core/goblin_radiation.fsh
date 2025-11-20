#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // 1. NABIZ (Yavaş ve Derin)
    float pulseSpeed = GameTime * 1200.0;
    float pulse = (sin(pulseSpeed) + 1.0) * 0.5;

    // 2. BLUR (Yumuşak Geçiş)
    float spread = 0.03;
    vec4 blurSum = vec4(0.0);

    blurSum += texture(Sampler0, texCoord0 + vec2(-spread, -spread));
    blurSum += texture(Sampler0, texCoord0 + vec2( 0.0,    -spread));
    blurSum += texture(Sampler0, texCoord0 + vec2( spread, -spread));
    blurSum += texture(Sampler0, texCoord0 + vec2(-spread,  0.0));
    blurSum += texture(Sampler0, texCoord0 + vec2( 0.0,     0.0));
    blurSum += texture(Sampler0, texCoord0 + vec2( spread,  0.0));
    blurSum += texture(Sampler0, texCoord0 + vec2(-spread,  spread));
    blurSum += texture(Sampler0, texCoord0 + vec2( 0.0,     spread));
    blurSum += texture(Sampler0, texCoord0 + vec2( spread,  spread));

    vec4 blurredTex = blurSum / 9.0;

    // 3. KENAR YUMUŞATMA & HALKA EFEKTİ
    // Merkeze olan uzaklık (0.0 -> 0.5)
    float dist = distance(texCoord0, vec2(0.5, 0.5));

    // a. Dış Sınır (Vignette): 0.5'e gelince yok olsun (Kareyi sil)
    float outerFade = 1.0 - smoothstep(0.35, 0.5, dist);

    // b. İç Boşluk (Hollow Center): Merkez biraz daha şeffaf olsun ki dışarısı "Outline" gibi dursun
    // Merkezden (0.0) 0.2'ye kadar hafifçe artan bir yoğunluk
    float innerDensity = smoothstep(0.0, 0.3, dist) + 0.3;

    // İkisini birleştir: Kenarları yumuşak ama ortası hafif boş bir halka
    float shapeMask = outerFade * innerDensity;

    // 4. RENK VE ALPHA
    vec3 glowColor = vec3(0.15, 1.0, 0.2); // Toksik Yeşil

    // Parlaklık Nabzı
    float brightness = 0.4 + (pulse * 0.3);

    // Final Alpha:
    // blurredTex.a : Dokunun şekli
    // shapeMask    : Halka efekti (Outline hissi veren kısım burası)
    float finalAlpha = blurredTex.a * shapeMask * brightness * 0.7;

    if (finalAlpha < 0.01) {
        discard;
    }

    vec4 finalColor = vec4(glowColor, finalAlpha);

    fragColor = finalColor * ColorModulator;
}