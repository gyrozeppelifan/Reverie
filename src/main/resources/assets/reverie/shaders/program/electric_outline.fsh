#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Time değişkenini boşuna kullanıyormuş gibi yapalım ki uyarı vermesin
    float t = Time * 0.00001;

    // O anki pikseli al
    vec4 sampleColor = texture(DiffuseSampler, texCoord);

    // Eğer burada bir Entity çizilmişse (Alpha 0'dan büyükse)
    if (sampleColor.a > 0.01) {
        // Entity'i KIRMIZI yap
        fragColor = vec4(1.0, 0.0, 0.0, 1.0 + t);
    }
    else {
        // Entity yoksa arka planı yarı saydam MAVİ yap
        // EKRAN MAVİ OLURSA SHADER ÇALIŞIYOR DEMEKTİR!
        fragColor = vec4(0.0, 0.0, 1.0, 0.4 + t);
    }
}