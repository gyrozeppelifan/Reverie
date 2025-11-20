#version 150

uniform sampler2D DiffuseSampler;
uniform float GlowStrength; // 0.0 (kapalı) - 1.0 (tam açık)

in vec2 texCoord;
in vec4 vertexColor; // Buradaki alpha, renderer'dan gelen genel şeffaflık (0.6F)

out vec4 fragColor;

void main() {
    vec4 baseTexColor = texture(DiffuseSampler, texCoord); // Orijinal dokunun rengi

    // Eğer orijinal dokuda hiçbir şey yoksa (tamamen şeffaf alan) çizme
    if (baseTexColor.a < 0.1) {
        discard;
    }

    // --- 1. BLUR MİKTARI ---
    // GlowStrength arttıkça daha fazla bulanıklaşsın ve yayılsın
    // Bu değer ne kadar büyük olursa o kadar geniş yayılır.
    float blurRadius = 0.012 * GlowStrength; // Önceki 0.008'den daha agresif

    // --- 2. GAUSSIAN BLUR (9 Noktalı) ---
    // Daha yumuşak bir geçiş için 9 noktadan daha fazla örnek alıyoruz.
    vec4 blurredSum = vec4(0.0);
    blurredSum += texture(DiffuseSampler, texCoord + vec2(-blurRadius, -blurRadius));
    blurredSum += texture(DiffuseSampler, texCoord + vec2(0.0, -blurRadius));
    blurredSum += texture(DiffuseSampler, texCoord + vec2(blurRadius, -blurRadius));
    blurredSum += texture(DiffuseSampler, texCoord + vec2(-blurRadius, 0.0));
    blurredSum += texture(DiffuseSampler, texCoord); // Merkez nokta
    blurredSum += texture(DiffuseSampler, texCoord + vec2(blurRadius, 0.0));
    blurredSum += texture(DiffuseSampler, texCoord + vec2(-blurRadius, blurRadius));
    blurredSum += texture(DiffuseSampler, texCoord + vec2(0.0, blurRadius));
    blurredSum += texture(DiffuseSampler, texCoord + vec2(blurRadius, blurRadius));

    // Ortalama al (9 örnek)
    vec4 finalBlurredColor = blurredSum / 9.0;

    // --- 3. RENK AYARI: TAMAMEN YEŞİL YAPMA ---
    // Burası çok önemli!
    // baseTexColor'ın alpha değerini kullanarak sadece dokunun olduğu yerleri etkileyeceğiz.
    // blurredColor'ın RGB değerlerini de kullanarak ışığın yayılma hissini artıracağız.

    vec3 neonGreen = vec3(0.1, 1.0, 0.1); // Canlı neon yeşili

    // Bayrağın orijinal dokusunun ne kadarını yeşile çevireceğimiz (GlowStrength'e bağlı)
    // GlowStrength 1.0 iken tamamen neon yeşil olacak.
    vec3 resultRGB = mix(baseTexColor.rgb, neonGreen, GlowStrength);

    // Bulanıklığın yeşil ışığa katkısı
    // Bulanık kısımlardaki renkleri de yeşil ile karıştırıp yoğunlaştırıyoruz
    resultRGB += (finalBlurredColor.rgb * neonGreen * GlowStrength * 0.7);

    // --- 4. ŞEFFAFLIK AYARI ---
    // GlowStrength arttıkça alpha değeri de artsın.
    // vertexColor.a (renderer'dan gelen 0.6) ile çarparak genel şeffaflığı koruyoruz.
    float finalAlpha = baseTexColor.a * GlowStrength * vertexColor.a;

    fragColor = vec4(resultRGB, finalAlpha);
}