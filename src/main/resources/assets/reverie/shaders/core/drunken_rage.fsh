#version 150

// Arka planı örneklemeye gerek yok, sadece üstüne renk bindireceğiz.
// uniform sampler2D DiffuseSampler;
uniform float GameTime;
uniform vec2 InSize; // Ekran boyutu (Java'dan geliyor)

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;

    // --- VIGNETTE (KENAR KARARTMA) HESABI ---

    // 1. Merkeze olan uzaklığı bul
    vec2 centerVector = uv - vec2(0.5);

    // Ekran en/boy oranını hesaba kat (Yoksa daire yerine yumurta gibi olur)
    float aspectRatio = InSize.x / InSize.y;
    // Merkeze uzaklık (0.0 = merkez, 0.5+ = köşeler)
    float dist = length(centerVector * vec2(aspectRatio, 1.0));

    // 2. Nabız animasyonu (Zamana göre 0.0 ile 1.0 arası gidip gelir)
    float pulse = (sin(GameTime * 4.0) + 1.0) * 0.5;

    // 3. Yoğunluk Haritası
    // smoothstep(iç_sınır, dış_sınır, uzaklık)
    // Ekranın %30'undan sonra başlar, %75'inde maksimuma ulaşır.
    float vignetteIntensity = smoothstep(0.3, 0.75, dist);

    // --- RENK VE ALPHA (SAYDAMLIK) ---

    // Kırmızımsı Mor Renk Tonu
    vec3 rageColor = vec3(0.8, 0.05, 0.5);

    // ALPHA HESABI (Kilit Nokta Burası!)
    // Yoğunluk * (Temel Görünürlük + Nabız Etkisi)
    // Merkezde intensity 0 olduğu için alpha 0 olur (Tam Şeffaf).
    // Kenarlarda alpha yaklaşık 0.6'ya kadar çıkar (Yarı Şeffaf).
    float alpha = vignetteIntensity * (0.3 + pulse * 0.3);

    // Final rengi ve hesapladığımız alpha'yı çıktı ver.
    // Java tarafındaki blending bunu oyunun üstüne bindirecek.
    fragColor = vec4(rageColor, alpha);
}