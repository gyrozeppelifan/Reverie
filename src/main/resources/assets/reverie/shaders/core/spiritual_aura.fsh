#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 vNormal; // Vertex Shader'dan gelen veri

out vec4 fragColor;

void main() {
    // 1. ZAMAN (Yavaş ve asil bir dalgalanma)
    float time = GameTime * 1200.0;

    // 2. TEXTURE (Sadece şekil maskesi olarak kullanacağız, rengini çok takmayacağız)
    vec4 texColor = texture(Sampler0, texCoord0);
    if (texColor.a < 0.1) discard;

    // --- 3. RENK PALETİ (Senin istediğin tonlar) ---
    vec3 colorCyan = vec3(0.6, 1.0, 0.84); // #99ffd6
    vec3 colorGold = vec3(1.0, 0.8, 0.2);
    vec3 colorWhite = vec3(0.95, 0.95, 1.0);

    // Renkleri karıştır (Aşağıdan yukarıya ve zamanla değişen bir geçiş)
    float wave = sin(time * 0.5 + texCoord0.y * 3.0); // -1 ile 1 arası
    vec3 finalColor = mix(colorCyan, colorGold, (wave + 1.0) * 0.2); // %20 Altın karışımı

    // Arada beyaz parıltılar at
    float sparkle = max(0.0, sin(time * 1.5 + texCoord0.y * 10.0 + texCoord0.x * 5.0));
    if (sparkle > 0.9) finalColor = mix(finalColor, colorWhite, 0.5);

    // --- 4. FRESNEL EFEKTİ (YUMUŞAK GEÇİŞ SIRRI) ---
    // Yüzeyin kameraya ne kadar dik baktığını bulur.
    // Kenarlar (0.0) -> Parlak, Orta (1.0) -> Şeffaf
    vec3 normal = normalize(vNormal);
    float viewDot = dot(normal, vec3(0.0, 0.0, 1.0)); // Kamera her zaman Z eksenindedir
    float fresnel = 1.0 - max(0.0, viewDot);

    // Kenarları daha keskin, içleri daha yumuşak yap (Üs alarak)
    fresnel = pow(fresnel, 1.5);

    // --- 5. FINAL ALPHA ---
    // Kenarlarda alpha yüksek, ortada düşük olsun.
    // Ayrıca zamanla tüm aura hafifçe nefes alsın.
    float alphaPulse = 0.3 + 0.2 * sin(time * 0.8);
    float finalAlpha = fresnel * alphaPulse;

    // Çok silik olmasın, en az %10 görünsün
    finalAlpha = clamp(finalAlpha + 0.1, 0.0, 0.6);

    fragColor = vec4(finalColor, finalAlpha * texColor.a) * ColorModulator;
}